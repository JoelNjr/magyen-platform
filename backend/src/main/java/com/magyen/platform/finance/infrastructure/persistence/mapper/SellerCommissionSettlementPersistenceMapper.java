package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.SellerCommissionSettlement;
import com.magyen.platform.finance.infrastructure.persistence.entity.SellerCommissionSettlementEntity;

import java.util.Objects;

/**
 * Convierte entre la liquidación de comisión y su modelo JPA.
 */
public class SellerCommissionSettlementPersistenceMapper {

    public SellerCommissionSettlementEntity toEntity(SellerCommissionSettlement settlement) {
        Objects.requireNonNull(settlement, "Seller commission settlement must not be null");

        SellerCommissionSettlementEntity entity = new SellerCommissionSettlementEntity();
        entity.setId(settlement.getId());
        entity.setEmployeeId(settlement.getEmployeeId());
        entity.setPeriodStart(settlement.getPeriodStart());
        entity.setPeriodEnd(settlement.getPeriodEnd());
        entity.setSalesSnapshot(settlement.getSalesSnapshot().getValue());
        entity.setOrderCountSnapshot(settlement.getOrderCountSnapshot());
        entity.setCommissionSnapshot(settlement.getCommissionSnapshot().getValue());
        entity.setStatus(settlement.getStatus());
        entity.setActualPaymentDate(settlement.getActualPaymentDate());
        entity.setPaidAt(settlement.getPaidAt());
        entity.setFinancialTransactionId(settlement.getFinancialTransactionId());
        entity.setObservation(settlement.getObservation());
        return entity;
    }

    public SellerCommissionSettlement toDomain(SellerCommissionSettlementEntity entity) {
        Objects.requireNonNull(entity, "Seller commission settlement entity must not be null");
        return SellerCommissionSettlement.reconstitute(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                FinancialAmount.of(entity.getSalesSnapshot()),
                entity.getOrderCountSnapshot(),
                FinancialAmount.of(entity.getCommissionSnapshot()),
                entity.getStatus(),
                entity.getActualPaymentDate(),
                entity.getPaidAt(),
                entity.getFinancialTransactionId(),
                entity.getObservation()
        );
    }
}
