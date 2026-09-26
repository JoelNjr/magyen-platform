import assert from 'node:assert/strict'
import test from 'node:test'
import {
  customersForExternalPlotter,
  customersForQuotation,
  customersForReview,
} from './customerCategory.js'

const customers = [
  { customerId: '1', name: 'Colegio', category: 'MAGYEN' },
  { customerId: '2', name: 'Foncho', category: 'PLOTTER' },
  { customerId: '3', name: 'Sandra', category: 'UNCLASSIFIED' },
]

test('quotation selector receives only Magyen customers', () => {
  assert.deepEqual(
    customersForQuotation(customers).map((customer) => customer.name),
    ['Colegio']
  )
})

test('external plotter selector receives only Plotter customers', () => {
  assert.deepEqual(
    customersForExternalPlotter(customers).map((customer) => customer.name),
    ['Foncho']
  )
})

test('unclassified customers stay available for review', () => {
  assert.deepEqual(
    customersForReview(customers).map((customer) => customer.name),
    ['Sandra']
  )
})
