package com.magyen.platform.commercial.domain;

/**
 * Formato comercial del número de cotización (C000001).
 * <p>
 * El UUID permanece como identidad técnica; este valor es el identificador de negocio.
 */
public final class QuotationNumberFormat {

    private QuotationNumberFormat() {
    }

    public static String display(QuotationNumber quotationNumber) {
        if (quotationNumber == null) {
            return null;
        }
        return display(quotationNumber.getValue());
    }

    public static String display(Long quotationNumber) {
        if (quotationNumber == null || quotationNumber <= 0) {
            return null;
        }
        return "C" + String.format("%06d", quotationNumber);
    }
}
