const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

function defaultRandomUUID() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  throw new Error('Plotter payment registration key must be a UUID')
}

export function createPlotterPaymentRegistrationKey(randomUUID = defaultRandomUUID) {
  const value = randomUUID()
  if (typeof value !== 'string' || !UUID_PATTERN.test(value)) {
    throw new Error('Plotter payment registration key must be a UUID')
  }
  return value
}

export function registrationKeyForPaymentAttempt(currentKey, intentionalNewPayment, createKey = createPlotterPaymentRegistrationKey) {
  if (!intentionalNewPayment && currentKey) {
    return currentKey
  }
  return createKey()
}

export function canSubmitPlotterPayment(submitting) {
  return submitting !== true
}

export function registrationKeyAfterPaymentFailure(currentKey) {
  return currentKey
}
