import { useState } from 'react'
import {
  Alert,
  Button,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { createQuotation } from '../services/commercialService'

function CreateQuotationPage() {
  const navigate = useNavigate()
  const [customerId, setCustomerId] = useState('')
  const [deliveryDate, setDeliveryDate] = useState('')
  const [salesperson, setSalesperson] = useState('')
  const [observations, setObservations] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [failed, setFailed] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setFailed(false)
    setSubmitting(true)

    const payload = {
      customerId,
      deliveryDate,
      salesperson,
      observations,
    }

    try {
      await createQuotation(payload)
      navigate('/commercial')
    } catch {
      setFailed(true)
      setSubmitting(false)
    }
  }

  return (
    <Paper
      sx={{
        p: 4,
        maxWidth: 640,
        mx: 'auto',
      }}
    >
      <form onSubmit={handleSubmit}>
        <Stack spacing={3}>
          <Typography variant="h4">Nueva Cotización</Typography>

          {failed && (
            <Alert severity="error">No fue posible crear la cotización.</Alert>
          )}

          <TextField
            label="Cliente"
            value={customerId}
            onChange={(event) => setCustomerId(event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Fecha de entrega"
            type="date"
            value={deliveryDate}
            onChange={(event) => setDeliveryDate(event.target.value)}
            fullWidth
            disabled={submitting}
            slotProps={{ inputLabel: { shrink: true } }}
          />

          <TextField
            label="Vendedor"
            value={salesperson}
            onChange={(event) => setSalesperson(event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Observaciones"
            value={observations}
            onChange={(event) => setObservations(event.target.value)}
            fullWidth
            multiline
            minRows={3}
            disabled={submitting}
          />

          <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-end' }}>
            <Button
              type="button"
              variant="outlined"
              disabled={submitting}
              onClick={() => navigate('/commercial')}
            >
              Cancelar
            </Button>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? 'Guardando...' : 'Guardar'}
            </Button>
          </Stack>
        </Stack>
      </form>
    </Paper>
  )
}

export default CreateQuotationPage
