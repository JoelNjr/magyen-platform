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

export function resolveSafeInternalPath(fromPath) {
  if (typeof fromPath !== 'string' || !fromPath.startsWith('/') || fromPath.startsWith('//')) {
    return '/home'
  }

  if (fromPath === '/login' || fromPath.startsWith('/login?')) {
    return '/home'
  }

  return fromPath
}
