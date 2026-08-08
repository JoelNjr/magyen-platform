import httpClient from '../../../services/httpClient'

export async function getQuotations() {
  const response = await httpClient.get('/quotations')
  return response.data
}

export function createQuotation(payload) {
  return httpClient.post('/quotations', payload).then((response) => response.data)
}
