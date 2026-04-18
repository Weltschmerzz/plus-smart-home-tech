package ru.yandex.practicum.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.analyzer.serialization.SensorsSnapshotDeserializer;
import ru.yandex.practicum.analyzer.service.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class SnapshotProcessor {

    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final AnalyzerKafkaProperties kafkaProperties;
    private final SnapshotService snapshotService;

    public SnapshotProcessor(AnalyzerKafkaProperties kafkaProperties,
                             SnapshotService snapshotService) {
        this.kafkaProperties = kafkaProperties;
        this.snapshotService = snapshotService;
    }

    public void start() {
        KafkaConsumer<String, SensorsSnapshotAvro> consumer = new KafkaConsumer<>(buildConsumerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения, останавливаем SnapshotProcessor");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(kafkaProperties.getTopics().getSnapshots()));
            log.info("SnapshotProcessor подписался на topic={}", kafkaProperties.getTopics().getSnapshots());

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofSeconds(1));

                for (var record : records) {
                    SensorsSnapshotAvro snapshot = record.value();
                    if (snapshot == null) {
                        continue;
                    }

                    try {
                        snapshotService.handle(snapshot);
                    } catch (Exception e) {
                        log.error("Ошибка при обработке снапшота: key={}, topic={}",
                                record.key(), record.topic(), e);
                    }
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {
            log.info("SnapshotProcessor остановлен");
        } catch (Exception e) {
            log.error("Ошибка во время обработки снапшотов", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("Не удалось выполнить финальный commit", e);
            } finally {
                consumer.close();
            }
        }
    }

    private Properties buildConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getConsumer().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getSnapshotGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getConsumer().getMaxPollRecords());
        return props;
    }
}