/**
 * Presentation-only formatter for the backend commercial quotation number.
 * Routing and API calls must keep using quotationId (UUID).
 */
export function formatQuotationNumber(quotationNumber) {
  if (quotationNumber === null || quotationNumber === undefined || quotationNumber === '') {
    return 'Sin número'
  }

  const numericValue = Number(quotationNumber)

  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return 'Sin número'
  }

  return `C${String(Math.trunc(numericValue)).padStart(6, '0')}`
}

/**
 * Número comercial de orden reservado por la cotización de origen.
 * Cotización C000014 → orden 14.
 */
export function formatReservedOrderNumber(quotationNumber) {
  if (quotationNumber === null || quotationNumber === undefined || quotationNumber === '') {
    return null
  }

  const numericValue = Number(quotationNumber)

  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return null
  }

  return String(Math.trunc(numericValue))
}
