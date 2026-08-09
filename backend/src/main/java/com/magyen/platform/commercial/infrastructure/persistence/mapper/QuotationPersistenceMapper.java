package com.magyen.platform.commercial.infrastructure.persistence.mapper;

import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.infrastructure.persistence.entity.QuotationEntity;
import com.magyen.platform.commercial.infrastructure.persistence.entity.QuotationItemEntity;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link Quotation} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class QuotationPersistenceMapper {

    public QuotationEntity toEntity(Quotation quotation) {
        Objects.requireNonNull(quotation, "Quotation must not be null");

        QuotationEntity quotationEntity = new QuotationEntity();
        quotationEntity.setId(quotation.getId());
        quotationEntity.setQuotationNumber(toPersistedQuotationNumber(quotation.getQuotationNumber()));
        quotationEntity.setCustomerId(quotation.getCustomerId());
        quotationEntity.setCreationDate(quotation.getCreationDate());
        quotationEntity.setDeliveryDate(quotation.getDeliveryDate());
        quotationEntity.setStatus(quotation.getStatus());
        quotationEntity.setSalesperson(quotation.getSalesperson());
        quotationEntity.setObservations(quotation.getObservations());
        quotationEntity.setTotalAmount(toAmount(quotation.getTotal()));

        List<QuotationItemEntity> itemEntities = new ArrayList<>();
        for (QuotationItem item : quotation.getItems()) {
            QuotationItemEntity itemEntity = toItemEntity(item);
            itemEntity.setQuotation(quotationEntity);
            itemEntities.add(itemEntity);
        }
        quotationEntity.setItems(itemEntities);

        return quotationEntity;
    }

    public Quotation toDomain(QuotationEntity quotationEntity) {
        Objects.requireNonNull(quotationEntity, "Quotation entity must not be null");

        List<QuotationItem> items = new ArrayList<>();
        for (QuotationItemEntity itemEntity : quotationEntity.getItems()) {
            items.add(toItemDomain(itemEntity));
        }

        return Quotation.reconstitute(
                quotationEntity.getId(),
                toDomainQuotationNumber(quotationEntity.getQuotationNumber()),
                quotationEntity.getCustomerId(),
                quotationEntity.getCreationDate(),
                quotationEntity.getDeliveryDate(),
                quotationEntity.getStatus(),
                quotationEntity.getSalesperson(),
                quotationEntity.getObservations(),
                items
        );
    }

    private Long toPersistedQuotationNumber(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return quotationNumber.getValue();
    }

    private QuotationNumber toDomainQuotationNumber(Long quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return QuotationNumber.of(quotationNumber);
    }

    private QuotationItemEntity toItemEntity(QuotationItem item) {
        Objects.requireNonNull(item, "Quotation item must not be null");

        QuotationItemEntity itemEntity = new QuotationItemEntity();
        itemEntity.setId(item.getId());
        itemEntity.setProductName(item.getProductName());
        itemEntity.setQuantity(item.getQuantity());
        itemEntity.setFabric(item.getFabric());
        itemEntity.setColor(item.getColor());
        itemEntity.setUnitPrice(toAmount(item.getUnitPrice()));
        itemEntity.setSubtotal(toAmount(item.getSubtotal()));
        mapProductSpecification(itemEntity, item.getProductSpecification());
        return itemEntity;
    }

    private QuotationItem toItemDomain(QuotationItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Quotation item entity must not be null");

        return QuotationItem.reconstitute(
                itemEntity.getId(),
                itemEntity.getProductName(),
                itemEntity.getQuantity(),
                itemEntity.getFabric(),
                itemEntity.getColor(),
                toMoney(itemEntity.getUnitPrice()),
                toProductSpecification(itemEntity)
        );
    }

    private void mapProductSpecification(
            QuotationItemEntity itemEntity,
            ProductSpecification specification
    ) {
        Objects.requireNonNull(itemEntity, "Quotation item entity must not be null");
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

    private ProductSpecification toProductSpecification(QuotationItemEntity itemEntity) {
        Objects.requireNonNull(itemEntity, "Quotation item entity must not be null");

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

    private BigDecimal toAmount(Money money) {
        Objects.requireNonNull(money, "Money must not be null");
        return money.getAmount();
    }

    private Money toMoney(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        return Money.of(amount);
    }
}
