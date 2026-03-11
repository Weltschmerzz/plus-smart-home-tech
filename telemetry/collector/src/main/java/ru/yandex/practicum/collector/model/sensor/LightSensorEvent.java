package ru.yandex.practicum.collector.model.sensor;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public class LightSensorEvent extends SensorEvent {

    @NotNull
    @JsonAlias("link_quality")
    private Integer linkQuality;

    @NotNull
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    public Integer getLinkQuality() {
        return linkQuality;
    }

    public void setLinkQuality(Integer linkQuality) {
        this.linkQuality = linkQuality;
    }

    public Integer getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(Integer luminosity) {
        this.luminosity = luminosity;
    }
}