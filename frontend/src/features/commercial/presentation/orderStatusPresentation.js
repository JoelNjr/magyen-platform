/**
 * Presentation helpers for Commercial OrderStatus chips.
 */
export function getOrderStatusChipProps(status) {
  switch (status) {
    case 'CONFIRMED':
      return { label: 'Confirmada', color: 'info' }
    case 'IN_PRODUCTION':
      return { label: 'En producción', color: 'warning' }
    case 'READY_FOR_DELIVERY':
      return { label: 'Lista para entrega', color: 'primary' }
    case 'DELIVERED':
      return { label: 'Entregada', color: 'success' }
    case 'CLOSED':
      return { label: 'Cerrada', color: 'default' }
    default:
      return { label: status || 'Estado desconocido', color: 'default' }
  }
}
