import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { getPlotterPaperRolls } from '../../inventory/services/inventoryService'
import {
  calculatePlotterTotalPreview,
  formatPlotterMoney,
  formatPlotterNumber,
  isExternalPlotterJob,
  isInternalPlotterJob,
  isWastePlotterJob,
} from '../presentation/plotterJobPresentation'
import { eligiblePaperRolls, formatPlotterMachineLabel } from '../presentation/plotterMachinePresentation'
import { getPlotterMachines } from '../services/plotterService'

function EditPlotterJobDialog({ open, job, onClose, onSubmit, submitting, errorMessage }) {
  const [form, setForm] = useState(null)
  const [validationError, setValidationError] = useState('')
  const [paperRolls, setPaperRolls] = useState([])
  const [machines, setMachines] = useState([])

  useEffect(() => {
    if (!open || !job) {
      setForm(null)
      setValidationError('')
      return
    }
    setForm({
      creationDate: job.creationDate || '',
      paperInventoryItemId: job.paperInventoryItemId || '',
      printedMeters: job.printedMeters == null ? '' : String(job.printedMeters),
      pricePerMeter: job.pricePerMeter == null ? '' : String(job.pricePerMeter),
      commercialValue: job.totalAmount == null ? '' : String(job.totalAmount),
      observations: job.observations || '',
    })
    setValidationError('')
  }, [open, job])

  useEffect(() => {
    if (!open) {
      return undefined
    }
    let active = true
    Promise.all([getPlotterPaperRolls(), getPlotterMachines()])
      .then(([rollsData, machinesData]) => {
        if (!active) {
          return
        }
        setPaperRolls(Array.isArray(rollsData?.items) ? rollsData.items : [])
        setMachines(Array.isArray(machinesData?.machines) ? machinesData.machines : [])
      })
      .catch(() => {
        if (active) {
          setPaperRolls([])
          setMachines([])
        }
      })
    return () => {
      active = false
    }
  }, [open])

  const rolls = useMemo(
    () => eligiblePaperRolls(paperRolls, job?.plotterMachineId),
    [paperRolls, job?.plotterMachineId]
  )
  const external = isExternalPlotterJob(job?.jobType)
  const internal = isInternalPlotterJob(job?.jobType)
  const waste = isWastePlotterJob(job?.jobType)
  const suggested = calculatePlotterTotalPreview(form?.printedMeters, form?.pricePerMeter)

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setValidationError('')
  }

  function handleSubmit() {
    if (submitting || !form || !job) {
      return
    }
    const printedMeters = Number(form.printedMeters)
    if (!form.creationDate || Number.isNaN(printedMeters) || printedMeters <= 0) {
      setValidationError('La fecha de trabajo y los metros impresos son obligatorios.')
      return
    }
    if (!form.paperInventoryItemId) {
      setValidationError('Selecciona el rollo de la misma máquina.')
      return
    }
    let pricePerMeter = 0
    if (!waste) {
      pricePerMeter = Number(form.pricePerMeter)
      if (Number.isNaN(pricePerMeter) || pricePerMeter <= 0) {
        setValidationError('El precio por metro debe ser mayor que cero.')
        return
      }
    }
    let commercialValue
    if (external) {
      commercialValue = Number(form.commercialValue)
      if (Number.isNaN(commercialValue) || commercialValue <= 0) {
        setValidationError('El valor comercial debe ser mayor que cero.')
        return
      }
    }
    onSubmit({
      creationDate: form.creationDate,
      paperInventoryItemId: form.paperInventoryItemId,
      printedMeters,
      pricePerMeter,
      commercialValue,
      observations: form.observations.trim() || null,
      plotterMachineId: job.plotterMachineId,
    })
  }

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>Corregir trabajo de plotter</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Typography variant="body2" color="text.secondary">
            Máquina: {formatPlotterMachineLabel(job?.plotterMachineId, job?.machineName || machines.find((machine) => machine.plotterMachineId === job?.plotterMachineId)?.name)}
          </Typography>
          <TextField
            label="Fecha de trabajo"
            type="date"
            value={form?.creationDate || ''}
            onChange={(event) => updateField('creationDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            select
            label="Rollo de la máquina"
            value={form?.paperInventoryItemId || ''}
            onChange={(event) => updateField('paperInventoryItemId', event.target.value)}
            fullWidth
            disabled={submitting}
            helperText="Solo aparecen rollos asignados a la máquina del trabajo."
          >
            {form?.paperInventoryItemId &&
              !rolls.some((roll) => roll.inventoryItemId === form.paperInventoryItemId) && (
                <MenuItem value={form.paperInventoryItemId}>Rollo actual</MenuItem>
              )}
            {rolls.map((roll) => (
              <MenuItem key={roll.inventoryItemId} value={roll.inventoryItemId}>
                {roll.paperRollNumber || roll.name || roll.materialCode} — {formatPlotterNumber(roll.stock)} m
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Metros impresos"
            value={form?.printedMeters || ''}
            onChange={(event) => updateField('printedMeters', event.target.value)}
            fullWidth
            disabled={submitting}
          />
          {!waste && (
            <TextField
              label="Precio por metro"
              value={form?.pricePerMeter || ''}
              onChange={(event) => updateField('pricePerMeter', event.target.value)}
              fullWidth
              disabled={submitting}
              helperText={
                internal
                  ? 'El valor interno sigue siendo metros × precio.'
                  : 'Precio de referencia. El débito de la cuenta es el valor comercial.'
              }
            />
          )}
          {external && (
            <TextField
              label="Valor comercial"
              value={form?.commercialValue || ''}
              onChange={(event) => updateField('commercialValue', event.target.value)}
              fullWidth
              disabled={submitting}
              helperText={
                suggested == null
                  ? 'Este valor actualiza el débito de la cuenta una sola vez.'
                  : `Sugerido: ${formatPlotterMoney(suggested)}. El débito de la cuenta usa este valor.`
              }
            />
          )}
          <TextField
            label="Observaciones"
            value={form?.observations || ''}
            onChange={(event) => updateField('observations', event.target.value)}
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button variant="contained" onClick={handleSubmit} disabled={submitting || !form}>
          {submitting ? 'Guardando…' : 'Guardar corrección'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default EditPlotterJobDialog
