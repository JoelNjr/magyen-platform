import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import {
  canSubmitPlotterPayment,
  registrationKeyAfterPaymentFailure,
  registrationKeyForPaymentAttempt,
} from './plotterPaymentSubmission.js'

test('a retry of the same payment keeps the registration key', () => {
  const keys = ['6f0c1a10-0001-4a01-8a01-000000000001', '6f0c1a10-0002-4a02-8a02-000000000002']
  let index = 0
  const createKey = () => keys[index++]
  const first = registrationKeyForPaymentAttempt(null, true, createKey)

  assert.equal(registrationKeyForPaymentAttempt(first, false, createKey), first)
  assert.equal(registrationKeyAfterPaymentFailure(first), first)
  assert.equal(index, 1)
})

test('a new intentional payment receives a new registration key', () => {
  const keys = ['6f0c1a10-0001-4a01-8a01-000000000001', '6f0c1a10-0002-4a02-8a02-000000000002']
  let index = 0
  const createKey = () => keys[index++]
  const first = registrationKeyForPaymentAttempt(null, true, createKey)
  const second = registrationKeyForPaymentAttempt(first, true, createKey)

  assert.notEqual(second, first)
  assert.equal(second, keys[1])
})

test('a payment cannot be submitted again while the request is in progress', () => {
  assert.equal(canSubmitPlotterPayment(false), true)
  assert.equal(canSubmitPlotterPayment(true), false)
})

test('the legacy per-job payment action is not available', () => {
  const source = readFileSync(new URL('../services/plotterService.js', import.meta.url), 'utf8')

  assert.equal(source.includes('registerPlotterPayment'), false)
  assert.equal(
    /httpClient\.post\(\s*`\/plotter\/jobs\/\$\{plotterJobId\}\/payments`/.test(source),
    false
  )
  assert.match(source, /get\(`\/plotter\/jobs\/\$\{plotterJobId\}\/payments`\)/)
  assert.match(source, /\/plotter\/customer-accounts\/\$\{customerId\}\/payments/)
})
