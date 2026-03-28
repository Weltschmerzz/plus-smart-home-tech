package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {

    @EmbeddedId
    private ScenarioConditionId id = new ScenarioConditionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id", nullable = false)
    private Condition condition;

    public ScenarioCondition() {
    }

    public ScenarioCondition(Sensor sensor, Condition condition) {
        this.sensor = sensor;
        this.condition = condition;
    }

    @PostPersist
    @PostLoad
    @PostUpdate
    private void syncIds() {
        if (scenario != null) {
            id.setScenarioId(scenario.getId());
        }
        if (sensor != null) {
            id.setSensorId(sensor.getId());
        }
        if (condition != null) {
            id.setConditionId(condition.getId());
        }
    }

    public ScenarioConditionId getId() {
        return id;
    }

    public void setId(ScenarioConditionId id) {
        this.id = id;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}