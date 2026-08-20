import assert from 'node:assert/strict'
import test from 'node:test'
import {
  loginEntranceAnimation,
  magyenMotion,
  magyenThemeTransitions,
  motionCss,
} from './magyenMotion.js'

test('motion tokens stay short and professional', () => {
  assert.equal(magyenMotion.duration.fast, 120)
  assert.equal(magyenMotion.duration.normal, 220)
  assert.equal(magyenMotion.duration.slow, 320)
  assert.ok(magyenMotion.duration.slow <= 320)
  assert.equal(magyenMotion.easing.standard, 'cubic-bezier(0.4, 0, 0.2, 1)')
  assert.equal(magyenMotion.easing.emphasized, 'cubic-bezier(0.2, 0, 0, 1)')
})

test('motionCss uses centralized duration and easing', () => {
  assert.equal(
    motionCss('background-color', 'fast'),
    `background-color ${magyenMotion.duration.fast}ms ${magyenMotion.easing.standard}`
  )
  assert.equal(
    motionCss('transform', 'normal', 'emphasized'),
    `transform ${magyenMotion.duration.normal}ms ${magyenMotion.easing.emphasized}`
  )
})

test('MUI transition mapping uses Magyen durations', () => {
  assert.equal(magyenThemeTransitions.duration.standard, magyenMotion.duration.normal)
  assert.equal(magyenThemeTransitions.duration.enteringScreen, magyenMotion.duration.normal)
  assert.ok(magyenThemeTransitions.duration.leavingScreen < magyenMotion.duration.normal)
})

test('login entrance is a one-shot fade, not a continuous animation', () => {
  assert.match(loginEntranceAnimation.animation, /magyenLoginEnter/)
  assert.equal(
    loginEntranceAnimation[magyenMotion.reducedMotionQuery].animation,
    'none'
  )
})
