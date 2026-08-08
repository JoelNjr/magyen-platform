import httpClient from '../../../services/httpClient'

export async function getQuotations() {
  const response = await httpClient.get('/quotations')
  return response.data
}
