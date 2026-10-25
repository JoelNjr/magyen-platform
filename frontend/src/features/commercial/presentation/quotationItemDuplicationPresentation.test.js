import assert from 'node:assert/strict'
import test from 'node:test'
import {
  duplicatedItemPreservesBusinessFields,
  findQuotationItemById,
  quotationTotalAfterDiscount,
  replaceQuotationItem,
} from './quotationItemDuplicationPresentation.js'

const original = {
  itemId: 'item-1',
  productName: 'Camiseta',
  quantity: 10,
  fabric: 'Hydrotech',
  secondaryFabric: 'Perchada',
  color: 'Blanco',
  unitPrice: 20000,
  subtotal: 200000,
  productSpecification: {
    garmentType: 'Camiseta',
    collarType: 'Redondo',
    sleeveType: 'Manga corta sisa',
    cuffRequired: true,
    sublimationRequired: true,
    embroideryRequired: false,
    dtfRequired: false,
    decorationNotes: 'Frente',
    includesNames: true,
    includesNumbers: false,
    includesLogos: true,
    personalizationNotes: 'Espalda',
    itemObservations: 'Logo pequeño',
  },
}

test('a duplicated quotation item is a new row with the same business fields', () => {
  const duplicate = {
    ...original,
    itemId: 'item-2',
    subtotal: 200000,
  }
  const items = [original, duplicate]

  assert.equal(duplicatedItemPreservesBusinessFields(original, duplicate), true)
  assert.equal(findQuotationItemById(items, 'item-2')?.itemId, 'item-2')
  assert.equal(findQuotationItemById(items, 'missing'), null)
})

test('editing the duplicate recalculates only that row and keeps the original', () => {
  const duplicate = { ...original, itemId: 'item-2' }
  const edited = replaceQuotationItem([original, duplicate], 'item-2', {
    quantity: 4,
    unitPrice: 25000,
  })

  assert.equal(edited[0].itemId, 'item-1')
  assert.equal(edited[0].quantity, 10)
  assert.equal(edited[0].unitPrice, 20000)
  assert.equal(edited[0].subtotal, 200000)
  assert.equal(edited[1].itemId, 'item-2')
  assert.equal(edited[1].quantity, 4)
  assert.equal(edited[1].subtotal, 100000)
  assert.equal(quotationTotalAfterDiscount(edited, 10000), 290000)
})

test('the same identifier is not treated as a duplicate', () => {
  assert.equal(duplicatedItemPreservesBusinessFields(original, original), false)
})
