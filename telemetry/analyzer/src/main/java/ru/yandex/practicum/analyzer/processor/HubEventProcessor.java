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
import ru.yandex.practicum.analyzer.serialization.HubEventDeserializer;
import ru.yandex.practicum.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class HubEventProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);

    private final AnalyzerKafkaProperties kafkaProperties;
    private final HubEventService hubEventService;

    public HubEventProcessor(AnalyzerKafkaProperties kafkaProperties,
                             HubEventService hubEventService) {
        this.kafkaProperties = kafkaProperties;
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        KafkaConsumer<String, HubEventAvro> consumer = new KafkaConsumer<>(buildConsumerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения, останавливаем HubEventProcessor");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(kafkaProperties.getTopics().getHubs()));
            log.info("HubEventProcessor подписался на topic={}", kafkaProperties.getTopics().getHubs());

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofSeconds(1));

                for (var record : records) {
                    HubEventAvro event = record.value();
                    if (event == null) {
                        continue;
                    }

                    hubEventService.handle(event);
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {
            log.info("HubEventProcessor остановлен");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хаба", e);
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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getHubGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getConsumer().getMaxPollRecords());
        return props;
    }
}