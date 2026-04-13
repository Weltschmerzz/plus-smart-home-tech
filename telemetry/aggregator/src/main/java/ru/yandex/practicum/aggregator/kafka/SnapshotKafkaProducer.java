package ru.yandex.practicum.aggregator.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.aggregator.serialization.AvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Component
public class SnapshotKafkaProducer {

    private final AggregatorKafkaProperties properties;
    private final AvroSerializer avroSerializer;

    public SnapshotKafkaProducer(AggregatorKafkaProperties properties,
                                 AvroSerializer avroSerializer) {
        this.properties = properties;
        this.avroSerializer = avroSerializer;
    }

    public void send(KafkaProducer<String, byte[]> producer, SensorsSnapshotAvro snapshot) {
        byte[] payload = avroSerializer.serialize(snapshot);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                properties.getTopics().getSnapshots(),
                snapshot.getHubId().toString(),
                payload
        );

        try {
            producer.send(record).get();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось отправить снапшот в Kafka", e);
        }
    }
}