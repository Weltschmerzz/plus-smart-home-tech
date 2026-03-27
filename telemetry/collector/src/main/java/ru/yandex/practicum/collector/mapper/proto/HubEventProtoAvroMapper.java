package ru.yandex.practicum.collector.mapper.proto;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
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
public class HubEventProtoAvroMapper {

    private final ProtoTimestampMapper timestampMapper;

    public HubEventProtoAvroMapper(ProtoTimestampMapper timestampMapper) {
        this.timestampMapper = timestampMapper;
    }

    public HubEventAvro toAvro(HubEventProto event) {
        HubEventAvro result = new HubEventAvro();
        result.setHubId(event.getHubId());
        result.setTimestamp(timestampMapper.toInstant(event.getTimestamp()));
        result.setPayload(mapPayload(event));
        return result;
    }

    private Object mapPayload(HubEventProto event) {
        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(event.getDeviceAdded());
            case DEVICE_REMOVED -> mapDeviceRemoved(event.getDeviceRemoved());
            case SCENARIO_ADDED -> mapScenarioAdded(event.getScenarioAdded());
            case SCENARIO_REMOVED -> mapScenarioRemoved(event.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("У события хаба отсутствует payload");
        };
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEventProto proto) {
        DeviceAddedEventAvro avro = new DeviceAddedEventAvro();
        avro.setId(proto.getId());
        avro.setType(DeviceTypeAvro.valueOf(proto.getType().name()));
        return avro;
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(DeviceRemovedEventProto proto) {
        DeviceRemovedEventAvro avro = new DeviceRemovedEventAvro();
        avro.setId(proto.getId());
        return avro;
    }

    private ScenarioAddedEventAvro mapScenarioAdded(ScenarioAddedEventProto proto) {
        ScenarioAddedEventAvro avro = new ScenarioAddedEventAvro();
        avro.setName(proto.getName());
        avro.setConditions(proto.getConditionList().stream()
                .map(this::mapCondition)
                .toList());
        avro.setActions(proto.getActionList().stream()
                .map(this::mapAction)
                .toList());
        return avro;
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(ScenarioRemovedEventProto proto) {
        ScenarioRemovedEventAvro avro = new ScenarioRemovedEventAvro();
        avro.setName(proto.getName());
        return avro;
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto proto) {
        ScenarioConditionAvro avro = new ScenarioConditionAvro();
        avro.setSensorId(proto.getSensorId());
        avro.setType(ConditionTypeAvro.valueOf(proto.getType().name()));
        avro.setOperation(ConditionOperationAvro.valueOf(proto.getOperation().name()));

        switch (proto.getValueCase()) {
            case BOOL_VALUE -> avro.setValue(proto.getBoolValue());
            case INT_VALUE -> avro.setValue(proto.getIntValue());
            case VALUE_NOT_SET -> avro.setValue(null);
        }

        return avro;
    }

    private DeviceActionAvro mapAction(DeviceActionProto proto) {
        DeviceActionAvro avro = new DeviceActionAvro();
        avro.setSensorId(proto.getSensorId());
        avro.setType(ActionTypeAvro.valueOf(proto.getType().name()));
        avro.setValue(proto.hasValue() ? proto.getValue() : null);
        return avro;
    }
}