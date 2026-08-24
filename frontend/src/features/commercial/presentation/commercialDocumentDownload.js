export const QUOTATION_PDF_ACTION_LABEL = 'Generar PDF'
export const ORDER_REMISSION_ACTION_LABEL = 'Generar remisión'
export const PRODUCTION_ORDER_PDF_ACTION_LABEL = 'Generar PDF'

export function parseContentDispositionFilename(header, fallbackFilename) {
  if (!header || typeof header !== 'string') {
    return fallbackFilename
  }

  const utfMatch = /filename\*=UTF-8''([^;]+)/i.exec(header)
  if (utfMatch) {
    try {
      return decodeURIComponent(utfMatch[1])
    } catch {
      return utfMatch[1]
    }
  }

  const quotedMatch = /filename="([^"]+)"/i.exec(header)
  if (quotedMatch) {
    return quotedMatch[1]
  }

  const plainMatch = /filename=([^;]+)/i.exec(header)
  if (plainMatch) {
    return plainMatch[1].trim()
  }

  return fallbackFilename
}

export async function resolveBlobApiErrorMessage(error, fallbackMessage) {
  const data = error?.response?.data
  if (data && typeof data.text === 'function') {
    try {
      const parsed = JSON.parse(await data.text())
      return parsed.message || fallbackMessage
    } catch {
      return fallbackMessage
    }
  }

  return error?.response?.data?.message || fallbackMessage
}

export function triggerBrowserPdfDownload(response, fallbackFilename) {
  const filename = parseContentDispositionFilename(
    response?.headers?.['content-disposition'],
    fallbackFilename
  )
  const blob = new Blob([response.data], { type: 'application/pdf' })
  const objectUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(objectUrl)
}
