import httpClient from '../../../services/httpClient'
import { triggerBrowserPdfDownload } from '../../commercial/presentation/commercialDocumentDownload'

export async function getProductionOrders(params = {}) {
  const response = await httpClient.get('/production-orders', {
    params: {
      fromDate: params.fromDate || undefined,
      toDate: params.toDate || undefined,
    },
  })
  return response.data
}

export async function getProductionOrder(productionOrderId) {
  const response = await httpClient.get(`/production-orders/${productionOrderId}`)
  return response.data
}

export async function downloadProductionOrderPdf(productionOrderId) {
  const response = await httpClient.get(
    `/production-orders/${productionOrderId}/pdf`,
    {
      responseType: 'blob',
      timeout: 30000,
    }
  )
  triggerBrowserPdfDownload(response, 'Orden-de-Produccion.pdf')
}

export async function getProductionMaterialConsumptions(productionOrderId) {
  const response = await httpClient.get(
    `/production-orders/${productionOrderId}/material-consumptions`
  )
  return response.data
}

export async function registerProductionMaterialConsumption(
  productionOrderId,
  payload
) {
  const response = await httpClient.post(
    `/production-orders/${productionOrderId}/material-consumptions`,
    payload
  )
  return response.data
}

export async function getEligibleProductionLaborOperators() {
  const response = await httpClient.get('/production/labor-operators')
  return response.data
}

export async function getProductionLaborWorks(productionOrderId) {
  const response = await httpClient.get(
    `/production-orders/${productionOrderId}/labor`
  )
  return response.data
}

export async function registerProductionLaborWork(productionOrderId, payload) {
  const response = await httpClient.post(
    `/production-orders/${productionOrderId}/labor`,
    payload
  )
  return response.data
}

export async function payProductionLaborWork(
  productionOrderId,
  laborWorkId,
  payload = {}
) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/labor/${laborWorkId}/pay`,
    payload
  )
  return response.data
}

export async function cancelProductionLaborWork(productionOrderId, laborWorkId) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/labor/${laborWorkId}/cancel`
  )
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

export async function startProductionOrder(productionOrderId, payload = {}) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/start`,
    payload
  )
  return response.data
}

export async function completeProductionOrder(productionOrderId, payload = {}) {
  const response = await httpClient.patch(
    `/production-orders/${productionOrderId}/complete`,
    payload
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
