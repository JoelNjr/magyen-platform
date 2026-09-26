import assert from 'node:assert/strict'
import test from 'node:test'
import {
  calendarMonthBoundsFromInput,
  formatPayrollPeriodLabel,
  isFullCalendarPayrollMonth,
} from './financePresentation.js'

test('monthly payroll label uses the calendar month', () => {
  assert.equal(isFullCalendarPayrollMonth('2026-09-01', '2026-09-30'), true)
  assert.equal(isFullCalendarPayrollMonth('2026-02-01', '2026-02-28'), true)
  assert.equal(isFullCalendarPayrollMonth('2028-02-01', '2028-02-29'), true)
  assert.equal(isFullCalendarPayrollMonth('2026-10-01', '2026-10-31'), true)
  assert.equal(isFullCalendarPayrollMonth('2026-09-01', '2026-09-15'), false)
  assert.equal(isFullCalendarPayrollMonth('2026-09-16', '2026-09-30'), false)
  assert.match(formatPayrollPeriodLabel('2026-09-01', '2026-09-30'), /septiembre de 2026/i)
  assert.match(formatPayrollPeriodLabel('2026-08-15', '2026-08-28'), /15\/08\/2026/)
})

test('month input expands to the full calendar month', () => {
  assert.deepEqual(calendarMonthBoundsFromInput('2026-09'), {
    fromDate: '2026-09-01',
    toDate: '2026-09-30',
  })
  assert.deepEqual(calendarMonthBoundsFromInput('2028-02'), {
    fromDate: '2028-02-01',
    toDate: '2028-02-29',
  })
})
