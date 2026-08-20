import { createTheme } from '@mui/material/styles'
import { magyenColors, magyenPalette } from './magyenColors'
import { magyenMotion, magyenThemeTransitions, motionCss } from './magyenMotion'

const fastColorMotion = motionCss('background-color, border-color, color, box-shadow', 'fast')
const fastInputMotion = motionCss('border-color, box-shadow', 'fast')
const fastTransformMotion = motionCss(
  'background-color, border-color, box-shadow, transform',
  'fast'
)

/**
 * Magyen visual identity + SPR-039 Phase 2 layout/responsive overrides
 * + SPR-039 Phase 4 motion tokens.
 */
const appTheme = createTheme({
  spacing: 8,
  shape: {
    borderRadius: 8,
  },
  transitions: magyenThemeTransitions,
  palette: {
    primary: {
      main: magyenPalette.primary,
      light: magyenColors.gold.highlight,
      dark: magyenPalette.primaryActive,
      contrastText: magyenColors.charcoal.main,
    },
    secondary: {
      main: magyenPalette.secondary,
      contrastText: magyenColors.charcoal.contrast,
    },
    background: {
      default: magyenPalette.background,
      paper: magyenPalette.surface,
    },
    text: {
      primary: magyenPalette.textPrimary,
      secondary: magyenPalette.textSecondary,
      disabled: magyenColors.text.disabled,
    },
    divider: magyenPalette.border,
    success: {
      main: magyenPalette.success,
    },
    warning: {
      main: magyenPalette.warning,
    },
    error: {
      main: magyenPalette.error,
    },
    info: {
      main: magyenPalette.info,
    },
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
    h3: {
      fontSize: '2.125rem',
      fontWeight: 700,
      letterSpacing: '-0.02em',
      '@media (max-width:600px)': {
        fontSize: '1.75rem',
      },
    },
    h4: {
      fontWeight: 700,
      '@media (max-width:600px)': {
        fontSize: '1.5rem',
      },
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        html: {
          overflowX: 'clip',
        },
        body: {
          overflowX: 'clip',
          maxWidth: '100%',
          backgroundColor: magyenPalette.background,
        },
        '#root': {
          minWidth: 0,
          maxWidth: '100%',
        },
        [magyenMotion.reducedMotionQuery]: {
          '*, *::before, *::after': {
            animationDuration: '0.01ms !important',
            animationIterationCount: 1,
            transitionDuration: '0.01ms !important',
            scrollBehavior: 'auto',
          },
          '.MuiTouchRipple-root': {
            display: 'none',
          },
        },
      },
    },
    MuiAppBar: {
      defaultProps: {
        color: 'secondary',
        elevation: 0,
      },
      styleOverrides: {
        root: {
          backgroundColor: magyenColors.charcoal.main,
          color: magyenColors.text.onDark,
          borderBottom: `1px solid ${magyenColors.gold.main}33`,
          '& .MuiButton-root': {
            transition: fastColorMotion,
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.08)',
            },
            '&:focus-visible': {
              outline: `2px solid ${magyenColors.gold.main}`,
              outlineOffset: 2,
            },
          },
          '& .MuiIconButton-root': {
            transition: fastColorMotion,
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.08)',
            },
            '&:active': {
              backgroundColor: 'rgba(255, 255, 255, 0.14)',
            },
            '&:focus-visible': {
              outline: `2px solid ${magyenColors.gold.main}`,
              outlineOffset: 2,
            },
          },
        },
      },
    },
    MuiPaper: {
      defaultProps: {
        elevation: 0,
      },
      styleOverrides: {
        outlined: {
          borderColor: magyenPalette.border,
        },
        rounded: {
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      defaultProps: {
        variant: 'outlined',
      },
      styleOverrides: {
        root: {
          borderRadius: 8,
          borderColor: magyenPalette.border,
          boxShadow: magyenColors.shadow.card,
          transition: fastColorMotion,
        },
      },
    },
    MuiCardContent: {
      styleOverrides: {
        root: {
          padding: 20,
          '&:last-child': {
            paddingBottom: 20,
          },
        },
      },
    },
    MuiFormLabel: {
      styleOverrides: {
        asterisk: {
          color: magyenPalette.error,
        },
      },
    },
    MuiFormHelperText: {
      styleOverrides: {
        root: {
          marginLeft: 0,
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          overflowX: 'auto',
          maxWidth: '100%',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: magyenPalette.surfaceMuted,
          '& .MuiTableCell-head': {
            fontWeight: 700,
            color: magyenPalette.textPrimary,
            borderBottom: `1px solid ${magyenPalette.border}`,
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          overflowWrap: 'anywhere',
          borderColor: magyenPalette.border,
          py: 1.25,
          px: 2,
          fontVariantNumeric: 'tabular-nums',
        },
        head: {
          py: 1.5,
          whiteSpace: 'nowrap',
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {
          '& .MuiTableRow-root': {
            transition: motionCss('background-color', 'fast'),
          },
          '& .MuiTableRow-root:hover': {
            backgroundColor: 'rgba(17, 17, 17, 0.03)',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&.MuiTableRow-hover:hover': {
            backgroundColor: 'rgba(17, 17, 17, 0.03)',
          },
        },
      },
    },
    MuiDialog: {
      defaultProps: {
        fullWidth: true,
        scroll: 'paper',
        transitionDuration: {
          enter: magyenMotion.duration.normal,
          exit: magyenThemeTransitions.duration.leavingScreen,
        },
      },
      styleOverrides: {
        paper: {
          margin: 16,
          maxHeight: 'calc(100dvh - 32px)',
        },
        paperWidthXs: {
          maxWidth: 'min(444px, calc(100vw - 32px))',
        },
        paperWidthSm: {
          maxWidth: 'min(600px, calc(100vw - 32px))',
        },
        paperWidthMd: {
          maxWidth: 'min(900px, calc(100vw - 32px))',
        },
        paperWidthLg: {
          maxWidth: 'min(1200px, calc(100vw - 32px))',
        },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          pb: 1,
        },
      },
    },
    MuiDialogContent: {
      styleOverrides: {
        root: {
          overflowY: 'auto',
          paddingTop: 16,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          flexWrap: 'wrap',
          gap: 8,
          px: 3,
          py: 2,
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 8,
          transition: fastTransformMotion,
          '@media (max-width:899px)': {
            minHeight: 40,
          },
          '&:active:not(.Mui-disabled)': {
            transform: 'scale(0.98)',
          },
          '&:focus-visible': {
            outline: `2px solid ${magyenColors.gold.main}`,
            outlineOffset: 2,
          },
        },
        containedPrimary: {
          backgroundColor: magyenPalette.primary,
          color: magyenColors.charcoal.main,
          '&:hover': {
            backgroundColor: magyenPalette.primaryHover,
          },
          '&:active': {
            backgroundColor: magyenPalette.primaryActive,
          },
        },
        outlined: {
          borderColor: magyenColors.border.strong,
          backgroundColor: magyenPalette.surface,
          '&:hover': {
            borderColor: magyenColors.charcoal.elevated,
            backgroundColor: magyenPalette.surfaceMuted,
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          transition: fastColorMotion,
          '@media (max-width:899px)': {
            minWidth: 40,
            minHeight: 40,
          },
          '&:focus-visible': {
            outline: `2px solid ${magyenColors.gold.main}`,
            outlineOffset: 2,
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          transition: fastColorMotion,
          '@media (max-width:899px)': {
            minHeight: 44,
          },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          backgroundColor: magyenPalette.surface,
          transition: fastInputMotion,
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: magyenColors.charcoal.elevated,
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: magyenPalette.primary,
            borderWidth: 2,
          },
        },
        notchedOutline: {
          borderColor: magyenPalette.border,
          transition: fastInputMotion,
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          '&.Mui-focused': {
            color: magyenColors.charcoal.elevated,
          },
        },
      },
    },
    MuiCheckbox: {
      styleOverrides: {
        root: {
          color: magyenColors.border.strong,
          transition: fastColorMotion,
          '&.Mui-checked': {
            color: magyenPalette.primary,
          },
        },
      },
    },
    MuiRadio: {
      styleOverrides: {
        root: {
          color: magyenColors.border.strong,
          transition: fastColorMotion,
          '&.Mui-checked': {
            color: magyenPalette.primary,
          },
        },
      },
    },
    MuiSwitch: {
      styleOverrides: {
        switchBase: {
          '&.Mui-checked': {
            color: magyenPalette.primary,
          },
          '&.Mui-checked + .MuiSwitch-track': {
            backgroundColor: magyenPalette.primary,
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          fontWeight: 600,
          transition: fastColorMotion,
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: magyenPalette.primary,
          height: 3,
          transition: motionCss('left, width', 'normal'),
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          transition: fastColorMotion,
          '&.Mui-selected': {
            color: magyenColors.charcoal.elevated,
          },
        },
      },
    },
    MuiLink: {
      styleOverrides: {
        root: {
          color: magyenPalette.textPrimary,
          textDecorationColor: magyenPalette.primary,
          transition: motionCss('color', 'fast'),
          '&:hover': {
            color: magyenPalette.primaryHover,
          },
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiSnackbar: {
      defaultProps: {
        transitionDuration: {
          enter: magyenMotion.duration.normal,
          exit: magyenThemeTransitions.duration.leavingScreen,
        },
      },
    },
    MuiBackdrop: {
      styleOverrides: {
        root: {
          transition: motionCss('opacity', 'normal'),
        },
      },
    },
    MuiDrawer: {
      defaultProps: {
        transitionDuration: {
          enter: magyenMotion.duration.normal,
          exit: magyenThemeTransitions.duration.leavingScreen,
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          opacity: 0.72,
        },
      },
    },
  },
})

export default appTheme
