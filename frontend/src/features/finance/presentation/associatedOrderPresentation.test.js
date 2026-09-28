import assert from 'node:assert/strict'
import test from 'node:test'
import {
  NO_ASSOCIATED_ORDER_LABEL,
  associatedOrderSubmitValue,
  formatAssociatedOrderLabel,
} from './associatedOrderPresentation.js'

test('empty selection is sin pedido and is omitted from the payload', () => {
  assert.equal(formatAssociatedOrderLabel(null), NO_ASSOCIATED_ORDER_LABEL)
  assert.equal(formatAssociatedOrderLabel({ orderId: '' }), NO_ASSOCIATED_ORDER_LABEL)
  assert.equal(associatedOrderSubmitValue('', 'EXPENSE'), null)
  assert.equal(associatedOrderSubmitValue(null, 'EXPENSE'), null)
})

test('an order option shows number and customer', () => {
  assert.equal(
    formatAssociatedOrderLabel({
      orderId: 'abc',
      orderNumber: '123',
      customerName: 'Colegio San José',
    }),
    'Pedido #123 — Colegio San José'
  )
})

test('income does not submit an associated order', () => {
  assert.equal(associatedOrderSubmitValue('abc', 'INCOME'), null)
})

test('an expense can submit the selected order', () => {
  assert.equal(associatedOrderSubmitValue('abc', 'EXPENSE'), 'abc')
})
