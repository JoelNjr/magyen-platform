import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  QUOTATION_HAS_ASSOCIATED_ORDER_MESSAGE,
  QUOTATION_ORDER_FROZEN_MESSAGE,
  QUOTATION_ORDER_LEGACY_MESSAGE,
  canShowApplyQuotationToOrder,
  getQuotationOrderSynchronizationNotes,
} from './quotationOrderSynchronizationPresentation.js'

test('apply is shown only when the preview allows it', () => {
  assert.equal(canShowApplyQuotationToOrder({ applyAllowed: true }), true)
  assert.equal(canShowApplyQuotationToOrder({ applyAllowed: false }), false)
  assert.equal(canShowApplyQuotationToOrder(null), false)
})

test('associated order always explains that quotation save does not change the order', () => {
  const notes = getQuotationOrderSynchronizationNotes({
    orderExists: true,
    applyAllowed: true,
  })
  assert.equal(notes[0].message, QUOTATION_HAS_ASSOCIATED_ORDER_MESSAGE)
})

test('frozen and legacy orders add a specific explanation and hide apply', () => {
  const frozen = getQuotationOrderSynchronizationNotes({
    orderExists: true,
    applyAllowed: false,
    unavailableReason: 'ORDER_FROZEN',
  })
  assert.equal(frozen.some((note) => note.message === QUOTATION_ORDER_FROZEN_MESSAGE), true)

  const legacy = getQuotationOrderSynchronizationNotes({
    orderExists: true,
    applyAllowed: false,
    legacyUntraced: true,
    unavailableReason: 'LEGACY_UNTRACED',
  })
  assert.equal(legacy.some((note) => note.message === QUOTATION_ORDER_LEGACY_MESSAGE), true)
  assert.equal(getQuotationOrderSynchronizationNotes(null).length, 0)
})
