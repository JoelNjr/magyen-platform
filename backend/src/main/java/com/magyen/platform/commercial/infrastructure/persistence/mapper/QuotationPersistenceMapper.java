package com.magyen.platform.commercial.infrastructure.persistence.mapper;

import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationItem;
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
                quotationEntity.getCustomerId(),
                quotationEntity.getCreationDate(),
                quotationEntity.getDeliveryDate(),
                quotationEntity.getStatus(),
                quotationEntity.getSalesperson(),
                quotationEntity.getObservations(),
                items
        );
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
                toMoney(itemEntity.getUnitPrice())
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
