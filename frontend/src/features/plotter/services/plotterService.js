import httpClient from '../../../services/httpClient'

export async function getPlotterJobs() {
  const response = await httpClient.get('/plotter/jobs')
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
