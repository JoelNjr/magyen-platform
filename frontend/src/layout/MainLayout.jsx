import { useEffect, useState } from 'react'
import MenuIcon from '@mui/icons-material/Menu'
import {
  AppBar,
  Box,
  Button,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'
import { filterNavigationItems } from '../features/auth/presentation/authPresentation'
import { navigationItems } from './navigationItems'
import {
  PERMANENT_DRAWER_WIDTH,
  TEMPORARY_DRAWER_WIDTH,
} from './responsiveNavigation'

function isNavigationItemSelected(pathname, item) {
  if (typeof item.selectedWhen === 'function') {
    return item.selectedWhen(pathname)
  }
  return pathname === item.path || pathname.startsWith(`${item.path}/`)
}

function NavigationList({ items, pathname, onNavigate }) {
  return (
    <List>
      {items.map((item) => (
        <Box key={item.path}>
          <ListItemButton
            component={RouterLink}
            to={item.path}
            selected={isNavigationItemSelected(pathname, item)}
            onClick={onNavigate}
          >
            <ListItemText primary={item.label} />
          </ListItemButton>
          {Array.isArray(item.children)
            ? item.children.map((child) => (
                <ListItemButton
                  key={child.path}
                  component={RouterLink}
                  to={child.path}
                  selected={isNavigationItemSelected(pathname, child)}
                  onClick={onNavigate}
                  sx={{ pl: 4 }}
                >
                  <ListItemText primary={child.label} />
                </ListItemButton>
              ))
            : null}
        </Box>
      ))}
    </List>
  )
}

function MainLayout() {
  const location = useLocation()
  const theme = useTheme()
  const isCompactNavigation = useMediaQuery(theme.breakpoints.down('md'))
  const { identity, logout } = useAuth()
  const visibleNavigationItems = filterNavigationItems(navigationItems, identity)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)

  useEffect(() => {
    setMobileNavOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!isCompactNavigation) {
      setMobileNavOpen(false)
    }
  }, [isCompactNavigation])

  function closeMobileNav() {
    setMobileNavOpen(false)
  }

  const navigation = (
    <>
      <Toolbar />
      <Box sx={{ overflow: 'auto', display: 'flex', flexDirection: 'column', height: '100%' }}>
        <NavigationList
          items={visibleNavigationItems}
          pathname={location.pathname}
          onNavigate={isCompactNavigation ? closeMobileNav : undefined}
        />
        {isCompactNavigation && identity?.username ? (
          <Box sx={{ mt: 'auto', px: 2, py: 2 }}>
            <Typography variant="body2" color="text.secondary" noWrap>
              {identity.username}
            </Typography>
          </Box>
        ) : null}
      </Box>
    </>
  )

  return (
    <Box sx={{ display: 'flex', height: '100dvh', minWidth: 0, maxWidth: '100%' }}>
      <AppBar
        position="fixed"
        sx={{ zIndex: (appTheme) => appTheme.zIndex.drawer + 1 }}
      >
        <Toolbar
          sx={{ gap: 1, px: { xs: 1, sm: 2 }, minHeight: { xs: 56, sm: 64 } }}
        >
          {isCompactNavigation ? (
            <IconButton
              color="inherit"
              edge="start"
              aria-label="Abrir menú de navegación"
              aria-expanded={mobileNavOpen}
              onClick={() => setMobileNavOpen(true)}
            >
              <MenuIcon />
            </IconButton>
          ) : null}
          <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{ flexGrow: 1, minWidth: 0 }}
          >
            Magyen
            <Box component="span" sx={{ display: { xs: 'none', sm: 'inline' } }}>
              {' '}
              Platform
            </Box>
          </Typography>
          {identity?.username ? (
            <Typography
              variant="body2"
              noWrap
              title={identity.username}
              sx={{
                mr: { sm: 1 },
                maxWidth: { xs: 96, sm: 180 },
                flexShrink: 1,
              }}
            >
              {identity.username}
            </Typography>
          ) : null}
          <Button color="inherit" onClick={logout} sx={{ flexShrink: 0 }}>
            Cerrar sesión
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer
        variant={isCompactNavigation ? 'temporary' : 'permanent'}
        open={isCompactNavigation ? mobileNavOpen : true}
        onClose={closeMobileNav}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: isCompactNavigation ? 0 : PERMANENT_DRAWER_WIDTH,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: {
            width: isCompactNavigation
              ? TEMPORARY_DRAWER_WIDTH
              : PERMANENT_DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        {navigation}
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          width: '100%',
          p: { xs: 2, md: 3 },
          height: '100dvh',
          overflow: 'auto',
        }}
      >
        <Toolbar sx={{ minHeight: { xs: 56, sm: 64 } }} />
        <Box sx={{ minWidth: 0, maxWidth: '100%' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  )
}

export default MainLayout
