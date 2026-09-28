export const NO_ASSOCIATED_ORDER_LABEL = 'Sin pedido'

export function formatAssociatedOrderLabel(order) {
  if (!order?.orderId) {
    return NO_ASSOCIATED_ORDER_LABEL
  }
  const number =
    order.orderNumber != null && String(order.orderNumber).trim() !== ''
      ? `Pedido #${order.orderNumber}`
      : 'Pedido'
  return order.customerName ? `${number} — ${order.customerName}` : number
}

export function associatedOrderSubmitValue(orderId, transactionType) {
  if (transactionType !== 'EXPENSE') {
    return null
  }
  if (!orderId) {
    return null
  }
  return orderId
}
