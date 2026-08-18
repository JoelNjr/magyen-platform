import { getCommercialCatalogs } from '../services/commercialService'

/**
 * Carga catálogos comerciales activos desde el backend.
 * Las etiquetas de negocio viven en Administración, no duplicadas en el frontend.
 */
export function loadCommercialCatalogs() {
  return getCommercialCatalogs()
}

export function formatCuffRequired(value) {
  if (value === true) {
    return 'Sí'
  }
  if (value === false) {
    return 'No'
  }
  return '—'
}

export function formatQuotationNumberDisplay(quotationNumber, quotationNumberDisplay) {
  if (typeof quotationNumberDisplay === 'string' && quotationNumberDisplay.trim()) {
    return quotationNumberDisplay.trim()
  }
  if (quotationNumber == null || quotationNumber === '') {
    return null
  }
  const numeric = Number(quotationNumber)
  if (!Number.isFinite(numeric) || numeric < 0) {
    return String(quotationNumber)
  }
  return `C${String(Math.trunc(numeric)).padStart(6, '0')}`
}
