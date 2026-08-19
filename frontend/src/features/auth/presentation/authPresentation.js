export function resolveLoginErrorMessage(error) {
  const status = error?.response?.status

  if (status === 401) {
    return 'Usuario o contraseña incorrectos.'
  }

  if (status === 400) {
    return 'Usuario y contraseña son obligatorios.'
  }

  if (!error?.response) {
    return 'No fue posible conectar con el servidor.'
  }

  return 'No fue posible iniciar sesión.'
}

export function isAdmin(identity) {
  return identity?.role === 'ADMIN'
}

export function formatAuthenticationRoleLabel(role) {
  if (role === 'ADMIN') {
    return 'Administrador'
  }
  if (role === 'OPERATOR') {
    return 'Operador'
  }
  return role || '—'
}

export function resolveAdminUsersErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

/** Home is ADMIN-only in V1. Operators land on Cotizaciones. */
export function resolveDefaultAuthenticatedPath(identity) {
  return isAdmin(identity) ? '/home' : '/commercial'
}

export function isHomePath(path) {
  if (typeof path !== 'string') {
    return false
  }
  return path === '/home' || path.startsWith('/home?') || path.startsWith('/home/')
}

export function canAccessHome(identity) {
  return isAdmin(identity)
}

export function resolveSafeInternalPath(fromPath, identity) {
  const fallback = resolveDefaultAuthenticatedPath(identity)

  if (typeof fromPath !== 'string' || !fromPath.startsWith('/') || fromPath.startsWith('//')) {
    return fallback
  }

  if (fromPath === '/login' || fromPath.startsWith('/login?')) {
    return fallback
  }

  if (isHomePath(fromPath) && !canAccessHome(identity)) {
    return fallback
  }

  return fromPath
}

export function isNavigationItemVisible(item, identity) {
  if (!item) {
    return false
  }
  return !item.adminOnly || isAdmin(identity)
}

export function filterNavigationItems(navigationItems, identity) {
  if (!Array.isArray(navigationItems)) {
    return []
  }
  return navigationItems.filter((item) => isNavigationItemVisible(item, identity))
}
