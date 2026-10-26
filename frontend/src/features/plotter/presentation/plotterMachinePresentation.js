export const UNIDENTIFIED_PLOTTER_MACHINE_LABEL = 'Sin identificar'

export function activePlotterMachines(machines) {
  return (machines || []).filter((machine) => machine.active)
}

export function defaultPlotterMachineId(machines) {
  const active = activePlotterMachines(machines)
  const preferred = active.find((machine) => machine.defaultForRegistration)
  return preferred?.plotterMachineId || ''
}

export function formatPlotterMachineLabel(machineId, machineName) {
  if (!machineId) {
    return UNIDENTIFIED_PLOTTER_MACHINE_LABEL
  }
  return machineName || UNIDENTIFIED_PLOTTER_MACHINE_LABEL
}

export function eligiblePaperRolls(paperRolls, plotterMachineId) {
  if (!plotterMachineId) {
    return []
  }
  return (paperRolls || []).filter((roll) => {
    const stock = Number(roll.stock)
    return roll.plotterMachineId === plotterMachineId && !Number.isNaN(stock) && stock > 0
  })
}

export function rollAfterMachineChange(currentRollId, paperRolls, plotterMachineId) {
  if (!currentRollId) {
    return ''
  }
  const stillEligible = eligiblePaperRolls(paperRolls, plotterMachineId)
    .some((roll) => roll.inventoryItemId === currentRollId)
  return stillEligible ? currentRollId : ''
}

export function toPlotterMachineQuery(machineFilter) {
  if (!machineFilter) {
    return {}
  }
  if (machineFilter === 'UNASSIGNED') {
    return { unassignedMachine: true }
  }
  return { machineId: machineFilter }
}
