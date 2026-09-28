import assert from 'node:assert/strict'
import test from 'node:test'
import {
  ALL_PLOTTER_JOBS_FILTER,
  INTERNAL_MAGYEN_FILTER_LABEL,
  INTERNAL_MAGYEN_JOBS_FILTER,
  externalPlotterCustomerOptions,
  toPlotterJobsQuery,
} from './plotterJobFilters.js'
import { formatPlotterPendingMonthLabel } from './plotterJobPresentation.js'

const customers = [
  { customerId: 'magyen', name: 'Colegio', category: 'MAGYEN' },
  { customerId: 'plotter-1', name: 'Cliente Plotter 1', category: 'PLOTTER' },
  { customerId: 'plotter-2', name: 'Cliente Plotter 2', category: 'PLOTTER' },
  { customerId: 'none', name: 'Sin clasificar', category: 'UNCLASSIFIED' },
]

const period = { fromDate: '2026-09-01', toDate: '2026-09-30' }

test('external plotter options exclude Magyen and unclassified customers', () => {
  assert.deepEqual(
    externalPlotterCustomerOptions(customers).map((customer) => customer.customerId),
    ['plotter-1', 'plotter-2']
  )
})

test('all jobs keeps the date range and does not send a customer or job type', () => {
  assert.deepEqual(toPlotterJobsQuery(ALL_PLOTTER_JOBS_FILTER, period), {
    fromDate: '2026-09-01',
    toDate: '2026-09-30',
  })
})

test('internal Magyen filter uses job type instead of a customer id', () => {
  assert.equal(INTERNAL_MAGYEN_FILTER_LABEL, 'Trabajo interno Magyen')
  assert.deepEqual(toPlotterJobsQuery(INTERNAL_MAGYEN_JOBS_FILTER, period), {
    fromDate: '2026-09-01',
    toDate: '2026-09-30',
    jobType: 'INTERNAL_MAGYEN',
  })
})

test('a plotter customer filter keeps the date range and that customer id', () => {
  assert.deepEqual(toPlotterJobsQuery('plotter-1', period), {
    fromDate: '2026-09-01',
    toDate: '2026-09-30',
    customerId: 'plotter-1',
  })
})

test('pending balances month label uses the job month', () => {
  assert.equal(formatPlotterPendingMonthLabel(2026, 9), 'Septiembre 2026')
  assert.equal(formatPlotterPendingMonthLabel(2026, 8), 'Agosto 2026')
})
