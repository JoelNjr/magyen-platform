/**
 * Display-only date formatting (API dates remain YYYY-MM-DD).
 */
export function formatDisplayDate(value) {
  if (!value) {
    return ''
  }

  const [year, month, day] = String(value).split('-')

  if (!year || !month || !day) {
    return String(value)
  }

  return `${day}/${month}/${year}`
}
