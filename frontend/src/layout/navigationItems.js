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

export const navigationItems = [
  { label: 'Inicio', path: '/home', adminOnly: true },
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
  {
    label: 'Administración',
    path: '/admin/users',
    adminOnly: true,
    selectedWhen: (pathname) => pathname.startsWith('/admin'),
    children: [
      { label: 'Usuarios', path: '/admin/users' },
      { label: 'Catálogos', path: '/admin/catalogs' },
    ],
  },
]
