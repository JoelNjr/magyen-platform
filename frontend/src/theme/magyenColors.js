/**
 * Magyen / Confecciones Magyen visual identity tokens.
 * Gold is an accent (~5%), not a page fill. Surfaces stay white / warm neutral.
 */
export const magyenColors = {
  gold: {
    highlight: '#F0D078',
    main: '#C9A227',
    hover: '#B8911F',
    active: '#A07E18',
    muted: 'rgba(201, 162, 39, 0.16)',
  },
  charcoal: {
    main: '#111111',
    elevated: '#1A1A1A',
    contrast: '#F7F6F3',
  },
  surface: {
    background: '#F7F6F3',
    paper: '#FFFFFF',
    muted: '#F3F1EC',
  },
  text: {
    primary: '#1A1A1A',
    secondary: '#5C5C5C',
    disabled: '#8A8A8A',
    onDark: '#F7F6F3',
    onDarkMuted: 'rgba(247, 246, 243, 0.72)',
  },
  border: {
    default: '#E4E1D8',
    strong: '#C9C4B6',
    onDark: 'rgba(255, 255, 255, 0.08)',
  },
  semantic: {
    success: '#2E7D32',
    warning: '#ED6C02',
    error: '#C62828',
    info: '#1565C0',
  },
}

export const magyenPalette = {
  primary: magyenColors.gold.main,
  primaryHover: magyenColors.gold.hover,
  primaryActive: magyenColors.gold.active,
  secondary: magyenColors.charcoal.elevated,
  background: magyenColors.surface.background,
  surface: magyenColors.surface.paper,
  surfaceMuted: magyenColors.surface.muted,
  textPrimary: magyenColors.text.primary,
  textSecondary: magyenColors.text.secondary,
  border: magyenColors.border.default,
  success: magyenColors.semantic.success,
  warning: magyenColors.semantic.warning,
  error: magyenColors.semantic.error,
  info: magyenColors.semantic.info,
}

export const MAGYEN_LOGO_PUBLIC_PATH = '/assets/magyen-logo.png'
