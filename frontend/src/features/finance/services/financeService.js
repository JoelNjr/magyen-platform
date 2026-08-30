import httpClient from '../../../services/httpClient'

export async function getFinancialPeriodSummary(params) {
  const response = await httpClient.get('/finance/summary', { params })
  return response.data
}

export async function getFinancialTransactions(params = {}) {
  const response = await httpClient.get('/finance/transactions', {
    params: {
      fromDate: params.fromDate || undefined,
      toDate: params.toDate || undefined,
    },
  })
  return response.data
}

export async function getFinancialTransaction(transactionId) {
  const response = await httpClient.get(`/finance/transactions/${transactionId}`)
  return response.data
}

export async function registerFinancialTransaction(payload) {
  const response = await httpClient.post('/finance/transactions', payload)
  return response.data
}

export async function getRecurringFinancialObligations(params = {}) {
  const response = await httpClient.get('/finance/obligations', { params })
  return response.data
}

export async function createRecurringFinancialObligation(payload) {
  const response = await httpClient.post('/finance/obligations', payload)
  return response.data
}

export async function updateRecurringFinancialObligation(obligationId, payload) {
  const response = await httpClient.put(`/finance/obligations/${obligationId}`, payload)
  return response.data
}

export async function deactivateRecurringFinancialObligation(obligationId) {
  const response = await httpClient.patch(
    `/finance/obligations/${obligationId}/deactivate`
  )
  return response.data
}

export async function getPendingObligationOccurrences() {
  const response = await httpClient.get('/finance/obligation-occurrences/pending')
  return response.data
}

export async function getOverdueObligationOccurrences() {
  const response = await httpClient.get('/finance/obligation-occurrences/overdue')
  return response.data
}

export async function getUpcomingObligationOccurrences(daysAhead = 7) {
  const response = await httpClient.get('/finance/obligation-occurrences/upcoming', {
    params: { daysAhead },
  })
  return response.data
}

export async function payObligationOccurrence(occurrenceId, payload = {}) {
  const response = await httpClient.patch(
    `/finance/obligation-occurrences/${occurrenceId}/pay`,
    payload
  )
  return response.data
}

export async function cancelObligationOccurrence(occurrenceId) {
  const response = await httpClient.patch(
    `/finance/obligation-occurrences/${occurrenceId}/cancel`
  )
  return response.data
}

export async function generateObligationOccurrences(payload) {
  const response = await httpClient.post(
    '/finance/obligation-occurrences/generate',
    payload
  )
  return response.data
}

export async function getPayrollEmployees(params = {}) {
  const response = await httpClient.get('/finance/payroll/employees', { params })
  return response.data
}

export async function createPayrollEmployee(payload) {
  const response = await httpClient.post('/finance/payroll/employees', payload)
  return response.data
}

export async function updatePayrollEmployeeCompensation(employeeId, payload) {
  const response = await httpClient.put(
    `/finance/payroll/employees/${employeeId}/compensation`,
    payload
  )
  return response.data
}

export async function activatePayrollEmployee(employeeId) {
  const response = await httpClient.patch(
    `/finance/payroll/employees/${employeeId}/activate`
  )
  return response.data
}

export async function deactivatePayrollEmployee(employeeId) {
  const response = await httpClient.patch(
    `/finance/payroll/employees/${employeeId}/deactivate`
  )
  return response.data
}

export async function getPayrollEmployeeProductionEarnings(employeeId, params) {
  const response = await httpClient.get(
    `/finance/payroll/employees/${employeeId}/production-earnings`,
    { params }
  )
  return response.data
}

export async function getPayrollEmployeeCommissions(employeeId, params = {}) {
  const response = await httpClient.get(
    `/finance/payroll/employees/${employeeId}/commissions`,
    { params }
  )
  return response.data
}

export async function getPayrollEmployeeFinancialSummary(employeeId, params = {}) {
  const response = await httpClient.get(
    `/finance/payroll/employees/${employeeId}/summary`,
    { params }
  )
  return response.data
}

export async function getPayrollEmployeePerformance(params = {}) {
  const response = await httpClient.get('/finance/payroll/employees/performance', {
    params,
  })
  return response.data
}

export async function getPayrollEmployeeDeductions(employeeId, params = {}) {
  const response = await httpClient.get(
    `/finance/payroll/employees/${employeeId}/deductions`,
    { params }
  )
  return response.data
}

export async function createPayrollDeduction(employeeId, payload) {
  const response = await httpClient.post(
    `/finance/payroll/employees/${employeeId}/deductions`,
    payload
  )
  return response.data
}

export async function cancelPayrollDeduction(employeeId, deductionId) {
  const response = await httpClient.patch(
    `/finance/payroll/employees/${employeeId}/deductions/${deductionId}/cancel`
  )
  return response.data
}

export async function getPayrollPeriods() {
  const response = await httpClient.get('/finance/payroll/periods')
  return response.data
}

export async function generatePayrollPeriods(payload) {
  const response = await httpClient.post(
    '/finance/payroll/periods/generate',
    payload
  )
  return response.data
}

export async function payPayrollPeriod(periodId, payload = {}) {
  const response = await httpClient.patch(
    `/finance/payroll/periods/${periodId}/pay`,
    payload
  )
  return response.data
}

export async function cancelPayrollPeriod(periodId) {
  const response = await httpClient.patch(
    `/finance/payroll/periods/${periodId}/cancel`
  )
  return response.data
}
