/**
 * Display-only friendly quotation code.
 * Derived from quotationId for readability; not a permanent business number.
 * Routing and API calls must keep using the real quotationId UUID.
 */
export function formatQuotationCode(quotationId) {
  if (!quotationId) {
    return ''
  }

  const normalized = String(quotationId).replace(/-/g, '')
  let hash = 0

  for (let index = 0; index < normalized.length; index += 1) {
    hash = (hash * 31 + normalized.charCodeAt(index)) >>> 0
  }

  const sequence = (hash % 1000000) + 1
  return `C${String(sequence).padStart(6, '0')}`
}
