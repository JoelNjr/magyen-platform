import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canOpenExternalPlotterAccount,
  canRegisterExternalPlotterPayment,
  getPlotterJobTypeChipProps,
  groupPlotterJobsByWorkDate,
  isExternalPlotterPaymentComplete,
  isPlotterDateOnlyCorrection,
} from './plotterJobPresentation.js'

test('A. unpaid EXTERNAL job opens the customer account, not a job payment', () => {
  const job = {
    jobType: 'EXTERNAL',
    customerId: 'customer-1',
    totalAmount: 100000,
    paidAmount: 0,
    outstandingAmount: 100000,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(canOpenExternalPlotterAccount(job), true)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('B. partially paid EXTERNAL job still does not expose a job payment', () => {
  const job = {
    jobType: 'EXTERNAL',
    customerId: 'customer-1',
    totalAmount: 100000,
    paidAmount: 40000,
    outstandingAmount: 60000,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(canOpenExternalPlotterAccount(job), true)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('C. a settled EXTERNAL job still opens the account and hides Pago completado', () => {
  const job = {
    jobType: 'EXTERNAL',
    customerId: 'customer-1',
    totalAmount: 100000,
    paidAmount: 100000,
    outstandingAmount: 0,
  }
  assert.equal(canRegisterExternalPlotterPayment(job), false)
  assert.equal(canOpenExternalPlotterAccount(job), true)
  assert.equal(isExternalPlotterPaymentComplete(job), false)
})

test('jobs stay grouped by work date in the order received', () => {
  const groups = groupPlotterJobsByWorkDate([
    { plotterJobId: 'a', creationDate: '2026-08-04' },
    { plotterJobId: 'b', creationDate: '2026-08-04' },
    { plotterJobId: 'c', creationDate: '2026-08-02' },
  ])
  assert.deepEqual(
    groups.map((group) => group.date),
    ['2026-08-04', '2026-08-02']
  )
  assert.equal(groups[0].jobs.length, 2)
})

test('a date-only correction does not look like a commercial edit', () => {
  const job = {
    jobType: 'EXTERNAL',
    creationDate: '2026-08-02',
    paperInventoryItemId: 'roll-1',
    printedMeters: 4,
    pricePerMeter: 1000,
    totalAmount: 5000,
    observations: 'Logo',
  }
  assert.equal(
    isPlotterDateOnlyCorrection(job, {
      creationDate: '2026-08-03',
      paperInventoryItemId: 'roll-1',
      printedMeters: 4,
      pricePerMeter: 1000,
      commercialValue: 5000,
      observations: 'Logo',
    }),
    true
  )
  assert.equal(
    isPlotterDateOnlyCorrection(job, {
      creationDate: '2026-08-03',
      paperInventoryItemId: 'roll-1',
      printedMeters: 4,
      pricePerMeter: 1000,
      commercialValue: 4500,
      observations: 'Logo',
    }),
    false
  )
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

test('plotter job types use distinct semantic chip colors, not gold for all', () => {
  assert.equal(getPlotterJobTypeChipProps('EXTERNAL').color, 'info')
  assert.equal(getPlotterJobTypeChipProps('INTERNAL_MAGYEN').color, 'secondary')
  assert.equal(getPlotterJobTypeChipProps('WASTE').color, 'warning')
  assert.notEqual(getPlotterJobTypeChipProps('EXTERNAL').color, 'primary')
  assert.notEqual(getPlotterJobTypeChipProps('INTERNAL_MAGYEN').color, 'primary')
  assert.notEqual(getPlotterJobTypeChipProps('WASTE').color, 'primary')
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
