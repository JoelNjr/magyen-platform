import httpClient from '../../../services/httpClient'

/**
 * Read model del Dashboard Home (SPR-037).
 * @param {{ fromDate?: string, toDate?: string }} [params]
 */
export async function getHomeDashboard(params = {}) {
  const response = await httpClient.get('/home/dashboard', { params })
  return response.data
}

/**
 * Rentabilidad Home por período de entrega programada.
 * Independiente del período financiero del dashboard.
 * @param {{ fromDate?: string, toDate?: string }} [params]
 */
export async function getHomeProfitability(params = {}) {
  const response = await httpClient.get('/home/profitability', { params })
  return response.data
}
