package ru.yandex.practicum.aggregator.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.aggregator.kafka.SnapshotKafkaProducer;
import ru.yandex.practicum.aggregator.serialization.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class AggregationStarter {

    private static final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final AggregatorKafkaProperties properties;
    private final SnapshotService snapshotService;
    private final SnapshotKafkaProducer snapshotKafkaProducer;

    public AggregationStarter(AggregatorKafkaProperties properties,
                              SnapshotService snapshotService,
                              SnapshotKafkaProducer snapshotKafkaProducer) {
        this.properties = properties;
        this.snapshotService = snapshotService;
        this.snapshotKafkaProducer = snapshotKafkaProducer;
    }

    public void start() {
        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(buildConsumerProperties());
        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(buildProducerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения, будим consumer");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(properties.getTopics().getSensors()));
            log.info("Aggregator подписался на topic={}", properties.getTopics().getSensors());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofSeconds(1));

                for (var record : records) {
                    SensorEventAvro event = record.value();
                    if (event == null) {
                        continue;
                    }

                    snapshotService.updateState(event)
                            .ifPresent(snapshot -> snapshotKafkaProducer.send(producer, snapshot));
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {
            log.info("Consumer остановлен через wakeup()");
        } catch (Exception e) {
            log.error("Ошибка во время агрегации событий датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("Ошибка при финальном flush/commit", e);
            } finally {
                log.info("Закрываем consumer");
                consumer.close();
                log.info("Закрываем producer");
                producer.close();
            }
        }
    }

    private Properties buildConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getConsumer().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getConsumer().getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getConsumer().getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.getConsumer().isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getConsumer().getMaxPollRecords());
        return props;
    }

    private Properties buildProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProducer().getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, properties.getProducer().getClientId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return props;
    }
}