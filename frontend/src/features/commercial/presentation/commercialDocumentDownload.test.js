import assert from 'node:assert/strict'
import test from 'node:test'
import {
  ORDER_REMISSION_ACTION_LABEL,
  QUOTATION_PDF_ACTION_LABEL,
  parseContentDispositionFilename,
  resolveBlobApiErrorMessage,
} from './commercialDocumentDownload.js'

test('quotation action label is Generar PDF', () => {
  assert.equal(QUOTATION_PDF_ACTION_LABEL, 'Generar PDF')
})

test('order action label is Generar remisión', () => {
  assert.equal(ORDER_REMISSION_ACTION_LABEL, 'Generar remisión')
})

test('filename uses the quoted Content-Disposition value', () => {
  assert.equal(
    parseContentDispositionFilename(
      'attachment; filename="Cotizacion_14.pdf"',
      'Cotizacion.pdf'
    ),
    'Cotizacion_14.pdf'
  )
})

test('filename preserves the existing order business number', () => {
  assert.equal(
    parseContentDispositionFilename(
      'attachment; filename="Remision_13.pdf"',
      'Remision.pdf'
    ),
    'Remision_13.pdf'
  )
})

test('blob API errors expose the backend message without breaking the page', async () => {
  const error = {
    response: {
      data: {
        text: async () => JSON.stringify({ message: 'Quotation not found' }),
      },
    },
  }
  assert.equal(
    await resolveBlobApiErrorMessage(error, 'No fue posible generar el PDF.'),
    'Quotation not found'
  )
})
