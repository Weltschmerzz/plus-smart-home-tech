package ru.yandex.practicum.collector.mapper.proto;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class SensorEventProtoAvroMapper {

    private final ProtoTimestampMapper timestampMapper;

    public SensorEventProtoAvroMapper(ProtoTimestampMapper timestampMapper) {
        this.timestampMapper = timestampMapper;
    }

    public SensorEventAvro toAvro(SensorEventProto event) {
        SensorEventAvro result = new SensorEventAvro();
        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(timestampMapper.toInstant(event.getTimestamp()));
        result.setPayload(mapPayload(event));
        return result;
    }

    private Object mapPayload(SensorEventProto event) {
        return switch (event.getPayloadCase()) {
            case CLIMATE_SENSOR -> mapClimate(event.getClimateSensor());
            case LIGHT_SENSOR -> mapLight(event.getLightSensor());
            case MOTION_SENSOR -> mapMotion(event.getMotionSensor());
            case SWITCH_SENSOR -> mapSwitch(event.getSwitchSensor());
            case TEMPERATURE_SENSOR -> mapTemperature(event);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("У события датчика отсутствует payload");
        };
    }

    private ClimateSensorAvro mapClimate(ClimateSensorProto proto) {
        ClimateSensorAvro avro = new ClimateSensorAvro();
        avro.setTemperatureC(proto.getTemperatureC());
        avro.setHumidity(proto.getHumidity());
        avro.setCo2Level(proto.getCo2Level());
        return avro;
    }

    private LightSensorAvro mapLight(LightSensorProto proto) {
        LightSensorAvro avro = new LightSensorAvro();
        avro.setLinkQuality(proto.getLinkQuality());
        avro.setLuminosity(proto.getLuminosity());
        return avro;
    }

    private MotionSensorAvro mapMotion(MotionSensorProto proto) {
        MotionSensorAvro avro = new MotionSensorAvro();
        avro.setLinkQuality(proto.getLinkQuality());
        avro.setMotion(proto.getMotion());
        avro.setVoltage(proto.getVoltage());
        return avro;
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorProto proto) {
        SwitchSensorAvro avro = new SwitchSensorAvro();
        avro.setState(proto.getState());
        return avro;
    }

    private TemperatureSensorAvro mapTemperature(SensorEventProto event) {
        TemperatureSensorProto proto = event.getTemperatureSensor();

        TemperatureSensorAvro avro = new TemperatureSensorAvro();
        avro.setId(event.getId());
        avro.setHubId(event.getHubId());
        avro.setTimestamp(timestampMapper.toInstant(event.getTimestamp()));
        avro.setTemperatureC(proto.getTemperatureC());
        avro.setTemperatureF(proto.getTemperatureF());
        return avro;
    }
}