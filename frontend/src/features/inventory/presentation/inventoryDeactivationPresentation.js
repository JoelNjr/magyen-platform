export function hasRemainingPhysicalStock(aggregatedStock) {
  const amount = Number(aggregatedStock)
  return Number.isFinite(amount) && amount > 0
}

export function canDeactivateInventoryMaterial(material) {
  if (!material) {
    return false
  }
  if (material.status === 'INACTIVE') {
    return false
  }
  return !hasRemainingPhysicalStock(material.aggregatedStock)
}
