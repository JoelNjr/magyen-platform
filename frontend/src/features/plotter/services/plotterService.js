import httpClient from '../../../services/httpClient'

export async function getPlotterJobs(params = {}) {
  const response = await httpClient.get('/plotter/jobs', {
    params: {
      fromDate: params.fromDate || undefined,
      toDate: params.toDate || undefined,
    },
  })
  return response.data
}

export async function getPlotterProfitability({ fromDate, toDate, scope } = {}) {
  const response = await httpClient.get('/plotter/profitability', {
    params: {
      fromDate: fromDate || undefined,
      toDate: toDate || undefined,
      scope: scope || undefined,
    },
  })
  return response.data
}

export async function getPlotterJob(plotterJobId) {
  const response = await httpClient.get(`/plotter/jobs/${plotterJobId}`)
  return response.data
}

export async function createPlotterJob(payload) {
  const response = await httpClient.post('/plotter/jobs', payload)
  return response.data
}

export async function getPlotterPayments(plotterJobId) {
  const response = await httpClient.get(`/plotter/jobs/${plotterJobId}/payments`)
  return response.data
}

export async function registerPlotterPayment(plotterJobId, payload) {
  const response = await httpClient.post(
    `/plotter/jobs/${plotterJobId}/payments`,
    payload
  )
  return response.data
}
