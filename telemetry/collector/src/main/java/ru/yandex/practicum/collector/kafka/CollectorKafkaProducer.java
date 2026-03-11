package ru.yandex.practicum.collector.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.serialization.AvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.concurrent.ExecutionException;

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

        try {
            var result = kafkaTemplate.send(sensorsTopic, key, payload).get();

            log.info("Событие датчика отправлено в Kafka: topic={}, key={}, sensorId={}, partition={}, offset={}",
                    sensorsTopic,
                    key,
                    event.getId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Поток был прерван при отправке события датчика в Kafka", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Не удалось отправить событие датчика в Kafka", e);
        }
    }

    public void sendHubEvent(HubEventAvro event) {
        byte[] payload = avroSerializer.serialize(event);
        String key = event.getHubId().toString();

        try {
            var result = kafkaTemplate.send(hubsTopic, key, payload).get();

            log.info("Событие хаба отправлено в Kafka: topic={}, key={}, partition={}, offset={}",
                    hubsTopic,
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Поток был прерван при отправке события хаба в Kafka", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Не удалось отправить событие хаба в Kafka", e);
        }
    }
}