import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
} from '@mui/material'

function ConfirmFinanceActionDialog({
  open,
  title,
  description,
  confirmLabel,
  submittingLabel,
  confirmColor = 'primary',
  onClose,
  onConfirm,
  submitting,
  errorMessage,
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
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          <DialogContentText>{description}</DialogContentText>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Volver
        </Button>
        <Button
          type="button"
          variant="contained"
          color={confirmColor}
          onClick={onConfirm}
          disabled={submitting}
        >
          {submitting ? submittingLabel : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ConfirmFinanceActionDialog
