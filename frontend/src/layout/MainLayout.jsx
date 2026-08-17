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
import { isAdmin } from '../features/auth/presentation/authPresentation'

const DRAWER_WIDTH = 240

function isQuotationsSelected(pathname) {
  return (
    pathname === '/commercial' ||
    pathname.startsWith('/commercial/new') ||
    pathname.startsWith('/commercial/quotations')
  )
}

function isOrdersSelected(pathname) {
  return (
    pathname === '/commercial/orders' ||
    pathname.startsWith('/commercial/orders/')
  )
}

const navigationItems = [
  { label: 'Inicio', path: '/home' },
  {
    label: 'Cotizaciones',
    path: '/commercial',
    selectedWhen: isQuotationsSelected,
    children: [
      { label: 'Clientes', path: '/commercial/customers' },
      { label: 'Vendedores', path: '/commercial/sellers' },
    ],
  },
  {
    label: 'Órdenes',
    path: '/commercial/orders',
    selectedWhen: isOrdersSelected,
  },
  { label: 'Producción', path: '/production' },
  { label: 'Inventario', path: '/inventory' },
  { label: 'Plotter', path: '/plotter' },
  { label: 'Finanzas', path: '/finance', adminOnly: true },
  { label: 'Usuarios', path: '/admin/users', adminOnly: true },
]

function isNavigationItemSelected(pathname, item) {
  if (typeof item.selectedWhen === 'function') {
    return item.selectedWhen(pathname)
  }
  return pathname === item.path || pathname.startsWith(`${item.path}/`)
}

function MainLayout() {
  const location = useLocation()
  const { identity, logout } = useAuth()
  const visibleNavigationItems = navigationItems.filter(
    (item) => !item.adminOnly || isAdmin(identity)
  )

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
