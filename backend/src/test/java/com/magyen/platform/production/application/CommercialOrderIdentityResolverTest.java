package com.magyen.platform.production.application;

import com.magyen.platform.commercial.application.dto.CustomerResult;
import com.magyen.platform.commercial.application.dto.GetCustomersResult;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.usecase.GetCustomersUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommercialOrderIdentityResolverTest {

    @Mock
    private GetOrdersUseCase getOrdersUseCase;

    @Mock
    private GetCustomersUseCase getCustomersUseCase;

    @Test
    void resolvesOrderNumberAndCustomerNameWithoutInventingValues() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(getOrdersUseCase.execute()).thenReturn(new GetOrdersResult(List.of(
                order(orderId, "PED-42", customerId)
        )));
        when(getCustomersUseCase.execute()).thenReturn(new GetCustomersResult(List.of(
                new CustomerResult(customerId, "Colegio XYZ")
        )));

        Map<UUID, CommercialOrderIdentityResolver.CommercialOrderIdentity> identities =
                new CommercialOrderIdentityResolver(getOrdersUseCase, getCustomersUseCase).resolveAll();

        CommercialOrderIdentityResolver.CommercialOrderIdentity identity = identities.get(orderId);
        assertEquals("PED-42", identity.orderNumber());
        assertEquals(customerId, identity.customerId());
        assertEquals("Colegio XYZ", identity.customerName());
    }

    @Test
    void missingCustomerNameDoesNotFallBackToUuid() {
        UUID orderId = UUID.randomUUID();
        UUID unknownCustomerId = UUID.randomUUID();
        when(getOrdersUseCase.execute()).thenReturn(new GetOrdersResult(List.of(
                order(orderId, "PED-99", unknownCustomerId)
        )));
        when(getCustomersUseCase.execute()).thenReturn(new GetCustomersResult(List.of()));

        CommercialOrderIdentityResolver.CommercialOrderIdentity identity =
                new CommercialOrderIdentityResolver(getOrdersUseCase, getCustomersUseCase).resolve(orderId);

        assertEquals("PED-99", identity.orderNumber());
        assertEquals(unknownCustomerId, identity.customerId());
        assertNull(identity.customerName());
    }

    @Test
    void unknownOrderReturnsMissingIdentityWithoutFakeName() {
        UUID missingOrderId = UUID.randomUUID();
        when(getOrdersUseCase.execute()).thenReturn(new GetOrdersResult(List.of()));
        when(getCustomersUseCase.execute()).thenReturn(new GetCustomersResult(List.of()));

        CommercialOrderIdentityResolver.CommercialOrderIdentity identity =
                new CommercialOrderIdentityResolver(getOrdersUseCase, getCustomersUseCase).resolve(missingOrderId);

        assertEquals(missingOrderId, identity.orderId());
        assertNull(identity.orderNumber());
        assertNull(identity.customerName());
    }

    private static OrderResult order(UUID orderId, String orderNumber, UUID customerId) {
        return new OrderResult(
                orderId,
                orderNumber,
                customerId,
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                OrderStatus.CONFIRMED,
                "Tester",
                null,
                BigDecimal.TEN
        );
    }
}
