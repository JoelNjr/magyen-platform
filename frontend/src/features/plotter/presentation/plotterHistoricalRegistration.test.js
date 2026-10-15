import assert from 'node:assert/strict'
import test from 'node:test'
import {
  formatHistoricalPlotterOrderOption,
  isEligibleHistoricalPlotterOrderStatus,
  parseHistoricalOrderMonth,
  plotterOrdersForRegistration,
  withHistoricalPlotterRegistration,
} from './plotterJobPresentation.js'

const openOrder = {
  orderId: 'open-1',
  orderNumber: '12',
  description: 'Pedido abierto',
  customerName: 'Colegio Norte',
  confirmationDate: '2026-10-02',
  status: 'IN_PRODUCTION',
}

const closedOrder = {
  orderId: 'closed-1',
  orderNumber: '8',
  description: 'Banderas septiembre',
  customerName: 'Colegio Sur',
  confirmationDate: '2026-09-05',
  status: 'CLOSED',
}

const deliveredOrder = {
  orderId: 'delivered-1',
  orderNumber: '9',
  description: 'Pendones',
  customerName: 'Colegio Centro',
  confirmationDate: '2026-09-11',
  status: 'DELIVERED',
}

test('normal registration keeps closed orders out of the selector', () => {
  const selectable = plotterOrdersForRegistration(
    [openOrder, closedOrder, deliveredOrder],
    [closedOrder, deliveredOrder],
    false
  )

  assert.deepEqual(selectable.map((order) => order.orderId), ['open-1'])
  assert.equal(isEligibleHistoricalPlotterOrderStatus('CLOSED'), true)
  assert.equal(isEligibleHistoricalPlotterOrderStatus('IN_PRODUCTION'), false)
})

test('historical registration selects only delivered or closed orders from the searched month', () => {
  assert.deepEqual(parseHistoricalOrderMonth('2026-09'), { year: 2026, month: 9 })
  assert.equal(parseHistoricalOrderMonth(''), null)
  assert.equal(parseHistoricalOrderMonth('2026-13'), null)

  const selectable = plotterOrdersForRegistration(
    [openOrder],
    [openOrder, closedOrder, deliveredOrder],
    true
  )

  assert.deepEqual(selectable.map((order) => order.orderId), ['closed-1', 'delivered-1'])
  const label = formatHistoricalPlotterOrderOption(closedOrder)
  assert.match(label, /^#8 — Banderas septiembre — Colegio Sur — .+ — Cerrada$/)
})

test('historical submission keeps the explicit work date and does not mark ordinary jobs', () => {
  const ordinary = {
    jobType: 'INTERNAL_MAGYEN',
    orderId: 'open-1',
    creationDate: '2026-10-14',
    printedMeters: 2,
    pricePerMeter: 5000,
  }
  const historical = withHistoricalPlotterRegistration(
    {
      jobType: 'INTERNAL_MAGYEN',
      orderId: 'closed-1',
      creationDate: '2026-09-12',
      printedMeters: 2,
      pricePerMeter: 5000,
    },
    true
  )

  assert.equal(ordinary.historicalRegistration, undefined)
  assert.equal(withHistoricalPlotterRegistration(ordinary, false).historicalRegistration, undefined)
  assert.equal(historical.historicalRegistration, true)
  assert.equal(historical.creationDate, '2026-09-12')
  assert.equal(historical.orderId, 'closed-1')
})
