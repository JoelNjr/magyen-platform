import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canCompleteProductionOrder,
  canConsumeProductionMaterial,
  canExecuteProductionOperations,
  canRegisterProductionLabor,
  canRegisterProductionOtherCost,
} from './productionStatusPresentation.js'

test('other cost action is available in IN_PROGRESS and COMPLETED only', () => {
  assert.equal(canRegisterProductionOtherCost('IN_PROGRESS'), true)
  assert.equal(canRegisterProductionOtherCost('COMPLETED'), true)
  assert.equal(canRegisterProductionOtherCost('CREATED'), false)
  assert.equal(canRegisterProductionOtherCost('PLANNED'), false)
})

test('other cost availability does not unlock labor, consumption, operations or completion', () => {
  assert.equal(canRegisterProductionLabor('COMPLETED'), false)
  assert.equal(canConsumeProductionMaterial('COMPLETED'), false)
  assert.equal(canExecuteProductionOperations('COMPLETED'), false)
  assert.equal(canCompleteProductionOrder('COMPLETED'), false)

  assert.equal(canRegisterProductionLabor('IN_PROGRESS'), true)
  assert.equal(canConsumeProductionMaterial('IN_PROGRESS'), true)
  assert.equal(canExecuteProductionOperations('IN_PROGRESS'), true)
  assert.equal(canCompleteProductionOrder('IN_PROGRESS'), true)

  assert.equal(canRegisterProductionLabor('CREATED'), false)
  assert.equal(canConsumeProductionMaterial('PLANNED'), false)
})
