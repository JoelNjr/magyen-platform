import { customersForExternalPlotter } from '../../commercial/presentation/customerCategory.js'

export const ALL_PLOTTER_JOBS_FILTER = ''
export const INTERNAL_MAGYEN_JOBS_FILTER = 'INTERNAL_MAGYEN'
export const INTERNAL_MAGYEN_FILTER_LABEL = 'Trabajo interno Magyen'
export const ALL_PLOTTER_JOBS_FILTER_LABEL = 'Todos los trabajos'

export function externalPlotterCustomerOptions(customers) {
  return customersForExternalPlotter(customers)
}

export function toPlotterJobsQuery(filterValue, period = {}) {
  const query = {
    fromDate: period.fromDate,
    toDate: period.toDate,
  }
  if (!filterValue || filterValue === ALL_PLOTTER_JOBS_FILTER) {
    return query
  }
  if (filterValue === INTERNAL_MAGYEN_JOBS_FILTER) {
    return { ...query, jobType: INTERNAL_MAGYEN_JOBS_FILTER }
  }
  return { ...query, customerId: filterValue }
}
