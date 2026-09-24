import assert from 'node:assert/strict'
import test from 'node:test'
import {
  calendarMonthBoundsFromInput,
  commissionPaymentAvailable,
  commissionSettlementLabel,
  selectSellerCommission,
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

test('september is calculated and august is historical and not payable', () => {
  assert.equal(commissionSettlementLabel('CALCULATED'), 'Comisión calculada')
  assert.equal(commissionSettlementLabel('HISTORICAL'), 'Histórico / no pagable')
  assert.equal(commissionPaymentAvailable(), false)
})

test('selecting a seller shows monthly lines that reconcile to the commission', () => {
  const sellers = [
    {
      employeeId: 'joel-david',
      displayName: 'Joel David Vasquez',
      numberOfEligibleOrders: 2,
      totalSales: 1000,
      accumulatedCommission: 50,
      settlementStatus: 'CALCULATED',
      orders: [
        { orderId: 'a', orderTotal: 600, commissionAmount: 30 },
        { orderId: 'b', orderTotal: 400, commissionAmount: 20 },
      ],
    },
  ]

  const selected = selectSellerCommission(sellers, 'joel-david')
  assert.equal(selected.displayName, 'Joel David Vasquez')
  assert.equal(selected.orders.length, 2)
  assert.equal(sumCommissionAmounts(selected.orders), selected.accumulatedCommission)
  assert.equal(selectSellerCommission(sellers, 'missing'), null)
  assert.equal(commissionPaymentAvailable(), false)
})
