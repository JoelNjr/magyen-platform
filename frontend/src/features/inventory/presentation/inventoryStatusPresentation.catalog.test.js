import assert from 'node:assert/strict'
import test from 'node:test'
import {
  formatCatalogMinimumStockLabel,
  formatCatalogUnitCostLabel,
  formatPhysicalUnitCount,
  getInventoryCatalogTitle,
  getInventoryMaterialTitle,
  toPurchaseSelectableCatalogItem,
} from './inventoryStatusPresentation.js'

test('catalog title uses material type and code, never the roll number', () => {
  assert.equal(
    getInventoryCatalogTitle({
      materialCode: 'MAT-001',
      materialType: 'PAPER',
      name: 'Papel Plotter',
      paperRollNumber: 'RP-002',
      plotterPaperRoll: true,
    }),
    'Papel MAT-001'
  )
  assert.equal(
    getInventoryCatalogTitle({
      materialCode: 'MAT-004',
      materialType: 'INK',
      name: 'Tinta cian',
    }),
    'Tinta MAT-004'
  )
})

test('unit title still prefers the physical roll number', () => {
  assert.equal(
    getInventoryMaterialTitle({
      materialCode: 'MAT-001',
      name: 'Papel Plotter',
      paperRollNumber: 'RP-002',
      plotterPaperRoll: true,
    }),
    'Rollo RP-002'
  )
})

test('physical unit count is shown only for paper materials', () => {
  assert.equal(
    formatPhysicalUnitCount({ paperMaterial: true, physicalUnitCount: 2 }),
    '2 rollos'
  )
  assert.equal(
    formatPhysicalUnitCount({ paperMaterial: true, physicalUnitCount: 1 }),
    '1 rollo'
  )
  assert.equal(
    formatPhysicalUnitCount({ paperMaterial: false, physicalUnitCount: 1 }),
    null
  )
})

test('catalog cost is omitted when units do not share the same cost', () => {
  assert.equal(
    formatCatalogUnitCostLabel({
      unitCostUniform: false,
      unitCost: 100,
      unitOfMeasure: 'METER',
    }),
    'Costos distintos'
  )
  assert.match(
    formatCatalogUnitCostLabel({
      unitCostUniform: true,
      unitCost: 100,
      unitOfMeasure: 'METER',
    }),
    /\$100,00 \/ m/
  )
})

test('catalog minimum stock is omitted when units differ', () => {
  assert.equal(
    formatCatalogMinimumStockLabel({
      minimumStockUniform: false,
      minimumStock: 5,
      unitOfMeasure: 'METER',
    }),
    '—'
  )
  assert.equal(
    formatCatalogMinimumStockLabel({
      minimumStockUniform: true,
      minimumStock: 5,
      unitOfMeasure: 'METER',
    }),
    '5,00 m'
  )
})

test('purchase selectable items exist only for non-paper catalog rows', () => {
  assert.equal(
    toPurchaseSelectableCatalogItem({
      paperMaterial: true,
      stockHoldingItemId: null,
      materialCode: 'MAT-001',
    }),
    null
  )

  const ink = toPurchaseSelectableCatalogItem({
    stockHoldingItemId: 'unit-ink',
    materialCode: 'MAT-004',
    materialType: 'INK',
    aggregatedStock: 8,
    unitOfMeasure: 'LITER',
    unitCost: 25000,
  })
  assert.equal(ink.inventoryItemId, 'unit-ink')
  assert.equal(ink.stock, 8)
  assert.equal(ink.name, 'Tinta MAT-004')
})
