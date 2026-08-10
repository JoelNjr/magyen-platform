import { useEffect, useState } from 'react'
import PrintOutlinedIcon from '@mui/icons-material/PrintOutlined'
import {
  Alert,
  Button,
  Chip,
  Grid,
  Link,
  Paper,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { getCustomers } from '../../commercial/services/commercialService'
import {
  getInventoryItem,
  getInventoryMovements,
} from '../../inventory/services/inventoryService'
import {
  formatPlotterDate,
  formatPlotterMoney,
  formatPlotterNumber,
  getPlotterStatusChipProps,
} from '../presentation/plotterJobPresentation'
import { getPlotterJob } from '../services/plotterService'

function DetailField({ label, children }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      {children}
    </Stack>
  )
}

function PlotterJobDetailPage() {
  const { plotterJobId } = useParams()
  const navigate = useNavigate()

  const [job, setJob] = useState(null)
  const [customerName, setCustomerName] = useState('')
  const [paperLabel, setPaperLabel] = useState('')
  const [legacyPaper, setLegacyPaper] = useState(false)
  const [materialCost, setMaterialCost] = useState(null)
  const [consumedMeters, setConsumedMeters] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function loadDetail() {
      setLoading(true)
      setFailed(false)
      setNotFound(false)
      setLegacyPaper(false)
      setMaterialCost(null)
      setConsumedMeters(null)

      try {
        const [plotterJob, customersData] = await Promise.all([
          getPlotterJob(plotterJobId),
          getCustomers().catch(() => ({ customers: [] })),
        ])

        if (cancelled) {
          return
        }

        setJob(plotterJob)

        const customers = Array.isArray(customersData?.customers)
          ? customersData.customers
          : []
        const matchedCustomer = customers.find(
          (customer) => customer.customerId === plotterJob.customerId
        )
        setCustomerName(matchedCustomer?.name || plotterJob.customerId)

        try {
          const inventoryItem = await getInventoryItem(
            plotterJob.paperInventoryItemId
          )
          if (inventoryItem?.plotterPaperRoll && inventoryItem?.paperRollNumber) {
            setPaperLabel(inventoryItem.paperRollNumber)
            setLegacyPaper(false)
          } else {
            setPaperLabel(
              inventoryItem?.name ||
                inventoryItem?.materialCode ||
                plotterJob.paperInventoryItemId
            )
            setLegacyPaper(true)
          }

          const movementsData = await getInventoryMovements(
            plotterJob.paperInventoryItemId
          )
          const movements = Array.isArray(movementsData?.movements)
            ? movementsData.movements
            : []
          const matchingMovement = movements.find(
            (movement) =>
              movement.sourceType === 'PLOTTER' &&
              movement.sourceId === plotterJob.plotterJobId
          )

          if (matchingMovement) {
            setConsumedMeters(matchingMovement.quantity)
            setMaterialCost(matchingMovement.totalCost)
          }
        } catch {
          setPaperLabel(plotterJob.paperInventoryItemId)
          setLegacyPaper(true)
        }

        setLoading(false)
      } catch (error) {
        if (cancelled) {
          return
        }

        if (error?.response?.status === 400 || error?.response?.status === 404) {
          setNotFound(true)
        } else {
          setFailed(true)
        }
        setJob(null)
        setLoading(false)
      }
    }

    loadDetail()

    return () => {
      cancelled = true
    }
  }, [plotterJobId])

  if (loading) {
    return (
      <Stack spacing={3}>
        <Skeleton variant="text" width={280} height={48} />
        <Paper sx={{ p: 3 }}>
          <Skeleton variant="rectangular" height={180} />
        </Paper>
      </Stack>
    )
  }

  if (notFound) {
    return (
      <Stack spacing={2}>
        <Alert severity="warning">No se encontró el trabajo de plotter.</Alert>
        <Button onClick={() => navigate('/plotter')}>Volver a Plotter</Button>
      </Stack>
    )
  }

  if (failed || !job) {
    return (
      <Stack spacing={2}>
        <Alert severity="error">
          No fue posible cargar el detalle del trabajo de plotter.
        </Alert>
        <Button onClick={() => navigate('/plotter')}>Volver a Plotter</Button>
      </Stack>
    )
  }

  const statusChip = getPlotterStatusChipProps(job.status)

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Stack spacing={0.5}>
          <Typography variant="h3">Trabajo de Plotter</Typography>
          <Typography variant="body2" color="text.secondary">
            {plotterJobId}
          </Typography>
        </Stack>
        <Button onClick={() => navigate('/plotter')}>Volver</Button>
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Cliente">
              <Typography>{customerName}</Typography>
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Fecha">
              <Typography>{formatPlotterDate(job.creationDate)}</Typography>
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Estado">
              <Chip size="small" {...statusChip} />
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Rollo utilizado">
              <Typography>
                {paperLabel}
                {legacyPaper ? ' (histórico / legado)' : ''}
              </Typography>
              {!legacyPaper && (
                <Link
                  component={RouterLink}
                  to={`/inventory/${job.paperInventoryItemId}`}
                  underline="hover"
                >
                  Ver inventario
                </Link>
              )}
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Metros impresos">
              <Typography>{formatPlotterNumber(job.printedMeters)} m</Typography>
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Precio por metro">
              <Typography>{formatPlotterMoney(job.pricePerMeter)}</Typography>
            </DetailField>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <DetailField label="Total cobrado">
              <Typography variant="h6">
                {formatPlotterMoney(job.totalAmount)}
              </Typography>
            </DetailField>
          </Grid>
          <Grid item xs={12}>
            <DetailField label="Observaciones">
              <Typography>{job.observations || '—'}</Typography>
            </DetailField>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1} alignItems="center">
            <PrintOutlinedIcon color="action" />
            <Typography variant="h6">Consumo de material</Typography>
          </Stack>
          {legacyPaper && !materialCost && consumedMeters === null ? (
            <Alert severity="info">
              Este trabajo histórico no tiene consumo de inventario asociado. Los
              trabajos nuevos consumen el rollo de papel seleccionado.
            </Alert>
          ) : (
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <DetailField label="Rollo">
                  <Typography>{paperLabel}</Typography>
                </DetailField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <DetailField label="Consumido">
                  <Typography>
                    {formatPlotterNumber(
                      consumedMeters ?? job.printedMeters
                    )}{' '}
                    m
                  </Typography>
                </DetailField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <DetailField label="Costo material">
                  <Typography>
                    {materialCost === null || materialCost === undefined
                      ? 'Sin valoración histórica'
                      : formatPlotterMoney(materialCost)}
                  </Typography>
                </DetailField>
              </Grid>
            </Grid>
          )}
        </Stack>
      </Paper>
    </Stack>
  )
}

export default PlotterJobDetailPage
