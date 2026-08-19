import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canRegisterExternalPlotterPayment,
  isExternalPlotterPaymentComplete,
} from './plotterJobPresentation.js'

test('A. unpaid EXTERNAL job exposes Registrar pago', () => {
  const job = {
    jobType: 'EXTERNAL',
    totalAmount: 100000,
    paidAmount: 0,
    outstandingAmount: 100000,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), true)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('B. partially paid EXTERNAL job still exposes Registrar pago', () => {
  const job = {
    jobType: 'EXTERNAL',
    totalAmount: 100000,
    paidAmount: 40000,
    outstandingAmount: 60000,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), true)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('C. fully paid EXTERNAL job shows Pago completado', () => {
  const job = {
    jobType: 'EXTERNAL',
    totalAmount: 100000,
    paidAmount: 100000,
    outstandingAmount: 0,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(isExternalPlotterPaymentComplete(job), true)
})

test('I. INTERNAL jobs do not expose external payment actions', () => {
  const job = {
    jobType: 'INTERNAL_MAGYEN',
    totalAmount: 48000,
    paidAmount: 0,
    outstandingAmount: 0,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('J. WASTE jobs do not expose payment actions', () => {
  const job = {
    jobType: 'WASTE',
    totalAmount: 0,
    paidAmount: 0,
    outstandingAmount: 0,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})
