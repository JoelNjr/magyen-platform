package com.magyen.platform.administration.infrastructure.persistence.entity;

import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.administration.domain.AdministrationCatalogEntry}.
 */
@Entity
@Table(name = "administration_catalog_entries")
public class AdministrationCatalogEntryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_kind", nullable = false, length = 30)
    private AdministrationCatalogKind catalogKind;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    public AdministrationCatalogEntryEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AdministrationCatalogKind getCatalogKind() {
        return catalogKind;
    }

    public void setCatalogKind(AdministrationCatalogKind catalogKind) {
        this.catalogKind = catalogKind;
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
}
