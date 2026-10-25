export function validateQuotationDeliveryDate(deliveryDate, creationDate) {
  if (!deliveryDate) {
    return 'La fecha de entrega es obligatoria.'
  }
  if (creationDate && deliveryDate < creationDate) {
    return 'La fecha de entrega no puede ser anterior a la fecha de creación.'
  }
  return ''
}

export function validatePromisedDeliveryDate(promisedDeliveryDate, confirmationDate) {
  if (!promisedDeliveryDate) {
    return 'La fecha de entrega programada es obligatoria.'
  }
  if (confirmationDate && promisedDeliveryDate < confirmationDate) {
    return 'La fecha de entrega programada no puede ser anterior a la fecha de confirmación.'
  }
  return ''
}

export function buildUpdateQuotationDeliveryDateRequest(deliveryDate) {
  return { deliveryDate }
}

export function buildUpdatePromisedDeliveryDateRequest(promisedDeliveryDate) {
  return { promisedDeliveryDate }
}
