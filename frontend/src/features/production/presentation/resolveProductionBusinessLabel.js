/**
 * Display-only business identifier. Never derives a label from a UUID.
 */
export function resolveProductionBusinessLabel(value) {
  if (typeof value === 'string' && value.trim() !== '') {
    return value.trim()
  }
  return '—'
}
