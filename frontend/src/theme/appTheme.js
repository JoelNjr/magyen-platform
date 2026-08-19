import { createTheme } from '@mui/material/styles'

/**
 * Layout-only theme. Palette and visual identity are unchanged from MUI defaults
 * already used by the accepted desktop UI.
 */
const appTheme = createTheme({
  typography: {
    h3: {
      fontSize: '2.125rem',
      '@media (max-width:600px)': {
        fontSize: '1.75rem',
      },
    },
    h4: {
      '@media (max-width:600px)': {
        fontSize: '1.5rem',
      },
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
        },
        '#root': {
          minWidth: 0,
          maxWidth: '100%',
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
    MuiTableCell: {
      styleOverrides: {
        root: {
          overflowWrap: 'anywhere',
        },
      },
    },
    MuiDialog: {
      defaultProps: {
        fullWidth: true,
        scroll: 'paper',
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
    MuiDialogContent: {
      styleOverrides: {
        root: {
          overflowY: 'auto',
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          flexWrap: 'wrap',
          gap: 8,
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          '@media (max-width:899px)': {
            minHeight: 40,
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          '@media (max-width:899px)': {
            minWidth: 40,
            minHeight: 40,
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          '@media (max-width:899px)': {
            minHeight: 44,
          },
        },
      },
    },
  },
})

export default appTheme
