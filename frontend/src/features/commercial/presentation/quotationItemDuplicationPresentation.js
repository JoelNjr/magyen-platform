export function findQuotationItemById(items, itemId) {
  if (!itemId || !Array.isArray(items)) {
    return null
  }
  return items.find((item) => item.itemId === itemId) ?? null
}

export function quotationItemBusinessSnapshot(item) {
  const specification = item?.productSpecification || {}
  return {
    productName: item?.productName ?? '',
    quantity: item?.quantity ?? null,
    fabric: item?.fabric ?? '',
    secondaryFabric: item?.secondaryFabric || null,
    color: item?.color ?? '',
    unitPrice: item?.unitPrice ?? null,
    garmentType: specification.garmentType || null,
    collarType: specification.collarType || null,
    sleeveType: specification.sleeveType || null,
    cuffRequired: specification.cuffRequired ?? null,
    sublimationRequired: Boolean(specification.sublimationRequired),
    embroideryRequired: Boolean(specification.embroideryRequired),
    dtfRequired: Boolean(specification.dtfRequired),
    decorationNotes: specification.decorationNotes || null,
    includesNames: Boolean(specification.includesNames),
    includesNumbers: Boolean(specification.includesNumbers),
    includesLogos: Boolean(specification.includesLogos),
    personalizationNotes: specification.personalizationNotes || null,
    itemObservations: specification.itemObservations || null,
  }
}

export function duplicatedItemPreservesBusinessFields(original, duplicate) {
  if (!original || !duplicate || !original.itemId || !duplicate.itemId) {
    return false
  }
  if (original.itemId === duplicate.itemId) {
    return false
  }
  return JSON.stringify(quotationItemBusinessSnapshot(original))
    === JSON.stringify(quotationItemBusinessSnapshot(duplicate))
}

export function replaceQuotationItem(items, itemId, patch) {
  return items.map((item) => {
    if (item.itemId !== itemId) {
      return item
    }
    const quantity = patch.quantity ?? item.quantity
    const unitPrice = patch.unitPrice ?? item.unitPrice
    return {
      ...item,
      ...patch,
      subtotal: Number(quantity) * Number(unitPrice),
    }
  })
}

export function quotationTotalAfterDiscount(items, discountAmount) {
  const subtotal = items.reduce((sum, item) => sum + Number(item.subtotal || 0), 0)
  return subtotal - Number(discountAmount || 0)
}
