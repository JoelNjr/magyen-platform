import {
  AppBar,
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'

const DRAWER_WIDTH = 240

const navigationItems = [
  { label: 'Commercial', path: '/commercial' },
  { label: 'Production', path: '/production' },
  { label: 'Inventory', path: '/inventory' },
  { label: 'Finance', path: '/finance' },
  { label: 'Intelligence', path: '/intelligence' },
]

function isNavigationItemSelected(pathname, itemPath) {
  return pathname === itemPath || pathname.startsWith(`${itemPath}/`)
}

function MainLayout() {
  const location = useLocation()

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      <AppBar
        position="fixed"
        sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
      >
        <Toolbar>
          <Typography variant="h6" noWrap component="div">
            Magyen Platform
          </Typography>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {navigationItems.map((item) => (
              <ListItemButton
                key={item.path}
                component={RouterLink}
                to={item.path}
                selected={isNavigationItemSelected(location.pathname, item.path)}
              >
                <ListItemText primary={item.label} />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          height: '100vh',
          overflow: 'auto',
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  )
}

export default MainLayout
