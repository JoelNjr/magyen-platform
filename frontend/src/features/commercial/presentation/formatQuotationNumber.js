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
