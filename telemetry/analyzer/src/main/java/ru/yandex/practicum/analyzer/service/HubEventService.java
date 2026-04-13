package ru.yandex.practicum.analyzer.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    public HubEventService(SensorRepository sensorRepository,
                           ScenarioRepository scenarioRepository,
                           ConditionRepository conditionRepository,
                           ActionRepository actionRepository,
                           ScenarioConditionRepository scenarioConditionRepository,
                           ScenarioActionRepository scenarioActionRepository) {
        this.sensorRepository = sensorRepository;
        this.scenarioRepository = scenarioRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.scenarioConditionRepository = scenarioConditionRepository;
        this.scenarioActionRepository = scenarioActionRepository;
    }

    @Transactional
    public void handle(HubEventAvro event) {
        String hubId = event.getHubId().toString();
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro deviceAdded) {
            handleDeviceAdded(hubId, deviceAdded);
            return;
        }

        if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
            handleDeviceRemoved(hubId, deviceRemoved);
            return;
        }

        if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
            handleScenarioAdded(hubId, scenarioAdded);
            return;
        }

        if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
            handleScenarioRemoved(hubId, scenarioRemoved);
            return;
        }

        throw new IllegalArgumentException("Неизвестный тип payload в HubEventAvro: " + payload);
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro payload) {
        String sensorId = payload.getId().toString();

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseGet(() -> new Sensor(sensorId, hubId));

        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro payload) {
        String sensorId = payload.getId().toString();

        Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElse(null);

        if (sensor == null) {
            return;
        }

        Set<Long> scenarioIds = new HashSet<>();

        for (ScenarioCondition link : scenarioConditionRepository.findBySensor_Id(sensorId)) {
            if (link.getScenario() != null && link.getScenario().getId() != null) {
                scenarioIds.add(link.getScenario().getId());
            }
        }

        for (ScenarioAction link : scenarioActionRepository.findBySensor_Id(sensorId)) {
            if (link.getScenario() != null && link.getScenario().getId() != null) {
                scenarioIds.add(link.getScenario().getId());
            }
        }

        if (!scenarioIds.isEmpty()) {
            scenarioRepository.deleteAllById(scenarioIds);
            scenarioRepository.flush();
        }

        sensorRepository.delete(sensor);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro payload) {
        String scenarioName = payload.getName().toString();

        scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .ifPresent(existing -> {
                    scenarioRepository.delete(existing);
                    scenarioRepository.flush();
                });

        Scenario scenario = scenarioRepository.saveAndFlush(new Scenario(hubId, scenarioName));

        for (ScenarioConditionAvro conditionAvro : payload.getConditions()) {
            String sensorId = conditionAvro.getSensorId().toString();
            Sensor sensor = requireSensor(sensorId, hubId);

            Condition condition = conditionRepository.save(
                    new Condition(
                            mapConditionType(conditionAvro.getType()),
                            mapConditionOperation(conditionAvro.getOperation()),
                            extractConditionValue(conditionAvro.getValue())
                    )
            );

            ScenarioCondition link = new ScenarioCondition(sensor, condition);
            scenario.addConditionLink(link);
        }

        for (DeviceActionAvro actionAvro : payload.getActions()) {
            String sensorId = actionAvro.getSensorId().toString();
            Sensor sensor = requireSensor(sensorId, hubId);

            Action action = actionRepository.save(
                    new Action(
                            mapActionType(actionAvro.getType()),
                            extractActionValue(actionAvro.getValue())
                    )
            );

            ScenarioAction link = new ScenarioAction(sensor, action);
            scenario.addActionLink(link);
        }

        scenarioRepository.saveAndFlush(scenario);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro payload) {
        String scenarioName = payload.getName().toString();

        scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .ifPresent(scenarioRepository::delete);
    }

    private Sensor requireSensor(String sensorId, String hubId) {
        return sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElseThrow(() -> new IllegalStateException(
                        "Датчик " + sensorId + " не найден в хабе " + hubId
                ));
    }

    private ConditionType mapConditionType(ConditionTypeAvro type) {
        return ConditionType.valueOf(type.name());
    }

    private ConditionOperation mapConditionOperation(ConditionOperationAvro operation) {
        return ConditionOperation.valueOf(operation.name());
    }

    private ActionType mapActionType(ActionTypeAvro type) {
        return ActionType.valueOf(type.name());
    }

    private Integer extractConditionValue(Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue ? 1 : 0;
        }

        if (value instanceof Integer intValue) {
            return intValue;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        throw new IllegalArgumentException("Неподдерживаемый тип значения условия: " + value);
    }

    private Integer extractActionValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof Integer intValue) {
            return intValue;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        throw new IllegalArgumentException("Неподдерживаемый тип значения действия: " + value);
    }
}