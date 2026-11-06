import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import {
  buildPayObligationRequest,
  canSubmitObligationPayment,
  defaultObligationPaymentDate,
  isSelectableObligationPaymentDate,
  obligationPeriodLabel,
} from './obligationPaymentPresentation.js'

test('the payment dialog defaults to today and accepts a previous date', () => {
  assert.equal(defaultObligationPaymentDate(new Date(2026, 10, 6)), '2026-11-06')
  assert.equal(isSelectableObligationPaymentDate('2026-11-06'), true)
  assert.equal(isSelectableObligationPaymentDate('2026-10-31'), true)
  assert.equal(isSelectableObligationPaymentDate('2026-09-30'), true)
  assert.equal(isSelectableObligationPaymentDate(''), false)
  assert.equal(isSelectableObligationPaymentDate('2026-02-31'), false)
  assert.equal(obligationPeriodLabel('2026-10-15'), 'Octubre 2026')
  assert.notEqual(obligationPeriodLabel('2026-10-15'), '2026-10-31')
})

test('the selected payment date is the request body', () => {
  assert.deepEqual(buildPayObligationRequest('2026-10-31'), {
    paymentDate: '2026-10-31',
  })
  assert.throws(() => buildPayObligationRequest(''), /valid payment date/)
  assert.throws(() => buildPayObligationRequest('2026-02-31'), /valid payment date/)
})

test('the submit action cannot run twice while a payment is in progress', () => {
  assert.equal(
    canSubmitObligationPayment({ submitting: false, paymentDate: '2026-10-31' }),
    true
  )
  assert.equal(
    canSubmitObligationPayment({ submitting: true, paymentDate: '2026-10-31' }),
    false
  )
  assert.equal(
    canSubmitObligationPayment({ submitting: false, paymentDate: '' }),
    false
  )
})

test('mark as paid opens the date dialog and sends the selected date', () => {
  const page = readFileSync(new URL('../pages/FinancePage.jsx', import.meta.url), 'utf8')
  const dialog = readFileSync(
    new URL('../components/PayObligationOccurrenceDialog.jsx', import.meta.url),
    'utf8'
  )

  assert.match(page, /setPayTarget\(occurrence\)/)
  assert.match(page, /buildPayObligationRequest\(paymentDate\)/)
  assert.match(page, /payRequestLock/)
  assert.equal(page.includes('payObligationOccurrence(occurrence.occurrenceId)'), false)
  assert.match(dialog, /type="date"/)
  assert.match(dialog, /defaultObligationPaymentDate\(\)/)
  assert.match(dialog, /Período de la obligación/)
  assert.match(dialog, /Fecha real de pago/)
  assert.match(dialog, /disabled=\{submitting \|\| !paymentDateValid\}/)
})
