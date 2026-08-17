package com.magyen.platform.production.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo relacional del snapshot productivo {@link com.magyen.platform.production.domain.ProductionItem}.
 */
@Entity
@Table(name = "production_items")
public class ProductionItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrderEntity productionOrder;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "garment_type", length = 100)
    private String garmentType;

    @Column(name = "collar_type", length = 100)
    private String collarType;

    @Column(name = "sleeve_type", length = 100)
    private String sleeveType;

    @Column(name = "cuff_required")
    private Boolean cuffRequired;

    @Column(name = "sublimation_required", nullable = false)
    private boolean sublimationRequired;

    @Column(name = "embroidery_required", nullable = false)
    private boolean embroideryRequired;

    @Column(name = "dtf_required", nullable = false)
    private boolean dtfRequired;

    @Column(name = "decoration_notes", length = 2000)
    private String decorationNotes;

    @Column(name = "includes_names", nullable = false)
    private boolean includesNames;

    @Column(name = "includes_numbers", nullable = false)
    private boolean includesNumbers;

    @Column(name = "includes_logos", nullable = false)
    private boolean includesLogos;

    @Column(name = "personalization_notes", length = 2000)
    private String personalizationNotes;

    @Column(name = "item_observations", length = 2000)
    private String itemObservations;

    @OneToMany(mappedBy = "productionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionItemSizeEntity> sizeBreakdowns = new ArrayList<>();

    public ProductionItemEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductionOrderEntity getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrderEntity productionOrder) {
        this.productionOrder = productionOrder;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getGarmentType() {
        return garmentType;
    }

    public void setGarmentType(String garmentType) {
        this.garmentType = garmentType;
    }

    public String getCollarType() {
        return collarType;
    }

    public void setCollarType(String collarType) {
        this.collarType = collarType;
    }

    public String getSleeveType() {
        return sleeveType;
    }

    public void setSleeveType(String sleeveType) {
        this.sleeveType = sleeveType;
    }

    public Boolean getCuffRequired() {
        return cuffRequired;
    }

    public void setCuffRequired(Boolean cuffRequired) {
        this.cuffRequired = cuffRequired;
    }

    public boolean isSublimationRequired() {
        return sublimationRequired;
    }

    public void setSublimationRequired(boolean sublimationRequired) {
        this.sublimationRequired = sublimationRequired;
    }

    public boolean isEmbroideryRequired() {
        return embroideryRequired;
    }

    public void setEmbroideryRequired(boolean embroideryRequired) {
        this.embroideryRequired = embroideryRequired;
    }

    public boolean isDtfRequired() {
        return dtfRequired;
    }

    public void setDtfRequired(boolean dtfRequired) {
        this.dtfRequired = dtfRequired;
    }

    public String getDecorationNotes() {
        return decorationNotes;
    }

    public void setDecorationNotes(String decorationNotes) {
        this.decorationNotes = decorationNotes;
    }

    public boolean isIncludesNames() {
        return includesNames;
    }

    public void setIncludesNames(boolean includesNames) {
        this.includesNames = includesNames;
    }

    public boolean isIncludesNumbers() {
        return includesNumbers;
    }

    public void setIncludesNumbers(boolean includesNumbers) {
        this.includesNumbers = includesNumbers;
    }

    public boolean isIncludesLogos() {
        return includesLogos;
    }

    public void setIncludesLogos(boolean includesLogos) {
        this.includesLogos = includesLogos;
    }

    public String getPersonalizationNotes() {
        return personalizationNotes;
    }

    public void setPersonalizationNotes(String personalizationNotes) {
        this.personalizationNotes = personalizationNotes;
    }

    public String getItemObservations() {
        return itemObservations;
    }

    public void setItemObservations(String itemObservations) {
        this.itemObservations = itemObservations;
    }

    public List<ProductionItemSizeEntity> getSizeBreakdowns() {
        return sizeBreakdowns;
    }

    public void setSizeBreakdowns(List<ProductionItemSizeEntity> sizeBreakdowns) {
        this.sizeBreakdowns = sizeBreakdowns;
    }
}
