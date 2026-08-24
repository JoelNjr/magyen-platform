import assert from 'node:assert/strict'
import test from 'node:test'
import {
  formatQuotationNumber,
  formatReservedOrderNumber,
} from './formatQuotationNumber.js'

test('formats quotation consecutive as commercial display code', () => {
  assert.equal(formatQuotationNumber(14), 'C000014')
})

test('derives reserved order number from the quotation consecutive', () => {
  assert.equal(formatReservedOrderNumber(14), '14')
  assert.equal(formatReservedOrderNumber('14'), '14')
})

test('reserved order number is empty when the quotation has no consecutive', () => {
  assert.equal(formatReservedOrderNumber(null), null)
  assert.equal(formatReservedOrderNumber(0), null)
})
