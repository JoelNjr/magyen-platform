package com.magyen.platform.commercial.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderNumberTest {

    @Test
    void fromQuotationNumberUsesTheSameCommercialConsecutive() {
        OrderNumber orderNumber = OrderNumber.fromQuotationNumber(QuotationNumber.of(14L));

        assertEquals("14", orderNumber.getValue());
    }

    @Test
    void fromQuotationNumberRejectsNullQuotationNumber() {
        assertThrows(NullPointerException.class, () -> OrderNumber.fromQuotationNumber(null));
    }
}
