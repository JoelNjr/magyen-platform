package com.magyen.platform.production.infrastructure.persistence.mapper;

import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionAdditionalCost;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionReferenceImage;
import com.magyen.platform.production.domain.SizeBreakdown;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionItemEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionItemSizeEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionAdditionalCostEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionLaborWorkEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionMaterialConsumptionEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperationEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOrderEntity;

import com.magyen.platform.shared.domain.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link ProductionOrder} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class ProductionPersistenceMapper {

    public ProductionOrderEntity toEntity(ProductionOrder productionOrder) {
        Objects.requireNonNull(productionOrder, "Production order must not be null");

        ProductionOrderEntity productionOrderEntity = new ProductionOrderEntity();
        productionOrderEntity.setId(productionOrder.getId());
        productionOrderEntity.setOrderId(productionOrder.getOrderId());
        productionOrderEntity.setCreationDate(productionOrder.getCreationDate());
        productionOrderEntity.setStatus(productionOrder.getStatus());
        productionOrderEntity.setPriority(productionOrder.getPriority());
        productionOrderEntity.setPlannedStartDate(productionOrder.getPlannedStartDate());
        productionOrderEntity.setPlannedEndDate(productionOrder.getPlannedEndDate());
        productionOrderEntity.setActualStartDate(productionOrder.getActualStartDate());
        productionOrderEntity.setActualCompletionDate(productionOrder.getActualCompletionDate());
        productionOrderEntity.setObservations(productionOrder.getObservations());
        if (productionOrder.getReferenceImage() == null) {
            productionOrderEntity.setReferenceImageObjectKey(null);
            productionOrderEntity.setReferenceImageContentType(null);
        } else {
            productionOrderEntity.setReferenceImageObjectKey(productionOrder.getReferenceImage().getObjectKey());
            productionOrderEntity.setReferenceImageContentType(productionOrder.getReferenceImage().getContentType());
        }

        List<ProductionItemEntity> itemEntities = new ArrayList<>();
        for (ProductionItem item : productionOrder.getItems()) {
            ProductionItemEntity itemEntity = toItemEntity(item);
            itemEntity.setProductionOrder(productionOrderEntity);
            itemEntities.add(itemEntity);
        }
        productionOrderEntity.setItems(itemEntities);

        List<ProductionOperationEntity> operationEntities = new ArrayList<>();
        for (ProductionOperation operation : productionOrder.getOperations()) {
            ProductionOperationEntity operationEntity = toOperationEntity(operation);
            operationEntity.setProductionOrder(productionOrderEntity);
            operationEntities.add(operationEntity);
        }
        productionOrderEntity.setOperations(operationEntities);

        List<ProductionMaterialConsumptionEntity> consumptionEntities = new ArrayList<>();
        for (ProductionMaterialConsumption consumption : productionOrder.getMaterialConsumptions()) {
            ProductionMaterialConsumptionEntity consumptionEntity = toConsumptionEntity(consumption);
            consumptionEntity.setProductionOrder(productionOrderEntity);
            consumptionEntities.add(consumptionEntity);
        }
        productionOrderEntity.setMaterialConsumptions(consumptionEntities);

        List<ProductionLaborWorkEntity> laborWorkEntities = new ArrayList<>();
        for (ProductionLaborWork laborWork : productionOrder.getLaborWorks()) {
            ProductionLaborWorkEntity laborWorkEntity = toLaborWorkEntity(laborWork);
            laborWorkEntity.setProductionOrder(productionOrderEntity);
            laborWorkEntities.add(laborWorkEntity);
        }
        productionOrderEntity.setLaborWorks(laborWorkEntities);

        List<ProductionAdditionalCostEntity> additionalCostEntities = new ArrayList<>();
        for (ProductionAdditionalCost additionalCost : productionOrder.getAdditionalCosts()) {
            ProductionAdditionalCostEntity additionalCostEntity = toAdditionalCostEntity(additionalCost);
            additionalCostEntity.setProductionOrder(productionOrderEntity);
            additionalCostEntities.add(additionalCostEntity);
        }
        productionOrderEntity.setAdditionalCosts(additionalCostEntities);

        return productionOrderEntity;
    }

    public ProductionOrder toDomain(ProductionOrderEntity productionOrderEntity) {
        Objects.requireNonNull(productionOrderEntity, "Production order entity must not be null");

        List<ProductionItem> items = new ArrayList<>();
        for (ProductionItemEntity itemEntity : productionOrderEntity.getItems()) {
            items.add(toItemDomain(itemEntity));
        }

        List<ProductionOperation> operations = new ArrayList<>();
        for (ProductionOperationEntity operationEntity : productionOrderEntity.getOperations()) {
            operations.add(toOperationDomain(operationEntity));
        }

        List<ProductionMaterialConsumption> materialConsumptions = new ArrayList<>();
        List<ProductionMaterialConsumptionEntity> consumptionEntities =
                productionOrderEntity.getMaterialConsumptions() == null
                        ? List.of()
                        : productionOrderEntity.getMaterialConsumptions();
        for (ProductionMaterialConsumptionEntity consumptionEntity : consumptionEntities) {
            materialConsumptions.add(toConsumptionDomain(consumptionEntity));
        }

        List<ProductionLaborWork> laborWorks = new ArrayList<>();
        List<ProductionLaborWorkEntity> laborWorkEntities =
                productionOrderEntity.getLaborWorks() == null
                        ? List.of()
                        : productionOrderEntity.getLaborWorks();
        for (ProductionLaborWorkEntity laborWorkEntity : laborWorkEntities) {
            laborWorks.add(toLaborWorkDomain(laborWorkEntity));
        }

        List<ProductionAdditionalCost> additionalCosts = new ArrayList<>();
        List<ProductionAdditionalCostEntity> additionalCostEntities =
                productionOrderEntity.getAdditionalCosts() == null
                        ? List.of()
                        : productionOrderEntity.getAdditionalCosts();
        for (ProductionAdditionalCostEntity additionalCostEntity : additionalCostEntities) {
            additionalCosts.add(toAdditionalCostDomain(additionalCostEntity));
        }

        return ProductionOrder.reconstitute(
                productionOrderEntity.getId(),
                productionOrderEntity.getOrderId(),
                productionOrderEntity.getCreationDate(),
                productionOrderEntity.getStatus(),
                productionOrderEntity.getPriority(),
                productionOrderEntity.getPlannedStartDate(),
                productionOrderEntity.getPlannedEndDate(),
                productionOrderEntity.getObservations(),
                items,
                operations,
                materialConsumptions,
                laborWorks,
                productionOrderEntity.getActualStartDate(),
                productionOrderEntity.getActualCompletionDate(),
                toReferenceImage(productionOrderEntity),
                additionalCosts
        );
    }

    private ProductionReferenceImage toReferenceImage(ProductionOrderEntity productionOrderEntity) {
        if (productionOrderEntity.getReferenceImageObjectKey() == null
                || productionOrderEntity.getReferenceImageObjectKey().isBlank()) {
            return null;
        }
        return ProductionReferenceImage.of(
                productionOrderEntity.getReferenceImageObjectKey(),
                productionOrderEntity.getReferenceImageContentType()
        );
    }

    private ProductionItemEntity toItemEntity(ProductionItem item) {
        Objects.requireNonNull(item, "Production item must not be null");

        ProductionItemEntity itemEntity = new ProductionItemEntity();
        itemEntity.setId(item.getId());
        itemEntity.setProductName(item.getProductName());
        itemEntity.setQuantity(item.getQuantity());
        mapProductSpecification(itemEntity, item.getProductSpecification());

        List<ProductionItemSizeEntity> sizeEntities = new ArrayList<>();
        for (SizeBreakdown sizeBreakdown : item.getSizeBreakdowns()) {
            ProductionItemSizeEntity sizeEntity = toSizeEntity(sizeBreakdown);
            sizeEntity.setProductionItem(itemEntity);
            sizeEntities.add(sizeEntity);
        }
        itemEntity.setSizeBreakdowns(sizeEntities);

        return itemEntity;
    }

    private ProductionItem toItemDomain(ProductionItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Production item entity must not be null");

        List<SizeBreakdown> sizeBreakdowns = new ArrayList<>();
        for (ProductionItemSizeEntity sizeEntity : itemEntity.getSizeBreakdowns()) {
            sizeBreakdowns.add(toSizeDomain(sizeEntity));
        }

        return ProductionItem.reconstitute(
                itemEntity.getId(),
                itemEntity.getProductName(),
                itemEntity.getQuantity(),
                toProductSpecification(itemEntity),
                sizeBreakdowns
        );
    }

    private ProductionItemSizeEntity toSizeEntity(SizeBreakdown sizeBreakdown) {
        Objects.requireNonNull(sizeBreakdown, "Size breakdown must not be null");

        ProductionItemSizeEntity sizeEntity = new ProductionItemSizeEntity();
        sizeEntity.setId(sizeBreakdown.getId());
        sizeEntity.setSize(sizeBreakdown.getSize());
        sizeEntity.setQuantity(sizeBreakdown.getQuantity());
        return sizeEntity;
    }

    private SizeBreakdown toSizeDomain(ProductionItemSizeEntity sizeEntity) {
        Objects.requireNonNull(sizeEntity, "Production item size entity must not be null");

        return SizeBreakdown.reconstitute(
                sizeEntity.getId(),
                sizeEntity.getSize(),
                sizeEntity.getQuantity()
        );
    }

    private void mapProductSpecification(ProductionItemEntity itemEntity, ProductSpecification specification) {
        Objects.requireNonNull(itemEntity, "Production item entity must not be null");
        ProductSpecification resolved = specification == null ? ProductSpecification.empty() : specification;

        itemEntity.setGarmentType(resolved.getGarmentType());
        itemEntity.setCollarType(resolved.getCollarType());
        itemEntity.setSleeveType(resolved.getSleeveType());
        itemEntity.setCuffRequired(resolved.getCuffRequired());
        itemEntity.setSublimationRequired(resolved.isSublimationRequired());
        itemEntity.setEmbroideryRequired(resolved.isEmbroideryRequired());
        itemEntity.setDtfRequired(resolved.isDtfRequired());
        itemEntity.setDecorationNotes(resolved.getDecorationNotes());
        itemEntity.setIncludesNames(resolved.isIncludesNames());
        itemEntity.setIncludesNumbers(resolved.isIncludesNumbers());
        itemEntity.setIncludesLogos(resolved.isIncludesLogos());
        itemEntity.setPersonalizationNotes(resolved.getPersonalizationNotes());
        itemEntity.setItemObservations(resolved.getItemObservations());
    }

    private ProductSpecification toProductSpecification(ProductionItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Production item entity must not be null");

        return ProductSpecification.of(
                itemEntity.getGarmentType(),
                itemEntity.getCollarType(),
                itemEntity.getSleeveType(),
                itemEntity.getCuffRequired(),
                itemEntity.isSublimationRequired(),
                itemEntity.isEmbroideryRequired(),
                itemEntity.isDtfRequired(),
                itemEntity.getDecorationNotes(),
                itemEntity.isIncludesNames(),
                itemEntity.isIncludesNumbers(),
                itemEntity.isIncludesLogos(),
                itemEntity.getPersonalizationNotes(),
                itemEntity.getItemObservations()
        );
    }

    private ProductionOperationEntity toOperationEntity(ProductionOperation operation) {
        Objects.requireNonNull(operation, "Production operation must not be null");

        ProductionOperationEntity operationEntity = new ProductionOperationEntity();
        operationEntity.setId(operation.getId());
        operationEntity.setType(operation.getType());
        operationEntity.setStatus(operation.getStatus());
        operationEntity.setAssignedOperator(operation.getAssignedOperator());
        operationEntity.setPlannedStartDate(operation.getPlannedStartDate());
        operationEntity.setPlannedEndDate(operation.getPlannedEndDate());
        operationEntity.setActualStartDate(operation.getActualStartDate());
        operationEntity.setActualEndDate(operation.getActualEndDate());
        operationEntity.setObservations(operation.getObservations());
        return operationEntity;
    }

    private ProductionOperation toOperationDomain(ProductionOperationEntity operationEntity) {
        Objects.requireNonNull(operationEntity, "Production operation entity must not be null");

        return ProductionOperation.reconstitute(
                operationEntity.getId(),
                operationEntity.getType(),
                operationEntity.getStatus(),
                operationEntity.getAssignedOperator(),
                operationEntity.getPlannedStartDate(),
                operationEntity.getPlannedEndDate(),
                operationEntity.getActualStartDate(),
                operationEntity.getActualEndDate(),
                operationEntity.getObservations()
        );
    }

    private ProductionMaterialConsumptionEntity toConsumptionEntity(ProductionMaterialConsumption consumption) {
        Objects.requireNonNull(consumption, "Material consumption must not be null");

        ProductionMaterialConsumptionEntity consumptionEntity = new ProductionMaterialConsumptionEntity();
        consumptionEntity.setId(consumption.getId());
        consumptionEntity.setInventoryItemId(consumption.getInventoryItemId());
        consumptionEntity.setQuantity(consumption.getQuantity());
        consumptionEntity.setUnitOfMeasure(consumption.getUnitOfMeasure());
        consumptionEntity.setConsumptionDate(consumption.getConsumptionDate());
        consumptionEntity.setObservation(consumption.getObservation());
        return consumptionEntity;
    }

    private ProductionMaterialConsumption toConsumptionDomain(
            ProductionMaterialConsumptionEntity consumptionEntity
    ) {
        Objects.requireNonNull(consumptionEntity, "Material consumption entity must not be null");
        Objects.requireNonNull(
                consumptionEntity.getProductionOrder(),
                "Material consumption must reference a production order"
        );

        return ProductionMaterialConsumption.reconstitute(
                consumptionEntity.getId(),
                consumptionEntity.getProductionOrder().getId(),
                consumptionEntity.getInventoryItemId(),
                consumptionEntity.getQuantity(),
                consumptionEntity.getUnitOfMeasure(),
                consumptionEntity.getConsumptionDate(),
                consumptionEntity.getObservation()
        );
    }

    private ProductionLaborWorkEntity toLaborWorkEntity(ProductionLaborWork laborWork) {
        Objects.requireNonNull(laborWork, "Labor work must not be null");

        ProductionLaborWorkEntity laborWorkEntity = new ProductionLaborWorkEntity();
        laborWorkEntity.setId(laborWork.getId());
        laborWorkEntity.setOperatorEmployeeId(laborWork.getOperatorEmployeeId());
        laborWorkEntity.setWorkDate(laborWork.getWorkDate());
        laborWorkEntity.setOperation(laborWork.getOperation());
        laborWorkEntity.setQuantity(laborWork.getQuantity());
        laborWorkEntity.setUnitOfMeasure(laborWork.getUnitOfMeasure());
        laborWorkEntity.setUnitRate(laborWork.getUnitRate());
        laborWorkEntity.setCalculatedAmount(laborWork.getCalculatedAmount());
        laborWorkEntity.setStatus(laborWork.getStatus());
        laborWorkEntity.setObservation(laborWork.getObservation());
        laborWorkEntity.setPaidAt(laborWork.getPaidAt());
        laborWorkEntity.setFinancialTransactionId(laborWork.getFinancialTransactionId());
        return laborWorkEntity;
    }

    private ProductionLaborWork toLaborWorkDomain(ProductionLaborWorkEntity laborWorkEntity) {
        Objects.requireNonNull(laborWorkEntity, "Labor work entity must not be null");
        Objects.requireNonNull(
                laborWorkEntity.getProductionOrder(),
                "Labor work must reference a production order"
        );

        return ProductionLaborWork.reconstitute(
                laborWorkEntity.getId(),
                laborWorkEntity.getProductionOrder().getId(),
                laborWorkEntity.getOperatorEmployeeId(),
                laborWorkEntity.getWorkDate(),
                laborWorkEntity.getOperation(),
                laborWorkEntity.getQuantity(),
                laborWorkEntity.getUnitOfMeasure(),
                laborWorkEntity.getUnitRate(),
                laborWorkEntity.getCalculatedAmount(),
                laborWorkEntity.getObservation(),
                laborWorkEntity.getStatus(),
                laborWorkEntity.getPaidAt(),
                laborWorkEntity.getFinancialTransactionId()
        );
    }

    private ProductionAdditionalCostEntity toAdditionalCostEntity(ProductionAdditionalCost additionalCost) {
        Objects.requireNonNull(additionalCost, "Additional cost must not be null");

        ProductionAdditionalCostEntity entity = new ProductionAdditionalCostEntity();
        entity.setId(additionalCost.getId());
        entity.setCategory(additionalCost.getCategory());
        entity.setDescription(additionalCost.getDescription());
        entity.setAmount(additionalCost.getAmount().getAmount());
        entity.setIncurredDate(additionalCost.getIncurredDate());
        entity.setFinancialTransactionId(additionalCost.getFinancialTransactionId());
        return entity;
    }

    private ProductionAdditionalCost toAdditionalCostDomain(ProductionAdditionalCostEntity entity) {
        Objects.requireNonNull(entity, "Additional cost entity must not be null");
        Objects.requireNonNull(
                entity.getProductionOrder(),
                "Additional cost must reference a production order"
        );

        return ProductionAdditionalCost.reconstitute(
                entity.getId(),
                entity.getProductionOrder().getId(),
                entity.getCategory(),
                entity.getDescription(),
                Money.of(entity.getAmount()),
                entity.getIncurredDate(),
                entity.getFinancialTransactionId()
        );
    }
}
