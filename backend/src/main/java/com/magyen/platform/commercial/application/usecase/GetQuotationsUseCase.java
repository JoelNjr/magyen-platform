package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetQuotationsResult;
import com.magyen.platform.commercial.application.dto.QuotationResult;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Caso de uso que consulta las cotizaciones existentes.
 */
public class GetQuotationsUseCase {

    private final QuotationRepository quotationRepository;
    private final SellerNameResolver sellerNameResolver;

    public GetQuotationsUseCase(
            QuotationRepository quotationRepository,
            SellerNameResolver sellerNameResolver
    ) {
        this.quotationRepository = Objects.requireNonNull(quotationRepository, "Quotation repository must not be null");
        this.sellerNameResolver = Objects.requireNonNull(sellerNameResolver, "Seller name resolver must not be null");
    }

    public GetQuotationsResult execute() {
        List<Quotation> quotations = quotationRepository.findAll();
        Function<UUID, String> sellerNames = sellerNameResolver.nameLookup(
                quotations.stream().map(Quotation::getSellerId).toList()
        );

        List<QuotationResult> results = quotations.stream()
                .map(quotation -> toQuotationResult(quotation, sellerNames.apply(quotation.getSellerId())))
                .toList();

        return new GetQuotationsResult(results);
    }

    private QuotationResult toQuotationResult(Quotation quotation, String sellerName) {
        return new QuotationResult(
                quotation.getId(),
                toQuotationNumberValue(quotation.getQuotationNumber()),
                quotation.getCustomerId(),
                quotation.getCreationDate(),
                quotation.getDeliveryDate(),
                quotation.getStatus(),
                quotation.getSellerId(),
                sellerName,
                quotation.getObservations(),
                quotation.getTotal().getAmount()
        );
    }

    private Long toQuotationNumberValue(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return quotationNumber.getValue();
    }
}
