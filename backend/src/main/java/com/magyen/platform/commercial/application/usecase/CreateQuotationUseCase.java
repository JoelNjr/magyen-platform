package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que coordina la creación de una nueva cotización.
 */
public class CreateQuotationUseCase {

    private final QuotationRepository quotationRepository;

    public CreateQuotationUseCase(QuotationRepository quotationRepository) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
    }

    public CreateQuotationResult execute(CreateQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        LocalDate creationDate = LocalDate.now();

        Quotation quotation = Quotation.create(
                command.customerId(),
                creationDate,
                command.deliveryDate(),
                command.salesperson(),
                command.observations()
        );

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new CreateQuotationResult(
                savedQuotation.getId(),
                savedQuotation.getStatus(),
                savedQuotation.getCreationDate()
        );
    }

    private void validateCommand(CreateQuotationCommand command) {
        Objects.requireNonNull(command.customerId(), "Customer id must not be null");
        Objects.requireNonNull(command.deliveryDate(), "Delivery date must not be null");
        Objects.requireNonNull(command.salesperson(), "Salesperson must not be null");

        if (command.salesperson().isBlank()) {
            throw new IllegalArgumentException("Salesperson must not be blank");
        }
    }
}
