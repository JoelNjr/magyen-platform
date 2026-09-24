package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link OrderEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Usa findFirst porque el entorno de desarrollo puede contener duplicados históricos
     * hasta aplicar UNIQUE(quotation_id).
     */
    Optional<OrderEntity> findFirstByQuotationId(UUID quotationId);

    List<OrderEntity> findByPromisedDeliveryDateBetween(LocalDate fromDate, LocalDate toDate);

    @Query("""
            select orderEntity from OrderEntity orderEntity
            where (
                    :includeUndeliveredEligible = true
                    and orderEntity.status in (
                        com.magyen.platform.commercial.domain.OrderStatus.CONFIRMED,
                        com.magyen.platform.commercial.domain.OrderStatus.IN_PRODUCTION,
                        com.magyen.platform.commercial.domain.OrderStatus.READY_FOR_DELIVERY
                    )
                )
                or (
                    orderEntity.status = com.magyen.platform.commercial.domain.OrderStatus.DELIVERED
                    and (
                        (orderEntity.actualDeliveryDate is not null
                            and orderEntity.actualDeliveryDate between :fromDate and :toDate)
                        or (orderEntity.actualDeliveryDate is null
                            and orderEntity.promisedDeliveryDate between :fromDate and :toDate)
                    )
                )
            """)
    List<OrderEntity> findForIndividualProfitabilityMonth(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("includeUndeliveredEligible") boolean includeUndeliveredEligible
    );
}
