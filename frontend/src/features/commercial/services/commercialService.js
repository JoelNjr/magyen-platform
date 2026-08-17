import httpClient from '../../../services/httpClient'

export async function getQuotations() {
  const response = await httpClient.get('/quotations')
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

export async function createSeller(payload) {
  const response = await httpClient.post('/sellers', payload)
  return response.data
}

export function createQuotation(payload) {
  return httpClient.post('/quotations', payload).then((response) => response.data)
}

export async function getQuotation(quotationId) {
  const response = await httpClient.get(`/quotations/${quotationId}`)
  return response.data
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

export async function getOrders() {
  const response = await httpClient.get('/orders')
  return response.data
}

export async function getOrder(orderId) {
  const response = await httpClient.get(`/orders/${orderId}`)
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
