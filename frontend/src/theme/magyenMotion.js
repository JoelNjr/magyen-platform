/**
 * Magyen motion tokens. Durations stay short; easing stays professional.
 * Prefer CSS transitions. Do not scatter arbitrary timings in JSX.
 */
export const magyenMotion = {
  duration: {
    fast: 120,
    normal: 220,
    slow: 320,
  },
  easing: {
    standard: 'cubic-bezier(0.4, 0, 0.2, 1)',
    emphasized: 'cubic-bezier(0.2, 0, 0, 1)',
  },
  reducedMotionQuery: '@media (prefers-reduced-motion: reduce)',
}

export function motionCss(property, speed = 'normal', easing = 'standard') {
  return `${property} ${magyenMotion.duration[speed]}ms ${magyenMotion.easing[easing]}`
}

export const magyenThemeTransitions = {
  duration: {
    shortest: magyenMotion.duration.fast,
    shorter: magyenMotion.duration.fast,
    short: magyenMotion.duration.fast,
    standard: magyenMotion.duration.normal,
    complex: magyenMotion.duration.slow,
    enteringScreen: magyenMotion.duration.normal,
    leavingScreen: 180,
  },
  easing: {
    easeInOut: magyenMotion.easing.standard,
    easeOut: magyenMotion.easing.emphasized,
    easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
    sharp: 'cubic-bezier(0.4, 0, 0.6, 1)',
  },
}

export const loginEntranceAnimation = {
  '@keyframes magyenLoginEnter': {
    from: {
      opacity: 0,
      transform: 'translateY(8px)',
    },
    to: {
      opacity: 1,
      transform: 'translateY(0)',
    },
  },
  animation: `magyenLoginEnter ${magyenMotion.duration.slow}ms ${magyenMotion.easing.emphasized}`,
  [magyenMotion.reducedMotionQuery]: {
    animation: 'none',
  },
}
