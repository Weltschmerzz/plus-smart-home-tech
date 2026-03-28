package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "scenarios")
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioCondition> conditions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioAction> actions = new LinkedHashSet<>();

    public Scenario() {
    }

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ScenarioCondition> getConditions() {
        return conditions;
    }

    public void setConditions(Set<ScenarioCondition> conditions) {
        this.conditions = conditions;
    }

    public Set<ScenarioAction> getActions() {
        return actions;
    }

    public void setActions(Set<ScenarioAction> actions) {
        this.actions = actions;
    }

    public void addConditionLink(ScenarioCondition link) {
        conditions.add(link);
        link.setScenario(this);
    }

    public void addActionLink(ScenarioAction link) {
        actions.add(link);
        link.setScenario(this);
    }

    public void clearConditions() {
        conditions.clear();
    }

    public void clearActions() {
        actions.clear();
    }
}