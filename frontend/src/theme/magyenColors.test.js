import assert from 'node:assert/strict'
import test from 'node:test'
import { MAGYEN_LOGO_PUBLIC_PATH, magyenColors, magyenPalette } from './magyenColors.js'

test('semantic palette exposes Magyen gold as primary accent, not as a fill', () => {
  assert.equal(magyenPalette.primary, magyenColors.gold.main)
  assert.equal(magyenPalette.primaryHover, magyenColors.gold.hover)
  assert.equal(magyenPalette.primaryActive, magyenColors.gold.active)
  assert.notEqual(magyenPalette.background, magyenColors.gold.main)
  assert.notEqual(magyenPalette.surface, magyenColors.gold.main)
})

test('surfaces stay white / warm neutral and contrast stays charcoal', () => {
  assert.equal(magyenPalette.background, '#F7F6F3')
  assert.equal(magyenPalette.surface, '#FFFFFF')
  assert.equal(magyenPalette.secondary, magyenColors.charcoal.elevated)
  assert.equal(magyenPalette.textPrimary, magyenColors.text.primary)
})

test('functional states remain recognizable and are not replaced by gold', () => {
  assert.equal(magyenPalette.success, magyenColors.semantic.success)
  assert.equal(magyenPalette.warning, magyenColors.semantic.warning)
  assert.equal(magyenPalette.error, magyenColors.semantic.error)
  assert.equal(magyenPalette.info, magyenColors.semantic.info)
  assert.notEqual(magyenPalette.success, magyenPalette.primary)
  assert.notEqual(magyenPalette.warning, magyenPalette.primary)
  assert.notEqual(magyenPalette.error, magyenPalette.primary)
})

test('logo is the provided static public asset', () => {
  assert.equal(MAGYEN_LOGO_PUBLIC_PATH, '/assets/magyen-logo.png')
})
