import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined'
import {
  Alert,
  Button,
  Chip,
  Divider,
  Grid,
  Paper,
  Skeleton,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import { useNavigate, useParams, Link as RouterLink } from 'react-router-dom'
import AddProductionOperationDialog from '../components/AddProductionOperationDialog'
import AssignProductionOperatorDialog from '../components/AssignProductionOperatorDialog'
import ConfirmProductionLifecycleDialog from '../components/ConfirmProductionLifecycleDialog'
import PlanProductionOrderDialog from '../components/PlanProductionOrderDialog'
import RegisterProductionLaborDialog from '../components/RegisterProductionLaborDialog'
import RegisterProductionMaterialConsumptionDialog from '../components/RegisterProductionMaterialConsumptionDialog'
import { getInventoryItems } from '../../inventory/services/inventoryService'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { resolveProductionBusinessLabel } from '../presentation/resolveProductionBusinessLabel'
import { formatCuffRequired } from '../../commercial/presentation/commercialCatalogs'
import {
  formatProductionOperationType,
  getProductionOperationStatusChipProps,
  getProductionOrderStatusChipProps,
  getProductionPriorityChipProps,
} from '../presentation/productionStatusPresentation'
import {
  formatProductionMaterialCost,
  formatProductionMaterialCostOrUnvalued,
} from '../presentation/productionCostPresentation'
import {
  addProductionOperation,
  assignProductionOperationOperator,
  cancelProductionLaborWork,
  completeProductionOperation,
  completeProductionOrder,
  getEligibleProductionLaborOperators,
  getProductionLaborWorks,
  getProductionMaterialConsumptions,
  getProductionOrder,
  payProductionLaborWork,
  planProductionOrder,
  registerProductionLaborWork,
  registerProductionMaterialConsumption,
  startProductionOperation,
  startProductionOrder,
} from '../services/productionService'

function getLaborStatusLabel(status) {
  if (status === 'PENDING') return 'Pendiente'
  if (status === 'PAID') return 'Pagado'
  if (status === 'CANCELLED') return 'Cancelado'
  return status || '—'
}

function getLaborStatusColor(status) {
  if (status === 'PENDING') return 'warning'
  if (status === 'PAID') return 'success'
  if (status === 'CANCELLED') return 'default'
  return 'default'
}

const headerCellSx = { fontWeight: 'bold' }

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatYesNo(value) {
  return value ? 'Sí' : 'No'
}

function sumRegisteredSizes(sizes) {
  if (!Array.isArray(sizes)) {
    return 0
  }

  return sizes.reduce((sum, sizeEntry) => sum + Number(sizeEntry.quantity || 0), 0)
}

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

function ProductionItemSpecificationSection({ specification }) {
  if (!specification) {
    return (
      <Typography color="text.secondary">
        Sin especificación registrada.
      </Typography>
    )
  }

  return (
    <Stack spacing={1.5}>
      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
        Especificaciones
      </Typography>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <DetailField label="Tipo de prenda">
            <Typography>{specification.garmentType || '—'}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <DetailField label="Tipo de cuello">
            <Typography>{specification.collarType || '—'}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <DetailField label="Tipo de manga">
            <Typography>{specification.sleeveType || '—'}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <DetailField label="Lleva puño">
            <Typography>{formatCuffRequired(specification.cuffRequired)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="Sublimación">
            <Typography>{formatYesNo(specification.sublimationRequired)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="Bordado">
            <Typography>{formatYesNo(specification.embroideryRequired)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="DTF">
            <Typography>{formatYesNo(specification.dtfRequired)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="Incluye nombres">
            <Typography>{formatYesNo(specification.includesNames)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="Incluye números">
            <Typography>{formatYesNo(specification.includesNumbers)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <DetailField label="Incluye logos">
            <Typography>{formatYesNo(specification.includesLogos)}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <DetailField label="Notas de decoración">
            <Typography>{specification.decorationNotes || '—'}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <DetailField label="Notas de personalización">
            <Typography>{specification.personalizationNotes || '—'}</Typography>
          </DetailField>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <DetailField label="Observaciones">
            <Typography>{specification.itemObservations || '—'}</Typography>
          </DetailField>
        </Grid>
      </Grid>
    </Stack>
  )
}

function ProductionItemSizesSection({ item }) {
  const sizes = Array.isArray(item.sizes) ? item.sizes : []
  const registeredQuantity = sumRegisteredSizes(sizes)

  return (
    <Stack spacing={1.5}>
      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
        Tallas
      </Typography>

      {sizes.length === 0 ? (
        <Typography color="text.secondary">
          Tallas pendientes de registrar.
        </Typography>
      ) : (
        <Stack spacing={0.5}>
          {sizes.map((sizeEntry) => (
            <Typography key={`${item.productionItemId}-${sizeEntry.size}`}>
              {sizeEntry.size}: {sizeEntry.quantity}
            </Typography>
          ))}
        </Stack>
      )}

      <Typography variant="body2" color="text.secondary">
        Total registrado: {registeredQuantity} / {item.quantity}
      </Typography>
    </Stack>
  )
}

function ProductionOrderDetailLoadingSkeleton() {
  return (
    <>
      <Stack spacing={1}>
        <Skeleton width={180} height={24} />
        <Stack direction="row" spacing={1.5} alignItems="center">
          <Skeleton width={260} height={40} />
          <Skeleton width={90} height={28} sx={{ borderRadius: 4 }} />
        </Stack>
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          {Array.from({ length: 6 }).map((_, index) => (
            <Grid key={`production-detail-skeleton-${index}`} size={{ xs: 12, md: 6 }}>
              <Stack spacing={0.5}>
                <Skeleton width={100} height={20} />
                <Skeleton width="70%" height={28} />
              </Stack>
            </Grid>
          ))}
        </Grid>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Stack spacing={3}>
          <Skeleton width={140} height={32} />
          <Skeleton variant="rounded" height={140} />
        </Stack>
      </Paper>
    </>
  )
}

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function ProductionOrderDetailPage() {
  const { productionOrderId } = useParams()
  const navigate = useNavigate()
  const [productionOrder, setProductionOrder] = useState(null)
  const [materialConsumptions, setMaterialConsumptions] = useState([])
  const [materialCostSummary, setMaterialCostSummary] = useState(null)
  const [laborWorks, setLaborWorks] = useState([])
  const [laborCostSummary, setLaborCostSummary] = useState(null)
  const [laborOperators, setLaborOperators] = useState([])
  const [laborOperatorsLoading, setLaborOperatorsLoading] = useState(false)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)

  const [planDialogOpen, setPlanDialogOpen] = useState(false)
  const [startDialogOpen, setStartDialogOpen] = useState(false)
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false)

  const [planning, setPlanning] = useState(false)
  const [starting, setStarting] = useState(false)
  const [completing, setCompleting] = useState(false)

  const [planError, setPlanError] = useState('')
  const [startError, setStartError] = useState('')
  const [completeError, setCompleteError] = useState('')
  const [startDate, setStartDate] = useState('')
  const [completeDate, setCompleteDate] = useState('')
  const [payLaborDate, setPayLaborDate] = useState('')
  const [consumeDialogOpen, setConsumeDialogOpen] = useState(false)
  const [consumingMaterial, setConsumingMaterial] = useState(false)
  const [consumeError, setConsumeError] = useState('')
  const [inventoryItems, setInventoryItems] = useState([])
  const [inventoryItemsLoading, setInventoryItemsLoading] = useState(false)

  const [addOperationDialogOpen, setAddOperationDialogOpen] = useState(false)
  const [assignOperatorDialogOpen, setAssignOperatorDialogOpen] = useState(false)
  const [startOperationDialogOpen, setStartOperationDialogOpen] = useState(false)
  const [completeOperationDialogOpen, setCompleteOperationDialogOpen] =
    useState(false)

  const [selectedOperation, setSelectedOperation] = useState(null)

  const [addingOperation, setAddingOperation] = useState(false)
  const [assigningOperator, setAssigningOperator] = useState(false)
  const [startingOperation, setStartingOperation] = useState(false)
  const [completingOperation, setCompletingOperation] = useState(false)

  const [addOperationError, setAddOperationError] = useState('')
  const [assignOperatorError, setAssignOperatorError] = useState('')
  const [startOperationError, setStartOperationError] = useState('')
  const [completeOperationError, setCompleteOperationError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const [registerLaborOpen, setRegisterLaborOpen] = useState(false)
  const [registeringLabor, setRegisteringLabor] = useState(false)
  const [registerLaborError, setRegisterLaborError] = useState('')

  const [payLaborTarget, setPayLaborTarget] = useState(null)
  const [payingLabor, setPayingLabor] = useState(false)
  const [payLaborError, setPayLaborError] = useState('')

  const [cancelLaborTarget, setCancelLaborTarget] = useState(null)
  const [cancellingLabor, setCancellingLabor] = useState(false)
  const [cancelLaborError, setCancelLaborError] = useState('')

  async function loadMaterialCostAttribution() {
    const consumptionsResponse =
      await getProductionMaterialConsumptions(productionOrderId)
    setMaterialConsumptions(consumptionsResponse.consumptions ?? [])
    setMaterialCostSummary(consumptionsResponse.materialCostSummary ?? null)
  }

  async function loadLaborAttribution() {
    const laborResponse = await getProductionLaborWorks(productionOrderId)
    setLaborWorks(laborResponse.laborWorks ?? [])
    setLaborCostSummary(laborResponse.laborCostSummary ?? null)
  }

  async function refreshProductionOrder() {
    const data = await getProductionOrder(productionOrderId)
    setProductionOrder(data)
    await Promise.all([loadMaterialCostAttribution(), loadLaborAttribution()])
    return data
  }

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setProductionOrder(null)
    setMaterialConsumptions([])
    setMaterialCostSummary(null)
    setLaborWorks([])
    setLaborCostSummary(null)

    Promise.all([
      getProductionOrder(productionOrderId),
      getProductionMaterialConsumptions(productionOrderId),
      getProductionLaborWorks(productionOrderId),
    ])
      .then(([orderData, consumptionsResponse, laborResponse]) => {
        setProductionOrder(orderData)
        setMaterialConsumptions(consumptionsResponse.consumptions ?? [])
        setMaterialCostSummary(
          consumptionsResponse.materialCostSummary ??
            orderData.materialCostSummary ??
            null
        )
        setLaborWorks(laborResponse.laborWorks ?? [])
        setLaborCostSummary(
          laborResponse.laborCostSummary ?? orderData.laborCostSummary ?? null
        )
        setLoading(false)
      })
      .catch((error) => {
        const statusCode = error.response?.status

        if (statusCode === 400 || statusCode === 404) {
          setNotFound(true)
        } else {
          setFailed(true)
        }

        setLoading(false)
      })
  }, [productionOrderId])

  const items = productionOrder?.items ?? []
  const operations = productionOrder?.operations ?? []
  const status = productionOrder?.status
  const lifecycleBusy = planning || starting || completing
  const operationBusy =
    addingOperation ||
    assigningOperator ||
    startingOperation ||
    completingOperation
  const laborBusy = registeringLabor || payingLabor || cancellingLabor
  const pageBusy = lifecycleBusy || operationBusy || laborBusy
  const canAddOperation = status === 'CREATED'
  const orderAllowsOperationExecution = status === 'IN_PROGRESS'
  const canRegisterLabor = status === 'IN_PROGRESS'
  const usedOperationTypes = operations.map((operation) => operation.type)

  function openPlanDialog() {
    if (pageBusy) {
      return
    }

    setPlanError('')
    setPlanDialogOpen(true)
  }

  function closePlanDialog() {
    if (planning) {
      return
    }

    setPlanDialogOpen(false)
    setPlanError('')
  }

  function openStartDialog() {
    if (pageBusy) {
      return
    }

    setStartError('')
    setStartDate(productionOrder?.plannedStartDate || toIsoDate())
    setStartDialogOpen(true)
  }

  function closeStartDialog() {
    if (starting) {
      return
    }

    setStartDialogOpen(false)
    setStartError('')
  }

  function openCompleteDialog() {
    if (pageBusy) {
      return
    }

    setCompleteError('')
    setCompleteDate(productionOrder?.plannedEndDate || toIsoDate())
    setCompleteDialogOpen(true)
  }

  function closeCompleteDialog() {
    if (completing) {
      return
    }

    setCompleteDialogOpen(false)
    setCompleteError('')
  }

  function openAddOperationDialog() {
    if (pageBusy || !canAddOperation) {
      return
    }

    setAddOperationError('')
    setAddOperationDialogOpen(true)
  }

  function closeAddOperationDialog() {
    if (addingOperation) {
      return
    }

    setAddOperationDialogOpen(false)
    setAddOperationError('')
  }

  function openAssignOperatorDialog(operation) {
    if (pageBusy) {
      return
    }

    setSelectedOperation(operation)
    setAssignOperatorError('')
    setAssignOperatorDialogOpen(true)
  }

  function closeAssignOperatorDialog() {
    if (assigningOperator) {
      return
    }

    setAssignOperatorDialogOpen(false)
    setAssignOperatorError('')
    setSelectedOperation(null)
  }

  function openStartOperationDialog(operation) {
    if (pageBusy || !orderAllowsOperationExecution) {
      return
    }

    setSelectedOperation(operation)
    setStartOperationError('')
    setStartOperationDialogOpen(true)
  }

  function closeStartOperationDialog() {
    if (startingOperation) {
      return
    }

    setStartOperationDialogOpen(false)
    setStartOperationError('')
    setSelectedOperation(null)
  }

  function openCompleteOperationDialog(operation) {
    if (pageBusy || !orderAllowsOperationExecution) {
      return
    }

    setSelectedOperation(operation)
    setCompleteOperationError('')
    setCompleteOperationDialogOpen(true)
  }

  function closeCompleteOperationDialog() {
    if (completingOperation) {
      return
    }

    setCompleteOperationDialogOpen(false)
    setCompleteOperationError('')
    setSelectedOperation(null)
  }

  async function handlePlanSubmit(payload) {
    if (planning) {
      return
    }

    setPlanError('')
    setPlanning(true)

    try {
      await planProductionOrder(productionOrder.productionOrderId, payload)
      await refreshProductionOrder()
      setPlanDialogOpen(false)
      setSuccessMessage('Orden de producción planificada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setPlanError(
        resolveApiErrorMessage(
          error,
          'No fue posible planificar la orden de producción.'
        )
      )
    } finally {
      setPlanning(false)
    }
  }

  async function handleStartConfirm() {
    if (starting) {
      return
    }

    setStartError('')
    setStarting(true)

    try {
      await startProductionOrder(productionOrder.productionOrderId, {
        actualStartDate: startDate,
      })
      await refreshProductionOrder()
      setStartDialogOpen(false)
      setSuccessMessage('Orden de producción iniciada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setStartError(
        resolveApiErrorMessage(
          error,
          'No fue posible iniciar la orden de producción.'
        )
      )
    } finally {
      setStarting(false)
    }
  }

  async function handleCompleteConfirm() {
    if (completing) {
      return
    }

    setCompleteError('')
    setCompleting(true)

    try {
      await completeProductionOrder(productionOrder.productionOrderId, {
        actualCompletionDate: completeDate,
      })
      await refreshProductionOrder()
      setCompleteDialogOpen(false)
      setSuccessMessage('Orden de producción completada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setCompleteError(
        resolveApiErrorMessage(
          error,
          'No fue posible completar la orden de producción.'
        )
      )
    } finally {
      setCompleting(false)
    }
  }

  async function openRegisterLaborDialog() {
    if (pageBusy || !canRegisterLabor) {
      return
    }

    setRegisterLaborError('')
    setRegisterLaborOpen(true)
    setLaborOperatorsLoading(true)
    try {
      const data = await getEligibleProductionLaborOperators()
      setLaborOperators(Array.isArray(data?.operators) ? data.operators : [])
    } catch {
      setLaborOperators([])
      setRegisterLaborError('No fue posible cargar los operarios disponibles.')
    } finally {
      setLaborOperatorsLoading(false)
    }
  }

  async function handleRegisterLaborSubmit(payload) {
    if (registeringLabor) {
      return
    }

    setRegisteringLabor(true)
    setRegisterLaborError('')
    try {
      await registerProductionLaborWork(productionOrder.productionOrderId, payload)
      await refreshProductionOrder()
      setRegisterLaborOpen(false)
      setSuccessMessage('Trabajo de producción registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setRegisterLaborError(
        resolveApiErrorMessage(
          error,
          'No fue posible registrar el trabajo de producción.'
        )
      )
    } finally {
      setRegisteringLabor(false)
    }
  }

  async function openConsumeDialog() {
    if (pageBusy || status !== 'IN_PROGRESS') {
      return
    }

    setConsumeError('')
    setConsumeDialogOpen(true)
    setInventoryItemsLoading(true)
    try {
      const data = await getInventoryItems()
      setInventoryItems(Array.isArray(data?.items) ? data.items : [])
    } catch {
      setInventoryItems([])
    } finally {
      setInventoryItemsLoading(false)
    }
  }

  async function handleConsumeSubmit(payload) {
    if (consumingMaterial) {
      return
    }

    setConsumeError('')
    setConsumingMaterial(true)
    try {
      await registerProductionMaterialConsumption(
        productionOrder.productionOrderId,
        payload
      )
      setConsumeDialogOpen(false)
      await refreshProductionOrder()
      setSuccessMessage('Consumo de material registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setConsumeError(
        resolveApiErrorMessage(
          error,
          'No fue posible registrar el consumo de material.'
        )
      )
    } finally {
      setConsumingMaterial(false)
    }
  }

  async function handlePayLaborConfirm() {
    if (!payLaborTarget || payingLabor) {
      return
    }

    setPayingLabor(true)
    setPayLaborError('')
    try {
      await payProductionLaborWork(
        productionOrder.productionOrderId,
        payLaborTarget.laborWorkId,
        { paymentDate: payLaborDate }
      )
      setPayLaborTarget(null)
      await refreshProductionOrder()
      setSuccessMessage('Pago de mano de obra registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setPayLaborError(
        resolveApiErrorMessage(
          error,
          'No fue posible pagar el trabajo de producción.'
        )
      )
    } finally {
      setPayingLabor(false)
    }
  }

  async function handleCancelLaborConfirm() {
    if (!cancelLaborTarget || cancellingLabor) {
      return
    }

    setCancellingLabor(true)
    setCancelLaborError('')
    try {
      await cancelProductionLaborWork(
        productionOrder.productionOrderId,
        cancelLaborTarget.laborWorkId
      )
      setCancelLaborTarget(null)
      await refreshProductionOrder()
      setSuccessMessage('Trabajo de producción cancelado.')
      setSuccessOpen(true)
    } catch (error) {
      setCancelLaborError(
        resolveApiErrorMessage(
          error,
          'No fue posible cancelar el trabajo de producción.'
        )
      )
    } finally {
      setCancellingLabor(false)
    }
  }

  async function handleAddOperationSubmit(payload) {
    if (addingOperation) {
      return
    }

    setAddOperationError('')
    setAddingOperation(true)

    try {
      await addProductionOperation(productionOrder.productionOrderId, payload)
      await refreshProductionOrder()
      setAddOperationDialogOpen(false)
      setSuccessMessage('Operación creada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setAddOperationError(
        resolveApiErrorMessage(error, 'No fue posible crear la operación.')
      )
    } finally {
      setAddingOperation(false)
    }
  }

  async function handleAssignOperatorSubmit(payload) {
    if (assigningOperator || !selectedOperation) {
      return
    }

    setAssignOperatorError('')
    setAssigningOperator(true)

    try {
      await assignProductionOperationOperator(
        productionOrder.productionOrderId,
        selectedOperation.operationId,
        payload
      )
      await refreshProductionOrder()
      setAssignOperatorDialogOpen(false)
      setSelectedOperation(null)
      setSuccessMessage('Operario asignado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setAssignOperatorError(
        resolveApiErrorMessage(error, 'No fue posible asignar el operario.')
      )
    } finally {
      setAssigningOperator(false)
    }
  }

  async function handleStartOperationConfirm() {
    if (startingOperation || !selectedOperation) {
      return
    }

    setStartOperationError('')
    setStartingOperation(true)

    try {
      await startProductionOperation(
        productionOrder.productionOrderId,
        selectedOperation.operationId
      )
      await refreshProductionOrder()
      setStartOperationDialogOpen(false)
      setSelectedOperation(null)
      setSuccessMessage('Operación iniciada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setStartOperationError(
        resolveApiErrorMessage(error, 'No fue posible iniciar la operación.')
      )
    } finally {
      setStartingOperation(false)
    }
  }

  async function handleCompleteOperationConfirm() {
    if (completingOperation || !selectedOperation) {
      return
    }

    setCompleteOperationError('')
    setCompletingOperation(true)

    try {
      await completeProductionOperation(
        productionOrder.productionOrderId,
        selectedOperation.operationId
      )
      await refreshProductionOrder()
      setCompleteOperationDialogOpen(false)
      setSelectedOperation(null)
      setSuccessMessage('Operación completada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setCompleteOperationError(
        resolveApiErrorMessage(error, 'No fue posible completar la operación.')
      )
    } finally {
      setCompletingOperation(false)
    }
  }

  return (
    <>
      <Stack spacing={3}>
        <Button
          variant="outlined"
          onClick={() => navigate('/production')}
          sx={{ alignSelf: 'flex-start' }}
        >
          Volver
        </Button>

        {loading && <ProductionOrderDetailLoadingSkeleton />}

        {!loading && failed && (
          <>
            <Typography variant="h4">Detalle de Orden de Producción</Typography>
            <Alert severity="error">
              No fue posible obtener la orden de producción.
            </Alert>
          </>
        )}

        {!loading && !failed && notFound && (
          <>
            <Typography variant="h4">Detalle de Orden de Producción</Typography>
            <Alert severity="warning">Orden de producción no encontrada.</Alert>
          </>
        )}

        {!loading && !failed && productionOrder && (
          <>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
              justifyContent="space-between"
              alignItems={{ xs: 'stretch', sm: 'flex-start' }}
            >
              <Stack spacing={1}>
                <Typography variant="body2" color="text.secondary">
                  Detalle de Orden de Producción
                </Typography>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1.5}
                  alignItems={{ xs: 'flex-start', sm: 'center' }}
                >
                  <Typography variant="h4">
                    {productionOrder.orderNumber
                      ? resolveProductionBusinessLabel(productionOrder.orderNumber)
                      : 'Orden de producción'}
                  </Typography>
                  <Chip
                    label={
                      getProductionOrderStatusChipProps(productionOrder.status)
                        .label
                    }
                    color={
                      getProductionOrderStatusChipProps(productionOrder.status)
                        .color
                    }
                    size="small"
                  />
                  <Chip
                    label={
                      getProductionPriorityChipProps(productionOrder.priority)
                        .label
                    }
                    color={
                      getProductionPriorityChipProps(productionOrder.priority)
                        .color
                    }
                    size="small"
                    variant="outlined"
                  />
                </Stack>
                <Typography variant="body2" color="text.secondary">
                  {productionOrder.orderDescription || 'Sin descripción del pedido'}
                </Typography>
              </Stack>

              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1.5}
                sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
              >
                {status === 'CREATED' && (
                  <Button
                    variant="contained"
                    onClick={openPlanDialog}
                    disabled={pageBusy}
                  >
                    Planificar
                  </Button>
                )}

                {status === 'PLANNED' && (
                  <Button
                    variant="contained"
                    onClick={openStartDialog}
                    disabled={pageBusy}
                  >
                    Iniciar
                  </Button>
                )}

                {status === 'IN_PROGRESS' && (
                  <Button
                    variant="contained"
                    onClick={openCompleteDialog}
                    disabled={pageBusy}
                  >
                    Completar
                  </Button>
                )}
              </Stack>
            </Stack>

            <Paper sx={{ p: 3 }}>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Cliente">
                    <Typography>
                      {resolveProductionBusinessLabel(productionOrder.customerName)}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Orden comercial">
                    {productionOrder.orderId ? (
                      <Typography>
                        <RouterLink to={`/commercial/orders/${productionOrder.orderId}`}>
                          {resolveProductionBusinessLabel(productionOrder.orderNumber)}
                        </RouterLink>
                      </Typography>
                    ) : (
                      <Typography>—</Typography>
                    )}
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Descripción del pedido">
                    <Typography>
                      {productionOrder.orderDescription || '—'}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Fecha creación">
                    <Typography>
                      {formatDisplayDate(productionOrder.creationDate)}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Estado">
                    <Chip
                      label={
                        getProductionOrderStatusChipProps(productionOrder.status)
                          .label
                      }
                      color={
                        getProductionOrderStatusChipProps(productionOrder.status)
                          .color
                      }
                      size="small"
                    />
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Prioridad">
                    <Chip
                      label={
                        getProductionPriorityChipProps(productionOrder.priority)
                          .label
                      }
                      color={
                        getProductionPriorityChipProps(productionOrder.priority)
                          .color
                      }
                      size="small"
                      variant="outlined"
                    />
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Inicio planificado">
                    <Typography>
                      {formatDisplayDate(productionOrder.plannedStartDate) || '—'}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Fin planificado">
                    <Typography>
                      {formatDisplayDate(productionOrder.plannedEndDate) || '—'}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Inicio real">
                    <Typography>
                      {formatDisplayDate(productionOrder.actualStartDate) || '—'}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Cierre real">
                    <Typography>
                      {formatDisplayDate(productionOrder.actualCompletionDate) ||
                        '—'}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12 }}>
                  <DetailField label="Observaciones">
                    <Typography>
                      {productionOrder.observations || '—'}
                    </Typography>
                  </DetailField>
                </Grid>
              </Grid>
            </Paper>

            <Paper sx={{ p: 3 }}>
              <Stack spacing={3}>
                <Stack spacing={1}>
                  <Typography variant="h5">Productos a fabricar</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Estos datos corresponden a la información registrada al crear
                    la orden de producción.
                  </Typography>
                </Stack>

                {items.length === 0 ? (
                  <Stack spacing={1.5} alignItems="center" sx={{ py: 3 }}>
                    <Inventory2OutlinedIcon
                      color="action"
                      sx={{ fontSize: 48 }}
                    />
                    <Typography>
                      No hay productos registrados en el snapshot de producción.
                    </Typography>
                  </Stack>
                ) : (
                  <Stack spacing={2.5}>
                    {items.map((item) => (
                      <Paper
                        key={item.productionItemId}
                        variant="outlined"
                        sx={{ p: 2.5 }}
                      >
                        <Stack spacing={2.5}>
                          <Stack spacing={0.5}>
                            <Typography variant="body2" color="text.secondary">
                              Producto
                            </Typography>
                            <Typography variant="h6">
                              {item.productName}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              Cantidad: {item.quantity}
                            </Typography>
                          </Stack>

                          <Divider />

                          <ProductionItemSpecificationSection
                            specification={item.productSpecification}
                          />

                          <Divider />

                          <ProductionItemSizesSection item={item} />
                        </Stack>
                      </Paper>
                    ))}
                  </Stack>
                )}
              </Stack>
            </Paper>

            <Paper sx={{ p: 3 }}>
              <Stack spacing={3}>
                <Stack spacing={1}>
                  <Typography variant="h5">Costos de materiales</Typography>
                  <Typography variant="body2" color="text.secondary">
                    El costo se calcula con el costo histórico registrado al
                    momento del consumo de inventario.
                  </Typography>
                </Stack>

                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <DetailField label="Costo de materiales">
                      <Typography variant="h5">
                        {formatProductionMaterialCost(
                          materialCostSummary?.totalMaterialCost
                        ) ?? 'Sin costo configurado'}
                      </Typography>
                    </DetailField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <DetailField label="Consumos registrados">
                      <Typography>
                        {materialCostSummary?.consumptionCount ??
                          materialConsumptions.length}{' '}
                        consumos
                      </Typography>
                    </DetailField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <DetailField label="Valorización">
                      <Typography>
                        {materialCostSummary?.valuedConsumptionCount ?? 0}{' '}
                        valorizados
                      </Typography>
                      <Typography color="text.secondary">
                        {materialCostSummary?.unvaluedConsumptionCount ?? 0}{' '}
                        sin costo
                      </Typography>
                    </DetailField>
                  </Grid>
                </Grid>

                {status === 'IN_PROGRESS' ? (
                  <Button
                    type="button"
                    variant="outlined"
                    onClick={openConsumeDialog}
                    sx={{ alignSelf: 'flex-start' }}
                  >
                    Registrar consumo
                  </Button>
                ) : null}

                {materialConsumptions.length === 0 ? (
                  <Typography color="text.secondary">
                    No hay consumos de material registrados.
                  </Typography>
                ) : (
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Cantidad</TableCell>
                          <TableCell sx={headerCellSx}>Unidad</TableCell>
                          <TableCell sx={headerCellSx}>Costo unitario</TableCell>
                          <TableCell sx={headerCellSx}>Costo total</TableCell>
                          <TableCell sx={headerCellSx}>Observación</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {materialConsumptions.map((consumption) => (
                          <TableRow key={consumption.consumptionId}>
                            <TableCell>{consumption.quantity}</TableCell>
                            <TableCell>{consumption.unitOfMeasure}</TableCell>
                            <TableCell>
                              {formatProductionMaterialCostOrUnvalued(
                                consumption.unitCost
                              )}
                            </TableCell>
                            <TableCell>
                              {formatProductionMaterialCostOrUnvalued(
                                consumption.totalCost
                              )}
                            </TableCell>
                            <TableCell>
                              {consumption.observation || '—'}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}

                <Divider />

                <Stack spacing={1}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                    Costos
                  </Typography>
                  <Typography>
                    Materiales:{' '}
                    {formatProductionMaterialCost(
                      materialCostSummary?.totalMaterialCost ??
                        productionOrder?.materialCostSummary?.totalMaterialCost
                    ) ?? 'Sin costo configurado'}
                  </Typography>
                  <Typography>
                    Mano de obra:{' '}
                    {formatProductionMaterialCost(
                      laborCostSummary?.totalLaborCost ??
                        productionOrder?.laborCostSummary?.totalLaborCost
                    ) ?? 'No hay mano de obra registrada'}
                  </Typography>
                  <Typography variant="h6">
                    Total costo productivo:{' '}
                    {formatProductionMaterialCost(
                      productionOrder?.totalProductionCost
                    ) ?? '—'}
                  </Typography>
                </Stack>
              </Stack>
            </Paper>

            <Paper sx={{ p: 3 }}>
              <Stack spacing={3}>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1.5}
                  justifyContent="space-between"
                  alignItems={{ xs: 'stretch', sm: 'center' }}
                >
                  <Stack spacing={1}>
                    <Typography variant="h5">Mano de obra</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Costo de mano de obra:{' '}
                      {formatProductionMaterialCost(
                        laborCostSummary?.totalLaborCost
                      ) ?? 'No hay mano de obra registrada'}
                    </Typography>
                  </Stack>
                  {canRegisterLabor ? (
                    <Button
                      variant="outlined"
                      startIcon={<AddIcon />}
                      onClick={openRegisterLaborDialog}
                      disabled={pageBusy}
                    >
                      Registrar trabajo
                    </Button>
                  ) : null}
                </Stack>

                {laborWorks.length === 0 ? (
                  <Typography color="text.secondary">
                    No hay mano de obra registrada.
                  </Typography>
                ) : (
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Operario</TableCell>
                          <TableCell sx={headerCellSx}>Fecha</TableCell>
                          <TableCell sx={headerCellSx}>Operación</TableCell>
                          <TableCell sx={headerCellSx}>Cantidad</TableCell>
                          <TableCell sx={headerCellSx}>Tarifa</TableCell>
                          <TableCell sx={headerCellSx}>Total</TableCell>
                          <TableCell sx={headerCellSx}>Estado</TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Acciones
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {laborWorks.map((labor) => (
                          <TableRow key={labor.laborWorkId}>
                            <TableCell>
                              {labor.operatorDisplayName || '—'}
                            </TableCell>
                            <TableCell>
                              {formatDisplayDate(labor.workDate)}
                            </TableCell>
                            <TableCell>{labor.operation}</TableCell>
                            <TableCell>
                              {labor.quantity} {labor.unitOfMeasure}
                            </TableCell>
                            <TableCell>
                              {formatProductionMaterialCost(labor.unitRate) ??
                                '—'}
                            </TableCell>
                            <TableCell>
                              {formatProductionMaterialCost(
                                labor.calculatedAmount
                              ) ?? '—'}
                            </TableCell>
                            <TableCell>
                              <Chip
                                size="small"
                                color={getLaborStatusColor(labor.status)}
                                label={getLaborStatusLabel(labor.status)}
                              />
                            </TableCell>
                            <TableCell align="right">
                              {labor.status === 'PENDING' ? (
                                <Stack
                                  direction="row"
                                  spacing={1}
                                  justifyContent="flex-end"
                                >
                                  <Button
                                    size="small"
                                    variant="contained"
                                    disabled={pageBusy}
                                    onClick={() => {
                                      setPayLaborError('')
                                      setPayLaborDate(toIsoDate())
                                      setPayLaborTarget(labor)
                                    }}
                                  >
                                    Pagar
                                  </Button>
                                  <Button
                                    size="small"
                                    color="warning"
                                    disabled={pageBusy}
                                    onClick={() => {
                                      setCancelLaborError('')
                                      setCancelLaborTarget(labor)
                                    }}
                                  >
                                    Cancelar
                                  </Button>
                                </Stack>
                              ) : (
                                '—'
                              )}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Stack>
            </Paper>

            <Paper sx={{ p: 3 }}>
              <Stack spacing={3}>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1.5}
                  justifyContent="space-between"
                  alignItems={{ xs: 'stretch', sm: 'center' }}
                >
                  <Typography variant="h5">Operaciones</Typography>
                  {canAddOperation && operations.length > 0 && (
                    <Button
                      variant="outlined"
                      startIcon={<AddIcon />}
                      onClick={openAddOperationDialog}
                      disabled={pageBusy}
                    >
                      Agregar operación
                    </Button>
                  )}
                </Stack>

                {operations.length === 0 ? (
                  <Stack spacing={1.5} alignItems="center" sx={{ py: 3 }}>
                    <PrecisionManufacturingOutlinedIcon
                      color="action"
                      sx={{ fontSize: 48 }}
                    />
                    <Typography>No hay operaciones registradas.</Typography>
                    <Typography color="text.secondary" textAlign="center">
                      {canAddOperation
                        ? 'Agrega operaciones de fabricación mientras la orden esté en estado Creada.'
                        : 'Las operaciones de fabricación aparecerán aquí cuando se agreguen a esta orden.'}
                    </Typography>
                    {canAddOperation && (
                      <Button
                        variant="outlined"
                        startIcon={<AddIcon />}
                        onClick={openAddOperationDialog}
                        disabled={pageBusy}
                      >
                        Agregar operación
                      </Button>
                    )}
                  </Stack>
                ) : (
                  <TableContainer sx={{ overflowX: 'auto' }}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Tipo</TableCell>
                          <TableCell align="center" sx={headerCellSx}>
                            Estado
                          </TableCell>
                          <TableCell sx={headerCellSx}>Operario</TableCell>
                          <TableCell sx={headerCellSx}>
                            Inicio planificado
                          </TableCell>
                          <TableCell sx={headerCellSx}>Fin planificado</TableCell>
                          <TableCell sx={headerCellSx}>Inicio real</TableCell>
                          <TableCell sx={headerCellSx}>Fin real</TableCell>
                          <TableCell sx={headerCellSx}>Observaciones</TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Acciones
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {operations.map((operation) => {
                          const operationStatusChip =
                            getProductionOperationStatusChipProps(
                              operation.status
                            )
                          const canAssign =
                            status !== 'COMPLETED' &&
                            operation.status !== 'COMPLETED'
                          const canStartOperation =
                            orderAllowsOperationExecution &&
                            operation.status === 'PENDING'
                          const canCompleteOperation =
                            orderAllowsOperationExecution &&
                            operation.status === 'IN_PROGRESS'

                          return (
                            <TableRow key={operation.operationId} hover>
                              <TableCell>
                                {formatProductionOperationType(operation.type)}
                              </TableCell>
                              <TableCell align="center">
                                <Chip
                                  label={operationStatusChip.label}
                                  color={operationStatusChip.color}
                                  size="small"
                                />
                              </TableCell>
                              <TableCell>
                                {operation.assignedOperator || '—'}
                              </TableCell>
                              <TableCell>
                                {formatDisplayDate(operation.plannedStartDate) ||
                                  '—'}
                              </TableCell>
                              <TableCell>
                                {formatDisplayDate(operation.plannedEndDate) ||
                                  '—'}
                              </TableCell>
                              <TableCell>
                                {formatDisplayDate(operation.actualStartDate) ||
                                  '—'}
                              </TableCell>
                              <TableCell>
                                {formatDisplayDate(operation.actualEndDate) ||
                                  '—'}
                              </TableCell>
                              <TableCell>
                                {operation.observations || '—'}
                              </TableCell>
                              <TableCell align="right">
                                <Stack
                                  direction="row"
                                  spacing={1}
                                  justifyContent="flex-end"
                                  flexWrap="wrap"
                                  useFlexGap
                                >
                                  {canAssign && (
                                    <Button
                                      size="small"
                                      variant="outlined"
                                      onClick={() =>
                                        openAssignOperatorDialog(operation)
                                      }
                                      disabled={pageBusy}
                                    >
                                      Asignar
                                    </Button>
                                  )}
                                  {canStartOperation && (
                                    <Button
                                      size="small"
                                      variant="contained"
                                      onClick={() =>
                                        openStartOperationDialog(operation)
                                      }
                                      disabled={pageBusy}
                                    >
                                      Iniciar
                                    </Button>
                                  )}
                                  {canCompleteOperation && (
                                    <Button
                                      size="small"
                                      variant="contained"
                                      onClick={() =>
                                        openCompleteOperationDialog(operation)
                                      }
                                      disabled={pageBusy}
                                    >
                                      Completar
                                    </Button>
                                  )}
                                </Stack>
                              </TableCell>
                            </TableRow>
                          )
                        })}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Stack>
            </Paper>

            <RegisterProductionMaterialConsumptionDialog
              open={consumeDialogOpen}
              inventoryItems={inventoryItems}
              itemsLoading={inventoryItemsLoading}
              onClose={() => {
                if (!consumingMaterial) {
                  setConsumeDialogOpen(false)
                  setConsumeError('')
                }
              }}
              onSubmit={handleConsumeSubmit}
              submitting={consumingMaterial}
              errorMessage={consumeError}
            />

            <RegisterProductionLaborDialog
              open={registerLaborOpen}
              operators={laborOperators}
              operatorsLoading={laborOperatorsLoading}
              onClose={() => {
                if (!registeringLabor) {
                  setRegisterLaborOpen(false)
                }
              }}
              onSubmit={handleRegisterLaborSubmit}
              submitting={registeringLabor}
              errorMessage={registerLaborError}
            />

            <ConfirmProductionLifecycleDialog
              open={Boolean(payLaborTarget)}
              title="Pagar mano de obra"
              description={
                payLaborTarget
                  ? `¿Deseas pagar este trabajo de producción? Operario: ${payLaborTarget.operatorDisplayName}. Operación: ${payLaborTarget.operation}. Cantidad: ${payLaborTarget.quantity}. Tarifa: ${formatProductionMaterialCost(payLaborTarget.unitRate) ?? '—'}. Total: ${formatProductionMaterialCost(payLaborTarget.calculatedAmount) ?? '—'}.`
                  : ''
              }
              confirmLabel="Pagar"
              submittingLabel="Pagando..."
              onClose={() => {
                if (!payingLabor) {
                  setPayLaborTarget(null)
                  setPayLaborError('')
                }
              }}
              onConfirm={handlePayLaborConfirm}
              submitting={payingLabor}
              errorMessage={payLaborError}
              dateLabel="Fecha de pago"
              dateValue={payLaborDate}
              onDateChange={setPayLaborDate}
            />

            <ConfirmProductionLifecycleDialog
              open={Boolean(cancelLaborTarget)}
              title="Cancelar trabajo de producción"
              description={
                cancelLaborTarget
                  ? `¿Cancelar el trabajo pendiente de ${cancelLaborTarget.operatorDisplayName} (${cancelLaborTarget.operation})?`
                  : ''
              }
              confirmLabel="Cancelar trabajo"
              submittingLabel="Cancelando..."
              onClose={() => {
                if (!cancellingLabor) {
                  setCancelLaborTarget(null)
                  setCancelLaborError('')
                }
              }}
              onConfirm={handleCancelLaborConfirm}
              submitting={cancellingLabor}
              errorMessage={cancelLaborError}
            />

            <PlanProductionOrderDialog
              open={planDialogOpen}
              onClose={closePlanDialog}
              onSubmit={handlePlanSubmit}
              submitting={planning}
              errorMessage={planError}
              initialValues={{
                plannedStartDate: productionOrder.plannedStartDate || '',
                plannedEndDate: productionOrder.plannedEndDate || '',
                priority: productionOrder.priority || 'NORMAL',
              }}
            />

            <ConfirmProductionLifecycleDialog
              open={startDialogOpen}
              title="¿Iniciar orden de producción?"
              description="Esta acción cambiará el estado de la orden a EN PROCESO."
              confirmLabel="Iniciar"
              submittingLabel="Iniciando..."
              onClose={closeStartDialog}
              onConfirm={handleStartConfirm}
              submitting={starting}
              errorMessage={startError}
              dateLabel="Fecha de inicio"
              dateValue={startDate}
              onDateChange={setStartDate}
            />

            <ConfirmProductionLifecycleDialog
              open={completeDialogOpen}
              title="¿Completar orden de producción?"
              description="La orden solo podrá completarse cuando todas sus operaciones estén completadas."
              confirmLabel="Completar"
              submittingLabel="Completando..."
              onClose={closeCompleteDialog}
              onConfirm={handleCompleteConfirm}
              submitting={completing}
              errorMessage={completeError}
              dateLabel="Fecha de cierre"
              dateValue={completeDate}
              onDateChange={setCompleteDate}
            />

            <AddProductionOperationDialog
              open={addOperationDialogOpen}
              onClose={closeAddOperationDialog}
              onSubmit={handleAddOperationSubmit}
              submitting={addingOperation}
              errorMessage={addOperationError}
              usedTypes={usedOperationTypes}
            />

            <AssignProductionOperatorDialog
              open={assignOperatorDialogOpen}
              onClose={closeAssignOperatorDialog}
              onSubmit={handleAssignOperatorSubmit}
              submitting={assigningOperator}
              errorMessage={assignOperatorError}
              initialOperator={selectedOperation?.assignedOperator || ''}
            />

            <ConfirmProductionLifecycleDialog
              open={startOperationDialogOpen}
              title="¿Iniciar operación?"
              description="La operación pasará a estado EN PROCESO."
              confirmLabel="Iniciar"
              submittingLabel="Iniciando..."
              onClose={closeStartOperationDialog}
              onConfirm={handleStartOperationConfirm}
              submitting={startingOperation}
              errorMessage={startOperationError}
            />

            <ConfirmProductionLifecycleDialog
              open={completeOperationDialogOpen}
              title="¿Completar operación?"
              description="Esta acción marcará la operación como completada."
              confirmLabel="Completar"
              submittingLabel="Completando..."
              onClose={closeCompleteOperationDialog}
              onConfirm={handleCompleteOperationConfirm}
              submitting={completingOperation}
              errorMessage={completeOperationError}
            />
          </>
        )}
      </Stack>

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity="success"
          variant="filled"
          onClose={() => setSuccessOpen(false)}
        >
          {successMessage}
        </Alert>
      </Snackbar>
    </>
  )
}

export default ProductionOrderDetailPage
