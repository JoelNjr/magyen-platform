/**
 * Display-only customer name resolution from the Customer read API.
 * Falls back to the technical customerId when the name is unavailable.
 */
export function buildCustomerNameMap(customers) {
  const customerNameById = {}

  if (!Array.isArray(customers)) {
    return customerNameById
  }

  for (const customer of customers) {
    if (customer?.customerId && customer?.name) {
      customerNameById[customer.customerId] = customer.name
    }
  }

  return customerNameById
}

export function resolveCustomerName(customerId, customerNameById) {
  if (!customerId) {
    return ''
  }

  return customerNameById?.[customerId] || customerId
}
