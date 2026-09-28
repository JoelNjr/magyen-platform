import assert from 'node:assert/strict'
import test from 'node:test'
import {
  applyOrderStatusToList,
  canSubmitOrderStatusChange,
  hasNextOrderStatus,
  orderStatusChangeErrorMessage,
  orderStatusChangeOptions,
  orderStatusChangeSuccessMessage,
  resolveOrderStatusSelection,
} from './orderListStatusChange.js'

test('list offers only the current status and the next legal transition', () => {
  assert.deepEqual(
    orderStatusChangeOptions('CONFIRMED').map((option) => option.status),
    ['CONFIRMED', 'IN_PRODUCTION']
  )
  assert.deepEqual(
    orderStatusChangeOptions('IN_PRODUCTION').map((option) => option.status),
    ['IN_PRODUCTION', 'READY_FOR_DELIVERY']
  )
  assert.deepEqual(
    orderStatusChangeOptions('READY_FOR_DELIVERY').map((option) => option.status),
    ['READY_FOR_DELIVERY', 'DELIVERED']
  )
  assert.deepEqual(
    orderStatusChangeOptions('DELIVERED').map((option) => option.status),
    ['DELIVERED', 'CLOSED']
  )
  assert.deepEqual(
    orderStatusChangeOptions('CLOSED').map((option) => option.status),
    ['CLOSED']
  )
  assert.equal(hasNextOrderStatus('CLOSED'), false)
  assert.equal(hasNextOrderStatus('CONFIRMED'), true)
})

test('selecting the next status uses the existing transition', () => {
  assert.deepEqual(resolveOrderStatusSelection('CONFIRMED', 'IN_PRODUCTION'), {
    type: 'transition',
    status: 'IN_PRODUCTION',
    action: 'start-production',
  })
  assert.deepEqual(resolveOrderStatusSelection('IN_PRODUCTION', 'READY_FOR_DELIVERY'), {
    type: 'transition',
    status: 'READY_FOR_DELIVERY',
    action: 'ready-for-delivery',
  })
  assert.deepEqual(resolveOrderStatusSelection('DELIVERED', 'CLOSED'), {
    type: 'transition',
    status: 'CLOSED',
    action: 'close',
  })
  assert.equal(resolveOrderStatusSelection('CONFIRMED', 'CONFIRMED').type, 'none')
})

test('delivery still requires the existing delivery date instead of changing immediately', () => {
  assert.deepEqual(resolveOrderStatusSelection('READY_FOR_DELIVERY', 'DELIVERED'), {
    type: 'requires-delivery-date',
    status: 'DELIVERED',
    action: 'deliver',
  })
})

test('skipping a status or moving backwards stays invalid', () => {
  assert.equal(resolveOrderStatusSelection('CONFIRMED', 'DELIVERED').type, 'invalid')
  assert.equal(resolveOrderStatusSelection('CONFIRMED', 'CLOSED').type, 'invalid')
  assert.equal(resolveOrderStatusSelection('IN_PRODUCTION', 'CONFIRMED').type, 'invalid')
  assert.equal(resolveOrderStatusSelection('CLOSED', 'DELIVERED').type, 'invalid')
})

test('a confirmed change replaces only that order status in the list', () => {
  const orders = [
    { orderId: 'a', status: 'CONFIRMED' },
    { orderId: 'b', status: 'IN_PRODUCTION' },
  ]
  assert.deepEqual(applyOrderStatusToList(orders, 'a', 'IN_PRODUCTION'), [
    { orderId: 'a', status: 'IN_PRODUCTION' },
    { orderId: 'b', status: 'IN_PRODUCTION' },
  ])
})

test('a row cannot submit another change while one is in progress', () => {
  assert.equal(canSubmitOrderStatusChange(null, 'a'), true)
  assert.equal(canSubmitOrderStatusChange('a', 'a'), false)
  assert.equal(canSubmitOrderStatusChange('a', 'b'), false)
})

test('backend error message is shown and success copy follows the new status', () => {
  assert.equal(
    orderStatusChangeErrorMessage(
      { response: { data: { message: 'Collected payments do not cover the order total.' } } },
      'No fue posible actualizar el estado del pedido.'
    ),
    'Collected payments do not cover the order total.'
  )
  assert.equal(
    orderStatusChangeErrorMessage({}, 'No fue posible actualizar el estado del pedido.'),
    'No fue posible actualizar el estado del pedido.'
  )
  assert.equal(orderStatusChangeSuccessMessage('IN_PRODUCTION'), 'Pedido marcado en producción.')
  assert.equal(orderStatusChangeSuccessMessage('CLOSED'), 'Pedido cerrado.')
})
