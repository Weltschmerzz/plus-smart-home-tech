package ru.yandex.practicum.collector.model.hub;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceAddedEvent extends HubEvent {

    @NotBlank
    private String id;

    @NotNull
    @JsonAlias("device_type")
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}