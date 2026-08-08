import httpClient from '../../../services/httpClient'

export async function getSalesReport() {
  const response = await httpClient.get('/reports/sales')
  return response.data
}

export async function getProductionReport() {
  const response = await httpClient.get('/reports/production')
  return response.data
}

export async function getInventoryReport() {
  const response = await httpClient.get('/reports/inventory')
  return response.data
}

export async function getPaymentsReport() {
  const response = await httpClient.get('/reports/payments')
  return response.data
}

export async function getNotifications() {
  const response = await httpClient.get('/notifications')
  return response.data
}
