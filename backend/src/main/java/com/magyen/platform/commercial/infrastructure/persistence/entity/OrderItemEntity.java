package com.magyen.platform.commercial.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.commercial.domain.OrderItem}.
 */
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "fabric", nullable = false, length = 255)
    private String fabric;

    @Column(name = "color", nullable = false, length = 100)
    private String color;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

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

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemSizeEntity> sizeBreakdowns = new ArrayList<>();

    public OrderItemEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
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

    public String getFabric() {
        return fabric;
    }

    public void setFabric(String fabric) {
        this.fabric = fabric;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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

    public List<OrderItemSizeEntity> getSizeBreakdowns() {
        return sizeBreakdowns;
    }

    public void setSizeBreakdowns(List<OrderItemSizeEntity> sizeBreakdowns) {
        this.sizeBreakdowns = sizeBreakdowns;
    }
}
