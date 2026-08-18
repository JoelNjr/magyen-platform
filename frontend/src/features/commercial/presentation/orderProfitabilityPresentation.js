export function formatProfitabilityMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const numericValue = Number(value)
  if (Number.isNaN(numericValue)) {
    return '—'
  }

  return `$${numericValue.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}

export function formatProfitabilityMargin(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const numericValue = Number(value)
  if (Number.isNaN(numericValue)) {
    return '—'
  }

  return `${numericValue.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} %`
}

export function isProfitabilityComplete(status) {
  return status === 'COMPLETE'
}

export function formatProfitabilityResultMoney(value, status) {
  if (!isProfitabilityComplete(status)) {
    return '—'
  }
  return formatProfitabilityMoney(value)
}

export function formatProfitabilityResultMargin(value, status) {
  if (!isProfitabilityComplete(status)) {
    return '—'
  }
  return formatProfitabilityMargin(value)
}

export function getOrderProfitabilityStatusLabel(status) {
  if (status === 'COMPLETE') {
    return 'Rentabilidad completa'
  }
  if (status === 'PARTIALLY_UNVALUED') {
    return 'Rentabilidad parcial — faltan costos por valorar'
  }
  if (status === 'NO_COST_DATA') {
    return 'Sin datos de costo'
  }
  return status || '—'
}

export function getOrderProfitabilityStatusAlertSeverity(status) {
  const color = getOrderProfitabilityStatusChipProps(status).color
  if (color === 'success' || color === 'warning' || color === 'info' || color === 'error') {
    return color
  }
  return 'info'
}

export function getOrderProfitabilityStatusChipProps(status) {
  if (status === 'COMPLETE') {
    return { label: getOrderProfitabilityStatusLabel(status), color: 'success' }
  }
  if (status === 'PARTIALLY_UNVALUED') {
    return { label: getOrderProfitabilityStatusLabel(status), color: 'warning' }
  }
  if (status === 'NO_COST_DATA') {
    return { label: getOrderProfitabilityStatusLabel(status), color: 'info' }
  }
  return { label: getOrderProfitabilityStatusLabel(status), color: 'default' }
}

export function formatPlotterProductionCost(profitability) {
  if (!profitability) {
    return '—'
  }
  if (profitability.plotterCostAttributable) {
    return formatProfitabilityMoney(profitability.plotterMaterialCost)
  }
  if (Number(profitability.plotterMaterialCost) > 0) {
    return `${formatProfitabilityMoney(profitability.plotterMaterialCost)} (parcial)`
  }
  if (profitability.profitabilityStatus === 'PARTIALLY_UNVALUED') {
    return 'Sin valorar'
  }
  return '—'
}

export function formatMaterialProductionCost(profitability) {
  if (!profitability) {
    return '—'
  }
  if (profitability.profitabilityStatus === 'NO_COST_DATA') {
    return '—'
  }
  if (
    profitability.profitabilityStatus === 'PARTIALLY_UNVALUED' &&
    Number(profitability.unvaluedMaterialConsumptionCount) > 0
  ) {
    return `${formatProfitabilityMoney(profitability.materialCost)} (parcial)`
  }
  return formatProfitabilityMoney(profitability.materialCost)
}

export function formatLaborProductionCost(profitability) {
  if (!profitability) {
    return '—'
  }
  if (profitability.profitabilityStatus === 'NO_COST_DATA') {
    return '—'
  }
  return formatProfitabilityMoney(profitability.laborCost)
}
