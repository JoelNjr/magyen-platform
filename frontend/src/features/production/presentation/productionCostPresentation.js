const moneyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

/**
 * Formatea un costo de material. Null/undefined = sin costo configurado (nunca $0).
 */
export function formatProductionMaterialCost(value) {
  if (value === null || value === undefined || value === '') {
    return null
  }

  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return null
  }

  return moneyFormatter.format(amount)
}

export function formatProductionMaterialCostOrUnvalued(value) {
  return formatProductionMaterialCost(value) ?? 'Sin costo configurado'
}
