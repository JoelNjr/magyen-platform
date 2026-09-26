export function customersForQuotation(customers) {
  return (Array.isArray(customers) ? customers : []).filter(
    (customer) => customer?.category === 'MAGYEN'
  )
}

export function customersForExternalPlotter(customers) {
  return (Array.isArray(customers) ? customers : []).filter(
    (customer) => customer?.category === 'PLOTTER'
  )
}

export function customersForReview(customers) {
  return (Array.isArray(customers) ? customers : []).filter(
    (customer) => customer?.category === 'UNCLASSIFIED'
  )
}
