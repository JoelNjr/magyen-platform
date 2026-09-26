package com.magyen.platform.shared.testsupport;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.domain.CustomerCategory;

import java.util.UUID;

/**
 * Crea un cliente del grupo PLOTTER para trabajos externos de prueba.
 */
public final class PlotterCustomerFixture {

    private PlotterCustomerFixture() {
    }

    public static UUID create(CreateCustomerUseCase createCustomerUseCase) {
        return createCustomerUseCase.execute(new CreateCustomerCommand(
                "Cliente Plotter " + UUID.randomUUID(),
                CustomerCategory.PLOTTER
        )).customerId();
    }
}
