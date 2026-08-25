import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  Typography,
} from '@mui/material'
import ConfirmProductionLifecycleDialog from './ConfirmProductionLifecycleDialog'
import {
  deleteProductionReferenceImage,
  getProductionReferenceImageBlob,
  replaceProductionReferenceImage,
} from '../services/productionService'

const MAX_SIZE_BYTES = 5 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png']

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function validateSelectedFile(file) {
  if (!file) {
    return 'Selecciona una imagen JPEG o PNG.'
  }
  if (file.size > MAX_SIZE_BYTES) {
    return 'La imagen no puede superar 5 MB.'
  }
  const type = (file.type || '').toLowerCase()
  const name = (file.name || '').toLowerCase()
  const typeAllowed = ALLOWED_TYPES.includes(type)
  const extensionAllowed =
    name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.png')
  if (!typeAllowed && !extensionAllowed) {
    return 'Solo se permiten imágenes JPEG o PNG.'
  }
  return ''
}

function ProductionReferenceImageSection({
  productionOrderId,
  hasReferenceImage,
  onChanged,
}) {
  const fileInputRef = useRef(null)
  const [storedPreviewUrl, setStoredPreviewUrl] = useState('')
  const [selectedFile, setSelectedFile] = useState(null)
  const [selectedPreviewUrl, setSelectedPreviewUrl] = useState('')
  const [loadingStored, setLoadingStored] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [removing, setRemoving] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [removeDialogOpen, setRemoveDialogOpen] = useState(false)
  const [removeError, setRemoveError] = useState('')

  useEffect(() => {
    let objectUrl = ''
    let cancelled = false

    if (!hasReferenceImage) {
      setStoredPreviewUrl('')
      setLoadingStored(false)
      return undefined
    }

    setLoadingStored(true)
    getProductionReferenceImageBlob(productionOrderId)
      .then((blob) => {
        if (cancelled) {
          return
        }
        objectUrl = URL.createObjectURL(blob)
        setStoredPreviewUrl(objectUrl)
        setErrorMessage('')
      })
      .catch((error) => {
        if (!cancelled) {
          setStoredPreviewUrl('')
          setErrorMessage(
            resolveApiErrorMessage(
              error,
              'No fue posible cargar la imagen de referencia.'
            )
          )
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingStored(false)
        }
      })

    return () => {
      cancelled = true
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [productionOrderId, hasReferenceImage])

  useEffect(() => {
    return () => {
      if (selectedPreviewUrl) {
        URL.revokeObjectURL(selectedPreviewUrl)
      }
    }
  }, [selectedPreviewUrl])

  function clearSelection() {
    if (selectedPreviewUrl) {
      URL.revokeObjectURL(selectedPreviewUrl)
    }
    setSelectedFile(null)
    setSelectedPreviewUrl('')
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  function handleFileChange(event) {
    const file = event.target.files?.[0]
    if (!file) {
      return
    }

    const validationMessage = validateSelectedFile(file)
    if (validationMessage) {
      setErrorMessage(validationMessage)
      clearSelection()
      return
    }

    if (selectedPreviewUrl) {
      URL.revokeObjectURL(selectedPreviewUrl)
    }
    setErrorMessage('')
    setSelectedFile(file)
    setSelectedPreviewUrl(URL.createObjectURL(file))
  }

  async function handleUpload() {
    const validationMessage = validateSelectedFile(selectedFile)
    if (validationMessage) {
      setErrorMessage(validationMessage)
      return
    }

    setUploading(true)
    setErrorMessage('')
    try {
      await replaceProductionReferenceImage(productionOrderId, selectedFile)
      clearSelection()
      await onChanged?.('Imagen de referencia guardada correctamente.')
    } catch (error) {
      setErrorMessage(
        resolveApiErrorMessage(error, 'No fue posible guardar la imagen de referencia.')
      )
    } finally {
      setUploading(false)
    }
  }

  async function handleRemoveConfirm() {
    setRemoving(true)
    setRemoveError('')
    try {
      await deleteProductionReferenceImage(productionOrderId)
      setRemoveDialogOpen(false)
      await onChanged?.('Imagen de referencia eliminada correctamente.')
    } catch (error) {
      setRemoveError(
        resolveApiErrorMessage(error, 'No fue posible eliminar la imagen de referencia.')
      )
    } finally {
      setRemoving(false)
    }
  }

  const previewUrl = selectedPreviewUrl || storedPreviewUrl
  const busy = uploading || removing

  return (
    <Paper sx={{ p: 3 }}>
      <Stack spacing={2}>
        <Stack spacing={0.5}>
          <Typography variant="h5">Imagen de referencia</Typography>
          <Typography variant="body2" color="text.secondary">
            Diseño, mockup o referencia visual del pedido. JPEG o PNG, máximo 5 MB.
          </Typography>
        </Stack>

        {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}

        {loadingStored ? (
          <Typography color="text.secondary">Cargando imagen…</Typography>
        ) : null}

        {previewUrl ? (
          <Box
            component="img"
            src={previewUrl}
            alt="Imagen de referencia"
            sx={{
              maxWidth: '100%',
              maxHeight: 360,
              objectFit: 'contain',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 1,
              bgcolor: 'background.default',
            }}
          />
        ) : (
          <Typography color="text.secondary">
            Esta orden no tiene imagen de referencia.
          </Typography>
        )}

        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,.jpg,.jpeg,.png"
          hidden
          onChange={handleFileChange}
        />

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <Button
            variant="outlined"
            onClick={() => fileInputRef.current?.click()}
            disabled={busy}
          >
            {hasReferenceImage || selectedFile ? 'Seleccionar otra imagen' : 'Seleccionar imagen'}
          </Button>
          <Button
            variant="contained"
            onClick={handleUpload}
            disabled={busy || !selectedFile}
          >
            {uploading
              ? 'Guardando…'
              : hasReferenceImage
                ? 'Reemplazar imagen'
                : 'Cargar imagen'}
          </Button>
          {hasReferenceImage ? (
            <Button
              color="error"
              variant="outlined"
              onClick={() => {
                setRemoveError('')
                setRemoveDialogOpen(true)
              }}
              disabled={busy}
            >
              Eliminar imagen
            </Button>
          ) : null}
        </Stack>
      </Stack>

      <ConfirmProductionLifecycleDialog
        open={removeDialogOpen}
        title="Eliminar imagen de referencia"
        description="Se eliminará la imagen de referencia de esta orden. La orden de producción no se elimina."
        confirmLabel="Eliminar"
        submittingLabel="Eliminando..."
        onClose={() => {
          if (!removing) {
            setRemoveDialogOpen(false)
            setRemoveError('')
          }
        }}
        onConfirm={handleRemoveConfirm}
        submitting={removing}
        errorMessage={removeError}
      />
    </Paper>
  )
}

export default ProductionReferenceImageSection
