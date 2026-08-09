package com.magyen.platform.production.infrastructure.persistence.mapper;

import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.SizeBreakdown;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionItemEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionItemSizeEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperationEntity;
import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOrderEntity;

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
        productionOrderEntity.setObservations(productionOrder.getObservations());

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
                operations
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
        itemEntity.setGarmentVariant(resolved.getGarmentVariant());
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
                itemEntity.getGarmentVariant(),
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
}
