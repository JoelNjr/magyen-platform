import assert from 'node:assert/strict'
import test from 'node:test'
import { filterNavigationItems } from '../features/auth/presentation/authPresentation.js'
import { navigationItems } from './navigationItems.js'
import {
  COMPACT_NAVIGATION_MAX_WIDTH,
  isCompactNavigationWidth,
} from './responsiveNavigation.js'

const admin = { userId: 'a', username: 'admin', role: 'ADMIN' }
const operator = { userId: 'o', username: 'operator', role: 'OPERATOR' }

test('compact navigation starts below the MUI md breakpoint', () => {
  assert.equal(isCompactNavigationWidth(360), true)
  assert.equal(isCompactNavigationWidth(375), true)
  assert.equal(isCompactNavigationWidth(480), true)
  assert.equal(isCompactNavigationWidth(600), true)
  assert.equal(isCompactNavigationWidth(768), true)
  assert.equal(isCompactNavigationWidth(COMPACT_NAVIGATION_MAX_WIDTH), true)
  assert.equal(isCompactNavigationWidth(900), false)
  assert.equal(isCompactNavigationWidth(1024), false)
  assert.equal(isCompactNavigationWidth(1280), false)
  assert.equal(isCompactNavigationWidth(1440), false)
})

test('OPERATOR compact navigation still hides Home, Finanzas and Administración', () => {
  const visible = filterNavigationItems(navigationItems, operator)
  const labels = visible.map((item) => item.label)
  assert.equal(labels.includes('Inicio'), false)
  assert.equal(labels.includes('Finanzas'), false)
  assert.equal(labels.includes('Administración'), false)
  assert.equal(labels.includes('Cotizaciones'), true)
  assert.equal(labels.includes('Órdenes'), true)
  assert.equal(labels.includes('Producción'), true)
  assert.equal(labels.includes('Inventario'), true)
  assert.equal(labels.includes('Plotter'), true)
})

test('ADMIN compact navigation keeps Home and Administración', () => {
  const visible = filterNavigationItems(navigationItems, admin)
  const labels = visible.map((item) => item.label)
  assert.equal(labels.includes('Inicio'), true)
  assert.equal(labels.includes('Administración'), true)
  assert.equal(labels.includes('Finanzas'), true)
})
