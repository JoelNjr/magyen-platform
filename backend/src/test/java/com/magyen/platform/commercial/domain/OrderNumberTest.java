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

    @Test
    void commercialConsecutiveIsNumericNotLexicographic() {
        assertEquals(9L, OrderNumber.of("9").numericConsecutive());
        assertEquals(10L, OrderNumber.of("10").numericConsecutive());
        assertEquals(11L, OrderNumber.of("11").numericConsecutive());

        var ordered = java.util.stream.Stream.of(
                        OrderNumber.of("10"),
                        OrderNumber.of("11"),
                        OrderNumber.of("9"),
                        OrderNumber.of("12"),
                        OrderNumber.of("1")
                )
                .sorted(OrderNumber.byCommercialConsecutive())
                .map(OrderNumber::getValue)
                .toList();

        assertEquals(java.util.List.of("1", "9", "10", "11", "12"), ordered);
    }

    @Test
    void nonNumericHistoricalIdentifiersDoNotPretendToBeConsecutives() {
        assertEquals(null, OrderNumber.of("PED-42").numericConsecutive());
    }
}
