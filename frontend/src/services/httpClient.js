import axios from 'axios'

const httpClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
})

export default httpClient
