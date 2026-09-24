import assert from 'node:assert/strict'
import test from 'node:test'
import {
  calendarMonthBoundsFromInput,
  canPaySellerCommission,
  commissionSettlementLabel,
  selectSellerCommission,
  sellerCommissionAlreadyPaidMessage,
  sellerCommissionHasVariance,
  sellerCommissionPaymentDescription,
  sumCommissionAmounts,
  yearMonthInputValue,
} from './financePresentation.js'

test('commission month input resolves one inclusive calendar month', () => {
  assert.equal(yearMonthInputValue(new Date(2026, 8, 24)), '2026-09')
  assert.deepEqual(calendarMonthBoundsFromInput('2026-09'), {
    fromDate: '2026-09-01',
    toDate: '2026-09-30',
  })
  assert.deepEqual(calendarMonthBoundsFromInput('2026-08'), {
    fromDate: '2026-08-01',
    toDate: '2026-08-31',
  })
})

test('unpaid september can be paid and august cannot', () => {
  const september = {
    settlementStatus: 'CALCULATED',
    accumulatedCommission: 764200,
    displayName: 'Joel David Vasquez',
    numberOfEligibleOrders: 7,
    totalSales: 15284000,
  }
  assert.equal(commissionSettlementLabel('CALCULATED'), 'Comisión calculada')
  assert.equal(canPaySellerCommission(september), true)
  assert.match(
    sellerCommissionPaymentDescription(september, '2026-09-01'),
    /Joel David Vasquez/
  )
  assert.match(
    sellerCommissionPaymentDescription(september, '2026-09-01'),
    /no registra un pago del cliente/
  )

  assert.equal(commissionSettlementLabel('HISTORICAL'), 'Histórico / no pagable')
  assert.equal(canPaySellerCommission({
    settlementStatus: 'HISTORICAL',
    accumulatedCommission: 2202944.45,
  }), false)
})

test('zero commission and paid commission hide the pay action', () => {
  assert.equal(canPaySellerCommission({
    settlementStatus: 'CALCULATED',
    accumulatedCommission: 0,
  }), false)
  assert.equal(canPaySellerCommission({
    settlementStatus: 'PAID',
    accumulatedCommission: 500000,
    paidCommissionSnapshot: 500000,
    actualPaymentDate: '2026-09-30',
  }), false)
  assert.equal(commissionSettlementLabel('PAID'), 'Comisión pagada')
})

test('paid state keeps order lines and shows a variance without paying again', () => {
  const sellers = [
    {
      employeeId: 'seller-1',
      displayName: 'Vendedor',
      settlementStatus: 'PAID',
      accumulatedCommission: 550000,
      paidCommissionSnapshot: 500000,
      actualPaymentDate: '2026-09-30',
      orders: [
        { orderId: 'a', commissionAmount: 300000 },
        { orderId: 'b', commissionAmount: 250000 },
      ],
    },
  ]
  const selected = selectSellerCommission(sellers, 'seller-1')
  assert.equal(selected.orders.length, 2)
  assert.equal(sumCommissionAmounts(selected.orders), 550000)
  assert.equal(sellerCommissionHasVariance(selected), true)
  assert.equal(canPaySellerCommission(selected), false)
})

test('already paid message is the conflict copy', () => {
  assert.equal(
    sellerCommissionAlreadyPaidMessage(),
    'La comisión de este vendedor para este mes ya fue pagada.'
  )
})
