export function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function validateDeliveryDate(deliveryDate, confirmationDate, today) {
  if (!deliveryDate) {
    return 'La fecha de entrega es obligatoria.'
  }
  if (confirmationDate && deliveryDate < confirmationDate) {
    return 'La fecha de entrega no puede ser anterior a la fecha de confirmación.'
  }
  if (today && deliveryDate > today) {
    return 'La fecha de entrega no puede ser posterior a hoy.'
  }
  return ''
}

export function buildDeliverOrderRequest(deliveryDate) {
  return { deliveryDate }
}
