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
