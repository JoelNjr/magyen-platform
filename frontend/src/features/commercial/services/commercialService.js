import httpClient from '../../../services/httpClient'
import { triggerBrowserPdfDownload } from '../presentation/commercialDocumentDownload'

export async function getQuotations(params = {}) {
  const response = await httpClient.get('/quotations', {
    params: {
      fromDate: params.fromDate || undefined,
      toDate: params.toDate || undefined,
    },
  })
  return response.data
}

export async function getCustomers() {
  const response = await httpClient.get('/customers')
  return response.data
}

export async function createCustomer(payload) {
  const response = await httpClient.post('/customers', payload)
  return response.data
}

export async function updateCustomer(customerId, payload) {
  const response = await httpClient.put(`/customers/${customerId}`, payload)
  return response.data
}

export async function getSellers() {
  const response = await httpClient.get('/sellers')
  return response.data
}

export async function getCommercialCatalogs() {
  const response = await httpClient.get('/commercial-catalogs')
  return response.data
}

export function createQuotation(payload) {
  return httpClient.post('/quotations', payload).then((response) => response.data)
}

export async function getQuotation(quotationId) {
  const response = await httpClient.get(`/quotations/${quotationId}`)
  return response.data
}

export async function downloadQuotationPdf(quotationId) {
  const response = await httpClient.get(`/quotations/${quotationId}/pdf`, {
    responseType: 'blob',
    timeout: 30000,
  })
  triggerBrowserPdfDownload(response, 'Cotizacion.pdf')
}

export async function addQuotationItem(quotationId, payload) {
  const response = await httpClient.post(`/quotations/${quotationId}/items`, payload)
  return response.data
}

export async function approveQuotation(quotationId) {
  const response = await httpClient.patch(`/quotations/${quotationId}/approve`)
  return response.data
}

export async function createOrder(payload) {
  const response = await httpClient.post('/orders', payload)
  return response.data
}

export async function getOrders(params = {}) {
  const response = await httpClient.get('/orders', {
    params: {
      fromDate: params.fromDate || undefined,
      toDate: params.toDate || undefined,
    },
  })
  return response.data
}

export async function getOrder(orderId) {
  const response = await httpClient.get(`/orders/${orderId}`)
  return response.data
}

export async function downloadOrderRemissionPdf(orderId) {
  const response = await httpClient.get(`/orders/${orderId}/remission/pdf`, {
    responseType: 'blob',
    timeout: 30000,
  })
  triggerBrowserPdfDownload(response, 'Remision.pdf')
}

export async function getOrderProfitabilityList() {
  const response = await httpClient.get('/orders/profitability')
  return response.data
}

export async function getOrderProfitability(orderId) {
  const response = await httpClient.get(`/orders/${orderId}/profitability`)
  return response.data
}

export async function getPaymentsByOrder(orderId) {
  const response = await httpClient.get(`/payments/orders/${orderId}`)
  return response.data
}

export async function registerOrderPayment(payload) {
  const response = await httpClient.post('/payments', payload)
  return response.data
}

export async function replaceOrderItemSizes(orderId, orderItemId, sizes) {
  const response = await httpClient.put(
    `/orders/${orderId}/items/${orderItemId}/sizes`,
    { sizes }
  )
  return response.data
}

export async function updateOrderItemProductSpecification(
  orderId,
  orderItemId,
  productSpecification
) {
  const response = await httpClient.put(
    `/orders/${orderId}/items/${orderItemId}/product-specification`,
    productSpecification
  )
  return response.data
}
