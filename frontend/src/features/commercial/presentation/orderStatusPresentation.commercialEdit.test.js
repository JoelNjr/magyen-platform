import assert from 'node:assert/strict'
import test from 'node:test'
import { canEditOrderCommercialContent } from './orderStatusPresentation.js'

test('commercial order editing is allowed before delivery', () => {
  assert.equal(canEditOrderCommercialContent('CONFIRMED'), true)
  assert.equal(canEditOrderCommercialContent('IN_PRODUCTION'), true)
  assert.equal(canEditOrderCommercialContent('READY_FOR_DELIVERY'), true)
})

test('commercial order editing is frozen after delivery', () => {
  assert.equal(canEditOrderCommercialContent('DELIVERED'), false)
  assert.equal(canEditOrderCommercialContent('CLOSED'), false)
  assert.equal(canEditOrderCommercialContent(undefined), false)
})
