package ru.yandex.practicum.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SnapshotService {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    public SnapshotService(ScenarioRepository scenarioRepository,
                           HubRouterClient hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    @Transactional(readOnly = true)
    public void handle(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId().toString();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            return;
        }

        for (Scenario scenario : scenarios) {
            if (matches(snapshot, scenario)) {
                executeScenario(snapshot, scenario);
            }
        }
    }

    private boolean matches(SensorsSnapshotAvro snapshot, Scenario scenario) {
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        for (ScenarioCondition link : scenario.getConditions()) {
            String sensorId = link.getSensor().getId();
            SensorStateAvro sensorState = states.get(sensorId);

            if (sensorState == null) {
                return false;
            }

            Integer actualValue = extractValue(sensorState, link.getCondition().getType());
            Integer expectedValue = link.getCondition().getValue();

            if (actualValue == null || expectedValue == null) {
                return false;
            }

            if (!compare(actualValue, expectedValue, link.getCondition().getOperation())) {
                return false;
            }
        }

        return true;
    }

    private void executeScenario(SensorsSnapshotAvro snapshot, Scenario scenario) {
        String hubId = snapshot.getHubId().toString();
        Instant timestamp = snapshot.getTimestamp();

        for (ScenarioAction actionLink : scenario.getActions()) {
            hubRouterClient.sendAction(hubId, scenario.getName(), actionLink, timestamp);
        }
    }

    private boolean compare(Integer actualValue, Integer expectedValue, ConditionOperation operation) {
        return switch (operation) {
            case EQUALS -> actualValue.equals(expectedValue);
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    private Integer extractValue(SensorStateAvro state, ConditionType conditionType) {
        Object data = state.getData();

        return switch (conditionType) {
            case MOTION -> extractMotion(data);
            case LUMINOSITY -> extractLuminosity(data);
            case SWITCH -> extractSwitch(data);
            case TEMPERATURE -> extractTemperature(data);
            case CO2LEVEL -> extractCo2(data);
            case HUMIDITY -> extractHumidity(data);
        };
    }

    private Integer extractMotion(Object data) {
        if (data instanceof MotionSensorAvro motion) {
            return motion.getMotion() ? 1 : 0;
        }
        return null;
    }

    private Integer extractLuminosity(Object data) {
        if (data instanceof LightSensorAvro light) {
            return light.getLuminosity();
        }
        return null;
    }

    private Integer extractSwitch(Object data) {
        if (data instanceof SwitchSensorAvro sw) {
            return sw.getState() ? 1 : 0;
        }
        return null;
    }

    private Integer extractTemperature(Object data) {
        if (data instanceof ClimateSensorAvro climate) {
            return climate.getTemperatureC();
        }
        if (data instanceof TemperatureSensorAvro temperature) {
            return temperature.getTemperatureC();
        }
        return null;
    }

    private Integer extractCo2(Object data) {
        if (data instanceof ClimateSensorAvro climate) {
            return climate.getCo2Level();
        }
        return null;
    }

    private Integer extractHumidity(Object data) {
        if (data instanceof ClimateSensorAvro climate) {
            return climate.getHumidity();
        }
        return null;
    }
}