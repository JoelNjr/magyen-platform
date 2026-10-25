import assert from 'node:assert/strict'
import test from 'node:test'
import {
  buildUpdatePromisedDeliveryDateRequest,
  buildUpdateQuotationDeliveryDateRequest,
  validatePromisedDeliveryDate,
  validateQuotationDeliveryDate,
} from './deliveryDateEditingPresentation.js'

test('quotation delivery date is required and cannot precede creation', () => {
  assert.equal(validateQuotationDeliveryDate('', '2026-08-01'), 'La fecha de entrega es obligatoria.')
  assert.equal(
    validateQuotationDeliveryDate('2026-07-31', '2026-08-01'),
    'La fecha de entrega no puede ser anterior a la fecha de creación.'
  )
  assert.equal(validateQuotationDeliveryDate('2026-08-01', '2026-08-01'), '')
  assert.equal(validateQuotationDeliveryDate('2026-08-02', '2026-08-01'), '')
})

test('quotation delivery date may be in the past when it is not before creation', () => {
  assert.equal(validateQuotationDeliveryDate('2026-08-15', '2026-08-01'), '')
})

test('promised delivery date is distinct from the actual delivery date payload', () => {
  assert.equal(
    validatePromisedDeliveryDate('', '2026-08-10'),
    'La fecha de entrega programada es obligatoria.'
  )
  assert.equal(
    validatePromisedDeliveryDate('2026-08-09', '2026-08-10'),
    'La fecha de entrega programada no puede ser anterior a la fecha de confirmación.'
  )
  assert.equal(validatePromisedDeliveryDate('2026-08-10', '2026-08-10'), '')
  assert.equal(validatePromisedDeliveryDate('2026-09-01', '2026-08-10'), '')
  assert.deepEqual(buildUpdateQuotationDeliveryDateRequest('2026-08-20'), {
    deliveryDate: '2026-08-20',
  })
  assert.deepEqual(buildUpdatePromisedDeliveryDateRequest('2026-09-02'), {
    promisedDeliveryDate: '2026-09-02',
  })
})
