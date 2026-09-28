import { getOrderStatusChipProps } from './orderStatusPresentation.js'

const NEXT_TRANSITION = {
  CONFIRMED: {
    status: 'IN_PRODUCTION',
    action: 'start-production',
  },
  IN_PRODUCTION: {
    status: 'READY_FOR_DELIVERY',
    action: 'ready-for-delivery',
  },
  READY_FOR_DELIVERY: {
    status: 'DELIVERED',
    action: 'deliver',
  },
  DELIVERED: {
    status: 'CLOSED',
    action: 'close',
  },
}

export function orderStatusChangeOptions(currentStatus) {
  const current = {
    status: currentStatus,
    label: getOrderStatusChipProps(currentStatus).label,
    current: true,
  }
  const next = NEXT_TRANSITION[currentStatus]
  if (!next) {
    return [current]
  }
  return [
    current,
    {
      status: next.status,
      label: getOrderStatusChipProps(next.status).label,
      action: next.action,
      current: false,
    },
  ]
}

export function hasNextOrderStatus(currentStatus) {
  return Boolean(NEXT_TRANSITION[currentStatus])
}

export function resolveOrderStatusSelection(currentStatus, selectedStatus) {
  if (!selectedStatus || selectedStatus === currentStatus) {
    return { type: 'none' }
  }
  const next = NEXT_TRANSITION[currentStatus]
  if (!next || next.status !== selectedStatus) {
    return { type: 'invalid' }
  }
  if (next.action === 'deliver') {
    return { type: 'requires-delivery-date', status: next.status, action: next.action }
  }
  return { type: 'transition', status: next.status, action: next.action }
}

export function canSubmitOrderStatusChange(changingOrderId, orderId) {
  return !changingOrderId && Boolean(orderId)
}

export function applyOrderStatusToList(orders, orderId, status) {
  return orders.map((order) =>
    order.orderId === orderId ? { ...order, status } : order
  )
}

export function orderStatusChangeSuccessMessage(status) {
  switch (status) {
    case 'IN_PRODUCTION':
      return 'Pedido marcado en producción.'
    case 'READY_FOR_DELIVERY':
      return 'Pedido marcado como listo para entrega.'
    case 'DELIVERED':
      return 'Entrega registrada.'
    case 'CLOSED':
      return 'Pedido cerrado.'
    default:
      return 'Estado del pedido actualizado.'
  }
}

export function orderStatusChangeErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}
