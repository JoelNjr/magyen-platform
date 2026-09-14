import assert from 'node:assert/strict'
import test from 'node:test'
import {
  getCalendarMonthRange,
  getPreviousCalendarMonthRange,
} from '../../finance/presentation/financePresentation.js'
import {
  HOME_ORDER_VALUE_LABEL,
  HOME_PAGE_SUBTITLE,
  HOME_PERIOD_SUBTITLE,
  HOME_PERIOD_TITLE,
  HOME_PROFITABILITY_BACKEND_CAPTION,
  HOME_PROFITABILITY_SUBTITLE,
  HOME_PROFITABILITY_TITLE,
} from './homeProfitabilityCopy.js'

test('profitability copy uses promised delivery wording, not actual delivery', () => {
  assert.equal(HOME_PROFITABILITY_TITLE, 'Rentabilidad')
  assert.match(HOME_PROFITABILITY_SUBTITLE, /entrega programada/)
  assert.match(HOME_PROFITABILITY_SUBTITLE, /mes seleccionado/)
  assert.doesNotMatch(HOME_PROFITABILITY_SUBTITLE, /entregad[oa]s? en el mes/i)
  assert.match(HOME_PAGE_SUBTITLE, /independiente/)
  assert.match(HOME_PERIOD_SUBTITLE, /resumen financiero/)
  assert.match(HOME_PERIOD_SUBTITLE, /No cambia la rentabilidad/)
  assert.equal(HOME_PERIOD_TITLE, 'Período')
  assert.equal(HOME_ORDER_VALUE_LABEL, 'Valor de pedidos')
  assert.match(HOME_PROFITABILITY_BACKEND_CAPTION, /No se recalculan en el frontend/)
})

test('order value label is committed commercial value, not cash income', () => {
  assert.doesNotMatch(HOME_ORDER_VALUE_LABEL, /ingreso/i)
  assert.doesNotMatch(HOME_ORDER_VALUE_LABEL, /revenue/i)
  assert.doesNotMatch(HOME_ORDER_VALUE_LABEL, /caja/i)
})

test('month navigation helpers stay calendar-month inclusive without calculating profitability', () => {
  const september = getCalendarMonthRange(new Date(2026, 8, 15))
  assert.equal(september.fromDate, '2026-09-01')
  assert.equal(september.toDate, '2026-09-30')

  const previous = getPreviousCalendarMonthRange(new Date(2026, 8, 15))
  assert.equal(previous.fromDate, '2026-08-01')
  assert.equal(previous.toDate, '2026-08-31')
})
