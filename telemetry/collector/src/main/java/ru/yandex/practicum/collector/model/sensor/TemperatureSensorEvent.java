package ru.yandex.practicum.collector.model.sensor;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public class TemperatureSensorEvent extends SensorEvent {

    @NotNull
    @JsonAlias("temperature_c")
    private Integer temperatureC;

    @NotNull
    @JsonAlias("temperature_f")
    private Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    public Integer getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Integer temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getTemperatureF() {
        return temperatureF;
    }

    public void setTemperatureF(Integer temperatureF) {
        this.temperatureF = temperatureF;
    }
}