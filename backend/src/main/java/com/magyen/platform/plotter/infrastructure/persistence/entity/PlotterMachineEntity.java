package com.magyen.platform.plotter.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.plotter.domain.PlotterMachine}.
 */
@Entity
@Table(name = "plotter_machines")
public class PlotterMachineEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "default_for_registration", nullable = false)
    private boolean defaultForRegistration;

    public PlotterMachineEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultForRegistration() {
        return defaultForRegistration;
    }

    public void setDefaultForRegistration(boolean defaultForRegistration) {
        this.defaultForRegistration = defaultForRegistration;
    }
}
