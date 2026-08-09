package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationNumberGenerator;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que coordina la creación de una nueva cotización.
 */
public class CreateQuotationUseCase {

    private final QuotationRepository quotationRepository;
    private final QuotationNumberGenerator quotationNumberGenerator;

    public CreateQuotationUseCase(
            QuotationRepository quotationRepository,
            QuotationNumberGenerator quotationNumberGenerator
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.quotationNumberGenerator = Objects.requireNonNull(
                quotationNumberGenerator,
                "Quotation number generator must not be null"
        );
    }

    public CreateQuotationResult execute(CreateQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        LocalDate creationDate = LocalDate.now();
        QuotationNumber quotationNumber = quotationNumberGenerator.next();

        Quotation quotation = Quotation.create(
                quotationNumber,
                command.customerId(),
                creationDate,
                command.deliveryDate(),
                command.salesperson(),
                command.observations()
        );

        Quotation savedQuotation = quotationRepository.save(quotation);

        return new CreateQuotationResult(
                savedQuotation.getId(),
                toQuotationNumberValue(savedQuotation.getQuotationNumber()),
                savedQuotation.getStatus(),
                savedQuotation.getCreationDate()
        );
    }

    private Long toQuotationNumberValue(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return quotationNumber.getValue();
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
