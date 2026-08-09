import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined'
import {
  Alert,
  Button,
  Chip,
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
import { useNavigate, useParams } from 'react-router-dom'
import AddProductionOperationDialog from '../components/AddProductionOperationDialog'
import AssignProductionOperatorDialog from '../components/AssignProductionOperatorDialog'
import ConfirmProductionLifecycleDialog from '../components/ConfirmProductionLifecycleDialog'
import PlanProductionOrderDialog from '../components/PlanProductionOrderDialog'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import {
  formatProductionOperationType,
  getProductionOperationStatusChipProps,
  getProductionOrderStatusChipProps,
  getProductionPriorityChipProps,
} from '../presentation/productionStatusPresentation'
import {
  addProductionOperation,
  assignProductionOperationOperator,
  completeProductionOperation,
  completeProductionOrder,
  getProductionOrder,
  planProductionOrder,
  startProductionOperation,
  startProductionOrder,
} from '../services/productionService'

const headerCellSx = { fontWeight: 'bold' }

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

  async function refreshProductionOrder() {
    const data = await getProductionOrder(productionOrderId)
    setProductionOrder(data)
    return data
  }

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setProductionOrder(null)

    getProductionOrder(productionOrderId)
      .then((data) => {
        setProductionOrder(data)
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

  const operations = productionOrder?.operations ?? []
  const status = productionOrder?.status
  const lifecycleBusy = planning || starting || completing
  const operationBusy =
    addingOperation ||
    assigningOperator ||
    startingOperation ||
    completingOperation
  const pageBusy = lifecycleBusy || operationBusy
  const canAddOperation = status === 'CREATED'
  const orderAllowsOperationExecution = status === 'IN_PROGRESS'
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
      await startProductionOrder(productionOrder.productionOrderId)
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
      await completeProductionOrder(productionOrder.productionOrderId)
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
                  <Typography variant="h4">Orden de producción</Typography>
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
                  <DetailField label="ID de producción">
                    <Typography sx={{ wordBreak: 'break-all' }}>
                      {productionOrder.productionOrderId}
                    </Typography>
                  </DetailField>
                </Grid>

                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Orden comercial">
                    <Typography sx={{ wordBreak: 'break-all' }}>
                      {productionOrder.orderId}
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
