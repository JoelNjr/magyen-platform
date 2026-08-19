import { CssBaseline, ThemeProvider } from '@mui/material'
import AppRouter from '../router/AppRouter'
import appTheme from '../theme/appTheme'

function App() {
  return (
    <ThemeProvider theme={appTheme}>
      <CssBaseline />
      <AppRouter />
    </ThemeProvider>
  )
}

export default App
