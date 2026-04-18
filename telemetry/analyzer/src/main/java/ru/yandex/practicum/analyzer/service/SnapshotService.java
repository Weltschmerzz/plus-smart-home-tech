package ru.yandex.practicum.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.analyzer.model.ConditionOperation;
import ru.yandex.practicum.analyzer.model.ConditionType;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);

    private static final int SCENARIO_RETRY_COUNT = 20;
    private static final long SCENARIO_RETRY_DELAY_MS = 100L;

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
        List<Scenario> scenarios = loadScenariosWithRetry(hubId);

        if (scenarios.isEmpty()) {
            log.debug("Для хаба {} пока не найдено сценариев, пропускаем снапшот", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            if (matches(snapshot, scenario)) {
                executeScenario(snapshot, scenario);
            }
        }
    }

    private List<Scenario> loadScenariosWithRetry(String hubId) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (int attempt = 1; scenarios.isEmpty() && attempt <= SCENARIO_RETRY_COUNT; attempt++) {
            sleepQuietly(SCENARIO_RETRY_DELAY_MS);
            scenarios = scenarioRepository.findByHubId(hubId);
        }

        return scenarios;
    }

    private void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            log.info("Отправляю команду в Hub Router: hubId={}, scenario={}, sensorId={}, actionType={}, value={}",
                    hubId,
                    scenario.getName(),
                    actionLink.getSensor().getId(),
                    actionLink.getAction().getType(),
                    actionLink.getAction().getValue());

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