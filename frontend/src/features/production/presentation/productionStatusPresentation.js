export function getProductionOrderStatusChipProps(status) {
  switch (status) {
    case 'CREATED':
      return { label: 'Creada', color: 'default' }
    case 'PLANNED':
      return { label: 'Planificada', color: 'info' }
    case 'IN_PROGRESS':
      return { label: 'En progreso', color: 'warning' }
    case 'COMPLETED':
      return { label: 'Completada', color: 'success' }
    default:
      return { label: status || 'Estado desconocido', color: 'default' }
  }
}

export function getProductionPriorityChipProps(priority) {
  switch (priority) {
    case 'LOW':
      return { label: 'Baja', color: 'default' }
    case 'NORMAL':
      return { label: 'Normal', color: 'default' }
    case 'HIGH':
      return { label: 'Alta', color: 'warning' }
    case 'URGENT':
      return { label: 'Urgente', color: 'error' }
    default:
      return { label: priority || 'Sin prioridad', color: 'default' }
  }
}

export function getProductionOperationStatusChipProps(status) {
  switch (status) {
    case 'PENDING':
      return { label: 'Pendiente', color: 'default' }
    case 'IN_PROGRESS':
      return { label: 'En progreso', color: 'warning' }
    case 'COMPLETED':
      return { label: 'Completada', color: 'success' }
    default:
      return { label: status || 'Estado desconocido', color: 'default' }
  }
}

export function formatProductionOperationType(type) {
  switch (type) {
    case 'CUTTING':
      return 'Corte'
    case 'CALENDERING':
      return 'Calandrado'
    case 'SUBLIMATION':
      return 'Sublimación'
    case 'SEWING':
      return 'Confección'
    case 'QUALITY_CONTROL':
      return 'Control de calidad'
    default:
      return type || ''
  }
}
