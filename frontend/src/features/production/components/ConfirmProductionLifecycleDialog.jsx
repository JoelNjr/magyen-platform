import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'

function ConfirmProductionLifecycleDialog({
  open,
  title,
  description,
  confirmLabel,
  submittingLabel,
  onClose,
  onConfirm,
  submitting,
  errorMessage,
  dateLabel,
  dateValue,
  onDateChange,
}) {
  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>
        <Stack spacing={2}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
          <DialogContentText>{description}</DialogContentText>
          {dateLabel ? (
            <TextField
              label={dateLabel}
              type="date"
              value={dateValue || ''}
              onChange={(event) => onDateChange?.(event.target.value)}
              InputLabelProps={{ shrink: true }}
              fullWidth
              disabled={submitting}
              helperText="Puede ser una fecha histórica. No se sobrescribe con la fecha de hoy."
            />
          ) : null}
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={onConfirm}
          disabled={submitting || (dateLabel && !dateValue)}
        >
          {submitting ? submittingLabel : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ConfirmProductionLifecycleDialog
