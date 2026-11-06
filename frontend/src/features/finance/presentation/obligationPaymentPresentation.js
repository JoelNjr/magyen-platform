import { formatMonthPeriodLabel } from '../../../shared/period/monthPeriod.js'
import { toIsoDate } from './financePresentation.js'

export function defaultObligationPaymentDate(referenceDate = new Date()) {
  return toIsoDate(referenceDate)
}

export function obligationPeriodLabel(dueDate) {
  return formatMonthPeriodLabel(dueDate)
}

export function isSelectableObligationPaymentDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value ?? '')) {
    return false
  }
  const [year, month, day] = value.split('-').map(Number)
  const parsed = new Date(year, month - 1, day)
  return parsed.getFullYear() === year
    && parsed.getMonth() === month - 1
    && parsed.getDate() === day
}

export function canSubmitObligationPayment({ submitting, paymentDate }) {
  return submitting !== true && isSelectableObligationPaymentDate(paymentDate)
}

export function buildPayObligationRequest(paymentDate) {
  if (!isSelectableObligationPaymentDate(paymentDate)) {
    throw new Error('A valid payment date is required')
  }
  return { paymentDate }
}
