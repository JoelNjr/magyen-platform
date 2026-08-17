import httpClient from '../../../services/httpClient'

export async function getInventoryItems(params = {}) {
  const response = await httpClient.get('/inventory', { params })
  return response.data
}

export async function getPlotterPaperRolls() {
  return getInventoryItems({ plotterPaperRoll: true })
}

export async function getInventoryItem(inventoryItemId) {
  const response = await httpClient.get(`/inventory/${inventoryItemId}`)
  return response.data
}

export async function getInventoryMovements(inventoryItemId) {
  const response = await httpClient.get(`/inventory/${inventoryItemId}/movements`)
  return response.data
}

export async function createInventoryItem(payload) {
  const response = await httpClient.post('/inventory', payload)
  return response.data
}

export async function updateInventoryMinimumStock(inventoryItemId, minimumStock) {
  const response = await httpClient.patch(
    `/inventory/${inventoryItemId}/minimum-stock`,
    { minimumStock }
  )
  return response.data
}

export async function updateInventoryUnitCost(inventoryItemId, unitCost) {
  const response = await httpClient.patch(
    `/inventory/${inventoryItemId}/unit-cost`,
    { unitCost }
  )
  return response.data
}

export async function registerInventoryPurchase(inventoryItemId, payload) {
  const response = await httpClient.post(
    `/inventory/${inventoryItemId}/purchases`,
    payload
  )
  return response.data
}

export async function registerInventoryMovement(inventoryItemId, payload) {
  const response = await httpClient.post(
    `/inventory/${inventoryItemId}/movements`,
    payload
  )
  return response.data
}
