package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.collector.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        SensorEventAvro result = new SensorEventAvro();
        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(event.getTimestamp());
        result.setPayload(mapPayload(event));
        return result;
    }

    private Object mapPayload(SensorEvent event) {
        if (event instanceof ClimateSensorEvent climateEvent) {
            ClimateSensorAvro payload = new ClimateSensorAvro();
            payload.setTemperatureC(climateEvent.getTemperatureC());
            payload.setHumidity(climateEvent.getHumidity());
            payload.setCo2Level(climateEvent.getCo2Level());
            return payload;
        }

        if (event instanceof LightSensorEvent lightEvent) {
            LightSensorAvro payload = new LightSensorAvro();
            payload.setLinkQuality(lightEvent.getLinkQuality());
            payload.setLuminosity(lightEvent.getLuminosity());
            return payload;
        }

        if (event instanceof MotionSensorEvent motionEvent) {
            MotionSensorAvro payload = new MotionSensorAvro();
            payload.setLinkQuality(motionEvent.getLinkQuality());
            payload.setMotion(motionEvent.getMotion());
            payload.setVoltage(motionEvent.getVoltage());
            return payload;
        }

        if (event instanceof SwitchSensorEvent switchEvent) {
            SwitchSensorAvro payload = new SwitchSensorAvro();
            payload.setState(switchEvent.getState());
            return payload;
        }

        if (event instanceof TemperatureSensorEvent temperatureEvent) {
            TemperatureSensorAvro payload = new TemperatureSensorAvro();
            payload.setId(temperatureEvent.getId());
            payload.setHubId(temperatureEvent.getHubId());
            payload.setTimestamp(temperatureEvent.getTimestamp());
            payload.setTemperatureC(temperatureEvent.getTemperatureC());
            payload.setTemperatureF(temperatureEvent.getTemperatureF());
            return payload;
        }

        throw new IllegalArgumentException(
                "Неизвестный тип события датчика: " + event.getClass().getSimpleName()
        );
    }
}