package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {

    @EmbeddedId
    private ScenarioActionId id = new ScenarioActionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("actionId")
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    public ScenarioAction() {
    }

    public ScenarioAction(Sensor sensor, Action action) {
        this.sensor = sensor;
        this.action = action;
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
        if (action != null) {
            id.setActionId(action.getId());
        }
    }

    public ScenarioActionId getId() {
        return id;
    }

    public void setId(ScenarioActionId id) {
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}