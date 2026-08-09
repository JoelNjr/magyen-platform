import httpClient from '../../../services/httpClient'

export async function getProductionOrders() {
  const response = await httpClient.get('/production-orders')
  return response.data
}

export async function getProductionOrder(productionOrderId) {
  const response = await httpClient.get(`/production-orders/${productionOrderId}`)
  return response.data
}

/**
 * Crea una Orden de Producción a partir de una Orden comercial existente.
 * Usa el contrato REST actual: POST /api/v1/production-orders
 */
export async function createProductionOrderFromOrder(orderId) {
  const response = await httpClient.post('/production-orders', {
    orderId,
    priority: 'NORMAL',
  })
  return response.data
}

export async function planProductionOrder(productionOrderId, payload) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/plan`,
    payload
  )
  return response.data
}

export async function startProductionOrder(productionOrderId) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/start`
  )
  return response.data
}

export async function completeProductionOrder(productionOrderId) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/complete`
  )
  return response.data
}

export async function addProductionOperation(productionOrderId, payload) {
  const response = await httpClient.post(
    `/production-orders/${productionOrderId}/operations`,
    payload
  )
  return response.data
}

export async function assignProductionOperationOperator(
  productionOrderId,
  operationId,
  payload
) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/operations/${operationId}/assign-operator`,
    payload
  )
  return response.data
}

export async function startProductionOperation(productionOrderId, operationId) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/operations/${operationId}/start`
  )
  return response.data
}

export async function completeProductionOperation(
  productionOrderId,
  operationId
) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/operations/${operationId}/complete`
  )
  return response.data
}
