package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;

    @Column
    private Integer value;

    public Action() {
    }

    public Action(ActionType type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}