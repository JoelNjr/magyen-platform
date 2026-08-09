import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'

function AssignProductionOperatorDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  initialOperator = '',
}) {
  const [assignedOperator, setAssignedOperator] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setAssignedOperator('')
      setValidationError('')
      return
    }

    setAssignedOperator(initialOperator || '')
    setValidationError('')
  }, [open, initialOperator])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmedOperator = assignedOperator.trim()

    if (!trimmedOperator) {
      setValidationError('El operario es obligatorio.')
      return
    }

    setValidationError('')
    onSubmit({ assignedOperator: trimmedOperator })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Asignar operario</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            label="Operario"
            value={assignedOperator}
            onChange={(event) => {
              setAssignedOperator(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            autoFocus
            helperText="Nombre del operario responsable (texto)."
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Asignando...' : 'Asignar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AssignProductionOperatorDialog
