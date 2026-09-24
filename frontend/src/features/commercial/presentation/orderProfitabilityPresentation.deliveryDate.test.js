import assert from 'node:assert/strict'
import test from 'node:test'
import { formatOrderProfitabilityDeliveryCaption } from './orderProfitabilityPresentation.js'
import {
  canDeliverOrder,
  canMarkOrderReadyForDelivery,
  canStartOrderProduction,
} from './orderStatusPresentation.js'

test('delivery caption distinguishes actual vs historical fallback vs current month', () => {
  assert.equal(
    formatOrderProfitabilityDeliveryCaption({
      deliveryDateSource: 'ACTUAL',
      actualDeliveryDate: '2026-09-18',
      promisedDeliveryDate: '2026-09-04',
    }),
    'Entrega real: 18/09/2026'
  )
  assert.equal(
    formatOrderProfitabilityDeliveryCaption({
      deliveryDateSource: 'HISTORICAL_FALLBACK',
      actualDeliveryDate: null,
      promisedDeliveryDate: '2026-09-14',
    }),
    'Entrega programada: 14/09/2026'
  )
  assert.equal(
    formatOrderProfitabilityDeliveryCaption({
      deliveryDateSource: 'CURRENT_MONTH',
      promisedDeliveryDate: '2026-09-25',
    }),
    'En curso (mes actual)'
  )
})

test('delivery action is only available for READY_FOR_DELIVERY', () => {
  assert.equal(canStartOrderProduction('CONFIRMED'), true)
  assert.equal(canMarkOrderReadyForDelivery('IN_PRODUCTION'), true)
  assert.equal(canDeliverOrder('READY_FOR_DELIVERY'), true)
  assert.equal(canDeliverOrder('CONFIRMED'), false)
  assert.equal(canDeliverOrder('IN_PRODUCTION'), false)
  assert.equal(canDeliverOrder('DELIVERED'), false)
})
