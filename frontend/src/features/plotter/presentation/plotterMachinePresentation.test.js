import assert from 'node:assert/strict'
import test from 'node:test'
import {
  UNIDENTIFIED_PLOTTER_MACHINE_LABEL,
  defaultPlotterMachineId,
  eligiblePaperRolls,
  formatPlotterMachineLabel,
  rollAfterMachineChange,
  toPlotterMachineQuery,
} from './plotterMachinePresentation.js'

const plotter1 = '6f0c1a10-0001-4a01-8a01-000000000001'
const plotter2 = '6f0c1a10-0002-4a02-8a02-000000000002'

const machines = [
  { plotterMachineId: plotter1, name: 'Plotter 1', active: true, defaultForRegistration: true },
  { plotterMachineId: plotter2, name: 'Plotter 2', active: true, defaultForRegistration: false },
  { plotterMachineId: 'retired', name: 'Plotter retirada', active: false, defaultForRegistration: false },
]

const rolls = [
  { inventoryItemId: 'roll-1', plotterMachineId: plotter1, stock: '12.0000' },
  { inventoryItemId: 'roll-2', plotterMachineId: plotter2, stock: '8.0000' },
  { inventoryItemId: 'roll-empty', plotterMachineId: plotter1, stock: '0.0000' },
  { inventoryItemId: 'roll-open', plotterMachineId: null, stock: '20.0000' },
]

test('default machine is the active registration default', () => {
  assert.equal(defaultPlotterMachineId(machines), plotter1)
  assert.equal(defaultPlotterMachineId([{ ...machines[0], active: false }]), '')
})

test('history without a machine is unidentified', () => {
  assert.equal(formatPlotterMachineLabel(null, null), UNIDENTIFIED_PLOTTER_MACHINE_LABEL)
  assert.equal(formatPlotterMachineLabel(plotter1, 'Plotter 1'), 'Plotter 1')
})

test('roll selector keeps only stocked rolls of the selected machine', () => {
  assert.deepEqual(
    eligiblePaperRolls(rolls, plotter1).map((roll) => roll.inventoryItemId),
    ['roll-1']
  )
  assert.deepEqual(
    eligiblePaperRolls(rolls, plotter2).map((roll) => roll.inventoryItemId),
    ['roll-2']
  )
  assert.deepEqual(eligiblePaperRolls(rolls, ''), [])
})

test('changing machine clears an incompatible roll', () => {
  assert.equal(rollAfterMachineChange('roll-1', rolls, plotter2), '')
  assert.equal(rollAfterMachineChange('roll-2', rolls, plotter2), 'roll-2')
  assert.equal(rollAfterMachineChange('roll-open', rolls, plotter1), '')
})

test('machine filter preserves an explicit machine or the unidentified bucket', () => {
  assert.deepEqual(toPlotterMachineQuery(''), {})
  assert.deepEqual(toPlotterMachineQuery(plotter2), { machineId: plotter2 })
  assert.deepEqual(toPlotterMachineQuery('UNASSIGNED'), { unassignedMachine: true })
})
