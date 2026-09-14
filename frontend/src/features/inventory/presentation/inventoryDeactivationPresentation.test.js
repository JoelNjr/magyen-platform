import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canDeactivateInventoryMaterial,
  hasRemainingPhysicalStock,
} from './inventoryDeactivationPresentation.js'

test('active material with zero stock can be deactivated', () => {
  assert.equal(
    canDeactivateInventoryMaterial({ status: 'ACTIVE', aggregatedStock: '0.0000' }),
    true
  )
})

test('remaining stock blocks deactivation', () => {
  assert.equal(hasRemainingPhysicalStock('8.0000'), true)
  assert.equal(
    canDeactivateInventoryMaterial({ status: 'ACTIVE', aggregatedStock: 8 }),
    false
  )
})

test('inactive material is not offered for deactivation again', () => {
  assert.equal(
    canDeactivateInventoryMaterial({ status: 'INACTIVE', aggregatedStock: 0 }),
    false
  )
})
