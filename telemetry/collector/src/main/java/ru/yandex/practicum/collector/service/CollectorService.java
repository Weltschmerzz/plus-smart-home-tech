package ru.yandex.practicum.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.kafka.CollectorKafkaProducer;
import ru.yandex.practicum.collector.mapper.HubEventAvroMapper;
import ru.yandex.practicum.collector.mapper.SensorEventAvroMapper;
import ru.yandex.practicum.collector.mapper.proto.HubEventProtoAvroMapper;
import ru.yandex.practicum.collector.mapper.proto.SensorEventProtoAvroMapper;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
public class CollectorService {

    private final SensorEventAvroMapper sensorEventAvroMapper;
    private final HubEventAvroMapper hubEventAvroMapper;
    private final SensorEventProtoAvroMapper sensorEventProtoAvroMapper;
    private final HubEventProtoAvroMapper hubEventProtoAvroMapper;
    private final CollectorKafkaProducer collectorKafkaProducer;

    public CollectorService(SensorEventAvroMapper sensorEventAvroMapper,
                            HubEventAvroMapper hubEventAvroMapper,
                            SensorEventProtoAvroMapper sensorEventProtoAvroMapper,
                            HubEventProtoAvroMapper hubEventProtoAvroMapper,
                            CollectorKafkaProducer collectorKafkaProducer) {
        this.sensorEventAvroMapper = sensorEventAvroMapper;
        this.hubEventAvroMapper = hubEventAvroMapper;
        this.sensorEventProtoAvroMapper = sensorEventProtoAvroMapper;
        this.hubEventProtoAvroMapper = hubEventProtoAvroMapper;
        this.collectorKafkaProducer = collectorKafkaProducer;
    }

    public void collectSensorEvent(SensorEvent event) {
        SensorEventAvro avroEvent = sensorEventAvroMapper.toAvro(event);
        collectorKafkaProducer.sendSensorEvent(avroEvent);
    }

    public void collectHubEvent(HubEvent event) {
        HubEventAvro avroEvent = hubEventAvroMapper.toAvro(event);
        collectorKafkaProducer.sendHubEvent(avroEvent);
    }

    public void collectSensorEvent(SensorEventProto event) {
        SensorEventAvro avroEvent = sensorEventProtoAvroMapper.toAvro(event);
        collectorKafkaProducer.sendSensorEvent(avroEvent);
    }

    public void collectHubEvent(HubEventProto event) {
        HubEventAvro avroEvent = hubEventProtoAvroMapper.toAvro(event);
        collectorKafkaProducer.sendHubEvent(avroEvent);
    }
}