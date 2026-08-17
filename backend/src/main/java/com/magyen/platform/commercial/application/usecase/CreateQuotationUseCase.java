package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeeInfo;
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
    private final SellerNameResolver sellerNameResolver;

    public CreateQuotationUseCase(
            QuotationRepository quotationRepository,
            QuotationNumberGenerator quotationNumberGenerator,
            SellerNameResolver sellerNameResolver
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.quotationNumberGenerator = Objects.requireNonNull(
                quotationNumberGenerator,
                "Quotation number generator must not be null"
        );
        this.sellerNameResolver = Objects.requireNonNull(
                sellerNameResolver,
                "Seller name resolver must not be null"
        );
    }

    public CreateQuotationResult execute(CreateQuotationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        CommercialSellerEmployeeInfo seller = sellerNameResolver.requireEligibleSeller(command.sellerId());
        LocalDate creationDate = command.quotationDate() != null
                ? command.quotationDate()
                : LocalDate.now();
        QuotationNumber quotationNumber = quotationNumberGenerator.next();

        Quotation quotation = Quotation.create(
                quotationNumber,
                command.customerId(),
                creationDate,
                command.deliveryDate(),
                seller.employeeId(),
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
        if (command.customerId() == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }
        if (command.deliveryDate() == null) {
            throw new IllegalArgumentException("Delivery date must not be null");
        }
        if (command.sellerId() == null) {
            throw new IllegalArgumentException("Seller id must not be null");
        }
    }
}
