package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetSellersResult;
import com.magyen.platform.commercial.application.dto.SellerResult;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeeInfo;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeePort;

import java.util.List;
import java.util.Objects;

/**
 * Consulta vendedores elegibles: empleados Finance activos con pago fijo.
 */
public class GetSellersUseCase {

    private final CommercialSellerEmployeePort commercialSellerEmployeePort;

    public GetSellersUseCase(CommercialSellerEmployeePort commercialSellerEmployeePort) {
        this.commercialSellerEmployeePort = Objects.requireNonNull(
                commercialSellerEmployeePort,
                "Commercial seller employee port must not be null"
        );
    }

    public GetSellersResult execute() {
        List<SellerResult> sellers = commercialSellerEmployeePort.listActiveFixedSellers().stream()
                .map(this::toSellerResult)
                .toList();

        return new GetSellersResult(sellers);
    }

    private SellerResult toSellerResult(CommercialSellerEmployeeInfo seller) {
        return new SellerResult(
                seller.employeeId(),
                seller.displayName(),
                seller.active()
        );
    }
}
