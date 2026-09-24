import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canCloseOrder,
  canCreateProductionOrder,
  canDeliverOrder,
  canMarkOrderReadyForDelivery,
  canStartOrderProduction,
} from './orderStatusPresentation.js'
import {
  buildDeliverOrderRequest,
  validateDeliveryDate,
} from './orderLifecyclePresentation.js'

test('lifecycle button is only the next commercial action', () => {
  assert.equal(canStartOrderProduction('CONFIRMED'), true)
  assert.equal(canMarkOrderReadyForDelivery('CONFIRMED'), false)
  assert.equal(canDeliverOrder('CONFIRMED'), false)
  assert.equal(canCloseOrder('CONFIRMED'), false)

  assert.equal(canStartOrderProduction('IN_PRODUCTION'), false)
  assert.equal(canMarkOrderReadyForDelivery('IN_PRODUCTION'), true)
  assert.equal(canDeliverOrder('IN_PRODUCTION'), false)

  assert.equal(canMarkOrderReadyForDelivery('READY_FOR_DELIVERY'), false)
  assert.equal(canDeliverOrder('READY_FOR_DELIVERY'), true)
  assert.equal(canCloseOrder('READY_FOR_DELIVERY'), false)

  assert.equal(canDeliverOrder('DELIVERED'), false)
  assert.equal(canCloseOrder('DELIVERED'), true)

  assert.equal(canCloseOrder('CLOSED'), false)
  assert.equal(canStartOrderProduction('CLOSED'), false)
})

test('production creation stays available before ready for delivery', () => {
  assert.equal(canCreateProductionOrder('CONFIRMED', false), true)
  assert.equal(canCreateProductionOrder('IN_PRODUCTION', false), true)
  assert.equal(canCreateProductionOrder('CONFIRMED', true), false)
  assert.equal(canCreateProductionOrder('IN_PRODUCTION', true), false)
  assert.equal(canCreateProductionOrder('READY_FOR_DELIVERY', false), false)
  assert.equal(canCreateProductionOrder('DELIVERED', false), false)
  assert.equal(canCreateProductionOrder('CLOSED', false), false)
})

test('delivery date validation and request body', () => {
  assert.equal(validateDeliveryDate('', '2026-09-01', '2026-09-23'), 'La fecha de entrega es obligatoria.')
  assert.match(
    validateDeliveryDate('2026-08-31', '2026-09-01', '2026-09-23'),
    /confirmación/
  )
  assert.match(
    validateDeliveryDate('2026-09-24', '2026-09-01', '2026-09-23'),
    /posterior/
  )
  assert.equal(validateDeliveryDate('2026-09-01', '2026-09-01', '2026-09-23'), '')
  assert.equal(validateDeliveryDate('2026-08-05', '2026-08-02', '2026-09-23'), '')
  assert.deepEqual(buildDeliverOrderRequest('2026-09-18'), { deliveryDate: '2026-09-18' })
})
