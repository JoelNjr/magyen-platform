import {
  AppBar,
  Box,
  Button,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'
import { filterNavigationItems } from '../features/auth/presentation/authPresentation'
import { navigationItems } from './navigationItems'

const DRAWER_WIDTH = 240

function isNavigationItemSelected(pathname, item) {
  if (typeof item.selectedWhen === 'function') {
    return item.selectedWhen(pathname)
  }
  return pathname === item.path || pathname.startsWith(`${item.path}/`)
}

function MainLayout() {
  const location = useLocation()
  const { identity, logout } = useAuth()
  const visibleNavigationItems = filterNavigationItems(navigationItems, identity)

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
          <Box sx={{ flexGrow: 1 }} />
          {identity?.username && (
            <Typography variant="body2" sx={{ mr: 2 }} noWrap>
              {identity.username}
            </Typography>
          )}
          <Button color="inherit" onClick={logout}>
            Cerrar sesión
          </Button>
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
            {visibleNavigationItems.map((item) => (
              <Box key={item.path}>
                <ListItemButton
                  component={RouterLink}
                  to={item.path}
                  selected={isNavigationItemSelected(location.pathname, item)}
                >
                  <ListItemText primary={item.label} />
                </ListItemButton>
                {Array.isArray(item.children)
                  ? item.children.map((child) => (
                      <ListItemButton
                        key={child.path}
                        component={RouterLink}
                        to={child.path}
                        selected={isNavigationItemSelected(
                          location.pathname,
                          child
                        )}
                        sx={{ pl: 4 }}
                      >
                        <ListItemText primary={child.label} />
                      </ListItemButton>
                    ))
                  : null}
              </Box>
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
