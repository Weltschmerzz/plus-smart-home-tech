package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.ActionType;
import ru.yandex.practicum.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.collector.model.hub.ConditionType;
import ru.yandex.practicum.collector.model.hub.DeviceAction;
import ru.yandex.practicum.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.collector.model.hub.DeviceType;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.collector.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
public class HubEventAvroMapper {

    public HubEventAvro toAvro(HubEvent event) {
        HubEventAvro result = new HubEventAvro();
        result.setHubId(event.getHubId());
        result.setTimestamp(event.getTimestamp());
        result.setPayload(mapPayload(event));
        return result;
    }

    private Object mapPayload(HubEvent event) {
        if (event instanceof DeviceAddedEvent deviceAddedEvent) {
            DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
            payload.setId(deviceAddedEvent.getId());
            payload.setType(mapDeviceType(deviceAddedEvent.getDeviceType()));
            return payload;
        }

        if (event instanceof DeviceRemovedEvent deviceRemovedEvent) {
            DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
            payload.setId(deviceRemovedEvent.getId());
            return payload;
        }

        if (event instanceof ScenarioAddedEvent scenarioAddedEvent) {
            ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
            payload.setName(scenarioAddedEvent.getName());
            payload.setConditions(
                    scenarioAddedEvent.getConditions()
                            .stream()
                            .map(this::mapScenarioCondition)
                            .toList()
            );
            payload.setActions(
                    scenarioAddedEvent.getActions()
                            .stream()
                            .map(this::mapDeviceAction)
                            .toList()
            );
            return payload;
        }

        if (event instanceof ScenarioRemovedEvent scenarioRemovedEvent) {
            ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
            payload.setName(scenarioRemovedEvent.getName());
            return payload;
        }

        throw new IllegalArgumentException(
                "Неизвестный тип события хаба: " + event.getClass().getSimpleName()
        );
    }

    private ScenarioConditionAvro mapScenarioCondition(ScenarioCondition condition) {
        ScenarioConditionAvro result = new ScenarioConditionAvro();
        result.setSensorId(condition.getSensorId());
        result.setType(mapConditionType(condition.getType()));
        result.setOperation(mapConditionOperation(condition.getOperation()));
        result.setValue(mapConditionValue(condition.getValue()));
        return result;
    }

    private DeviceActionAvro mapDeviceAction(DeviceAction action) {
        DeviceActionAvro result = new DeviceActionAvro();
        result.setSensorId(action.getSensorId());
        result.setType(mapActionType(action.getType()));
        result.setValue(action.getValue());
        return result;
    }

    private Object mapConditionValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return value;
        }
        if (value instanceof Boolean) {
            return value;
        }

        throw new IllegalArgumentException(
                "Недопустимое значение условия сценария. Ожидалось null, Integer или Boolean, а пришло: "
                        + value.getClass().getSimpleName()
        );
    }

    private DeviceTypeAvro mapDeviceType(DeviceType type) {
        return DeviceTypeAvro.valueOf(type.name());
    }

    private ConditionTypeAvro mapConditionType(ConditionType type) {
        return ConditionTypeAvro.valueOf(type.name());
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperation operation) {
        return ConditionOperationAvro.valueOf(operation.name());
    }

    private ActionTypeAvro mapActionType(ActionType type) {
        return ActionTypeAvro.valueOf(type.name());
    }
}