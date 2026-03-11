package ru.yandex.practicum.collector.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.serialization.AvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Component
public class CollectorKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CollectorKafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final AvroSerializer avroSerializer;

    @Value("${collector.kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${collector.kafka.topics.hubs}")
    private String hubsTopic;

    public CollectorKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate,
                                  AvroSerializer avroSerializer) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroSerializer = avroSerializer;
    }

    public void sendSensorEvent(SensorEventAvro event) {
        byte[] payload = avroSerializer.serialize(event);
        String key = event.getHubId().toString();

        kafkaTemplate.send(sensorsTopic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события датчика в Kafka: topic={}, key={}, sensorId={}",
                                sensorsTopic, key, event.getId(), ex);
                    } else {
                        log.info("Событие датчика отправлено в Kafka: topic={}, key={}, sensorId={}, partition={}, offset={}",
                                sensorsTopic,
                                key,
                                event.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void sendHubEvent(HubEventAvro event) {
        byte[] payload = avroSerializer.serialize(event);
        String key = event.getHubId().toString();

        kafkaTemplate.send(hubsTopic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события хаба в Kafka: topic={}, key={}",
                                hubsTopic, key, ex);
                    } else {
                        log.info("Событие хаба отправлено в Kafka: topic={}, key={}, partition={}, offset={}",
                                hubsTopic,
                                key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}