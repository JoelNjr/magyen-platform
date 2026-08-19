import assert from 'node:assert/strict'
import test from 'node:test'
import {
  canAccessHome,
  filterNavigationItems,
  isHomePath,
  resolveDefaultAuthenticatedPath,
  resolveSafeInternalPath,
} from './authPresentation.js'
import { navigationItems } from '../../../layout/navigationItems.js'

const admin = { userId: 'a', username: 'admin', role: 'ADMIN' }
const operator = { userId: 'o', username: 'operator', role: 'OPERATOR' }

test('ADMIN default authenticated path is Home', () => {
  assert.equal(resolveDefaultAuthenticatedPath(admin), '/home')
  assert.equal(canAccessHome(admin), true)
})

test('OPERATOR default authenticated path is Cotizaciones, not Home', () => {
  assert.equal(resolveDefaultAuthenticatedPath(operator), '/commercial')
  assert.equal(canAccessHome(operator), false)
})

test('OPERATOR safe path rejects direct Home navigation', () => {
  assert.equal(resolveSafeInternalPath('/home', operator), '/commercial')
  assert.equal(resolveSafeInternalPath('/home?tab=1', operator), '/commercial')
  assert.equal(isHomePath('/home'), true)
})

test('ADMIN safe path keeps Home navigation', () => {
  assert.equal(resolveSafeInternalPath('/home', admin), '/home')
  assert.equal(resolveSafeInternalPath(undefined, admin), '/home')
})

test('OPERATOR sidebar does not expose Home navigation', () => {
  const visible = filterNavigationItems(navigationItems, operator)
  assert.equal(
    visible.some((item) => item.path === '/home'),
    false,
    'Home must not appear in OPERATOR navigation'
  )
  assert.equal(
    visible.some((item) => item.path === '/commercial'),
    true,
    'OPERATOR must still see Cotizaciones'
  )
  assert.equal(
    visible.some((item) => item.path === '/finance'),
    false
  )
})

test('ADMIN sidebar still exposes Home navigation', () => {
  const visible = filterNavigationItems(navigationItems, admin)
  assert.equal(
    visible.some((item) => item.path === '/home'),
    true
  )
})
