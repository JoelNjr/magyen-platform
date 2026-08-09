package com.magyen.platform.commercial.domain;

/**
 * Port for obtaining the next commercial quotation number.
 * <p>
 * The concrete implementation lives in infrastructure and must use the database sequence.
 */
public interface QuotationNumberGenerator {

    QuotationNumber next();
}
