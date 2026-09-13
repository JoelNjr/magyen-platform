export const QUOTATION_HAS_ASSOCIATED_ORDER_MESSAGE =
  'La cotización tiene una orden asociada. Los cambios de la cotización no modifican automáticamente la orden.'

export const QUOTATION_ORDER_FROZEN_MESSAGE =
  'La cotización puede editarse, pero la orden ya fue entregada/cerrada y no puede modificarse.'

export const QUOTATION_ORDER_LEGACY_MESSAGE =
  'Esta orden fue creada antes de la trazabilidad por ítem. Los cambios de la cotización no pueden aplicarse automáticamente. Edite la orden directamente.'

export const QUOTATION_ORDER_PAYMENT_FLOOR_MESSAGE =
  'El nuevo total de la orden quedaría por debajo de lo ya pagado.'

export function canShowApplyQuotationToOrder(preview) {
  return Boolean(preview?.applyAllowed)
}

export function getQuotationOrderSynchronizationNotes(preview) {
  if (!preview?.orderExists) {
    return []
  }

  const notes = [
    {
      severity: 'info',
      message: QUOTATION_HAS_ASSOCIATED_ORDER_MESSAGE,
    },
  ]

  if (preview.unavailableReason === 'ORDER_FROZEN') {
    notes.push({
      severity: 'warning',
      message: QUOTATION_ORDER_FROZEN_MESSAGE,
    })
  } else if (preview.legacyUntraced || preview.unavailableReason === 'LEGACY_UNTRACED') {
    notes.push({
      severity: 'warning',
      message: QUOTATION_ORDER_LEGACY_MESSAGE,
    })
  }

  return notes
}
