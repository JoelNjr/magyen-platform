import { useEffect, useState } from 'react'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Alert,
  Button,
  Chip,
  Grid,
  Link,
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
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { isAdmin } from '../../auth/presentation/authPresentation'
import RegisterInventoryMovementDialog from '../components/RegisterInventoryMovementDialog'
import RegisterInventoryPurchaseDialog from '../components/RegisterInventoryPurchaseDialog'
import UpdateInventoryMinimumStockDialog from '../components/UpdateInventoryMinimumStockDialog'
import UpdateInventoryUnitCostDialog from '../components/UpdateInventoryUnitCostDialog'
import {
  formatInventoryMovementDateTime,
  formatInventoryMovementQuantity,
  formatInventoryMovementSourceId,
  formatInventoryMovementSourceLabel,
  formatInventoryMovementType,
} from '../presentation/inventoryMovementPresentation'
import {
  formatInventoryMoney,
  formatInventoryNumber,
  formatMaterialTypeLabel,
  formatStockWithUnit,
  formatUnitCostLabel,
  formatUnitOfMeasureLabel,
  getInventoryMaterialTitle,
  getInventoryStockStatusChipProps,
} from '../presentation/inventoryStatusPresentation'
import {
  getInventoryItem,
  getInventoryMovements,
  registerInventoryMovement,
  registerInventoryPurchase,
  updateInventoryMinimumStock,
  updateInventoryUnitCost,
} from '../services/inventoryService'
import { BrandAccentLine } from '../../../layout/PageHeader'

const headerCellSx = { fontWeight: 'bold' }

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
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

function MovementsTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Fecha</TableCell>
        <TableCell sx={headerCellSx}>Tipo</TableCell>
        <TableCell sx={headerCellSx}>Cantidad</TableCell>
        <TableCell sx={headerCellSx}>Unidad</TableCell>
        <TableCell sx={headerCellSx}>Origen</TableCell>
        <TableCell sx={headerCellSx}>Costo unitario</TableCell>
        <TableCell sx={headerCellSx}>Costo total</TableCell>
        <TableCell sx={headerCellSx}>Stock resultante</TableCell>
        <TableCell sx={headerCellSx}>Observación</TableCell>
      </TableRow>
    </TableHead>
  )
}

function formatMovementMoney(value) {
  const formatted = formatInventoryMoney(value)
  return formatted === null ? '—' : `$${formatted}`
}

function InventoryDetailPage() {
  const { inventoryItemId } = useParams()
  const navigate = useNavigate()
  const { identity } = useAuth()
  const canConfigureUnitCost = isAdmin(identity)

  const [item, setItem] = useState(null)
  const [movements, setMovements] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [movementsFailed, setMovementsFailed] = useState(false)

  const [minimumStockDialogOpen, setMinimumStockDialogOpen] = useState(false)
  const [updatingMinimumStock, setUpdatingMinimumStock] = useState(false)
  const [minimumStockError, setMinimumStockError] = useState('')

  const [unitCostDialogOpen, setUnitCostDialogOpen] = useState(false)
  const [updatingUnitCost, setUpdatingUnitCost] = useState(false)
  const [unitCostError, setUnitCostError] = useState('')

  const [movementDialogOpen, setMovementDialogOpen] = useState(false)
  const [registeringMovement, setRegisteringMovement] = useState(false)
  const [movementError, setMovementError] = useState('')

  const [purchaseDialogOpen, setPurchaseDialogOpen] = useState(false)
  const [purchasing, setPurchasing] = useState(false)
  const [purchaseError, setPurchaseError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const pageBusy =
    updatingMinimumStock || updatingUnitCost || registeringMovement || purchasing

  async function refreshDetail() {
    const inventoryItem = await getInventoryItem(inventoryItemId)
    setItem(inventoryItem)

    try {
      const movementsData = await getInventoryMovements(inventoryItemId)
      setMovements(
        Array.isArray(movementsData?.movements) ? movementsData.movements : []
      )
      setMovementsFailed(false)
    } catch {
      setMovements([])
      setMovementsFailed(true)
    }

    return inventoryItem
  }

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setMovementsFailed(false)
    setItem(null)
    setMovements([])

    getInventoryItem(inventoryItemId)
      .then(async (inventoryItem) => {
        setItem(inventoryItem)

        try {
          const movementsData = await getInventoryMovements(inventoryItemId)
          setMovements(
            Array.isArray(movementsData?.movements) ? movementsData.movements : []
          )
        } catch {
          setMovements([])
          setMovementsFailed(true)
        }

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
  }, [inventoryItemId])

  function openMinimumStockDialog() {
    if (pageBusy || !item) {
      return
    }

    setMinimumStockError('')
    setMinimumStockDialogOpen(true)
  }

  function closeMinimumStockDialog() {
    if (updatingMinimumStock) {
      return
    }

    setMinimumStockDialogOpen(false)
    setMinimumStockError('')
  }

  async function handleUpdateMinimumStock(minimumStock) {
    setMinimumStockError('')
    setUpdatingMinimumStock(true)

    try {
      await updateInventoryMinimumStock(inventoryItemId, minimumStock)
      await refreshDetail()
      setMinimumStockDialogOpen(false)
      setSuccessMessage('Stock mínimo actualizado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setMinimumStockError(
        resolveApiErrorMessage(
          error,
          'No fue posible actualizar el stock mínimo.'
        )
      )
    } finally {
      setUpdatingMinimumStock(false)
    }
  }

  function openUnitCostDialog() {
    if (pageBusy || !item) {
      return
    }

    setUnitCostError('')
    setUnitCostDialogOpen(true)
  }

  function closeUnitCostDialog() {
    if (updatingUnitCost) {
      return
    }

    setUnitCostDialogOpen(false)
    setUnitCostError('')
  }

  async function handleUpdateUnitCost(unitCost) {
    setUnitCostError('')
    setUpdatingUnitCost(true)

    try {
      await updateInventoryUnitCost(inventoryItemId, unitCost)
      await refreshDetail()
      setUnitCostDialogOpen(false)
      setSuccessMessage('Costo unitario actualizado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setUnitCostError(
        resolveApiErrorMessage(
          error,
          'No fue posible actualizar el costo unitario.'
        )
      )
    } finally {
      setUpdatingUnitCost(false)
    }
  }

  function openMovementDialog() {
    if (pageBusy || !item) {
      return
    }

    setMovementError('')
    setMovementDialogOpen(true)
  }

  function closeMovementDialog() {
    if (registeringMovement) {
      return
    }

    setMovementDialogOpen(false)
    setMovementError('')
  }

  async function handleRegisterMovement(payload) {
    setMovementError('')
    setRegisteringMovement(true)

    try {
      await registerInventoryMovement(inventoryItemId, payload)
      await refreshDetail()
      setMovementDialogOpen(false)
      setSuccessMessage('Movimiento registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setMovementError(
        resolveApiErrorMessage(
          error,
          'No fue posible registrar el movimiento.'
        )
      )
    } finally {
      setRegisteringMovement(false)
    }
  }

  function openPurchaseDialog() {
    if (pageBusy || !item) {
      return
    }
    setPurchaseError('')
    setPurchaseDialogOpen(true)
  }

  function closePurchaseDialog() {
    if (purchasing) {
      return
    }
    setPurchaseDialogOpen(false)
    setPurchaseError('')
  }

  async function handleRegisterPurchase(payload) {
    setPurchaseError('')
    setPurchasing(true)

    try {
      await registerInventoryPurchase(inventoryItemId, {
        purchaseId: payload.purchaseId,
        quantity: payload.quantity,
        unitCost: payload.unitCost,
        purchaseDate: payload.purchaseDate,
        observation: payload.observation,
      })
      await refreshDetail()
      setPurchaseDialogOpen(false)
      setSuccessMessage('Entrada de material registrada. El gasto de la compra quedó en Finanzas.')
      setSuccessOpen(true)
    } catch (error) {
      setPurchaseError(
        resolveApiErrorMessage(
          error,
          'No fue posible registrar la entrada de material.'
        )
      )
    } finally {
      setPurchasing(false)
    }
  }

  const statusChip = getInventoryStockStatusChipProps(item?.lowStock)
  const monitoringDisabled =
    item &&
    (item.minimumStock === null || item.minimumStock === undefined)

  return (
    <>
      <Stack spacing={3}>
        <Button
          variant="outlined"
          onClick={() => navigate('/inventory')}
          sx={{ alignSelf: 'flex-start' }}
        >
          Volver
        </Button>

        {loading && (
          <Stack spacing={2}>
            <Skeleton width={180} height={40} />
            <Skeleton width={280} height={32} />
            <Skeleton width={160} height={24} />
            <Paper sx={{ p: 3 }}>
              <Grid container spacing={2}>
                {Array.from({ length: 4 }).map((_, index) => (
                  <Grid key={`inventory-detail-skeleton-${index}`} size={{ xs: 12, md: 6 }}>
                    <Skeleton width="40%" />
                    <Skeleton width="60%" sx={{ mt: 1 }} />
                  </Grid>
                ))}
              </Grid>
            </Paper>
            <Skeleton width={220} height={32} />
            <Skeleton variant="rounded" height={180} />
          </Stack>
        )}

        {!loading && notFound && (
          <Alert severity="warning">
            No se encontró el material de inventario solicitado.
          </Alert>
        )}

        {!loading && failed && (
          <Alert severity="error">
            No fue posible obtener el detalle del material de inventario.
          </Alert>
        )}

        {!loading && !failed && !notFound && item && (
          <>
            <Stack spacing={1}>
              <BrandAccentLine />
              <Typography variant="h3">Inventario</Typography>
              {item.plotterPaperRoll ? (
                <>
                  <Typography variant="overline" color="text.secondary">
                    Rollo de papel
                  </Typography>
                  <Typography variant="h4">
                    {item.paperRollNumber || getInventoryMaterialTitle(item)}
                  </Typography>
                </>
              ) : (
                <Typography variant="h4">{getInventoryMaterialTitle(item)}</Typography>
              )}
              <Typography variant="subtitle1" color="text.secondary">
                {item.materialCode}
                {item.materialType
                  ? ` · ${formatMaterialTypeLabel(item.materialType)}`
                  : ''}
              </Typography>
            </Stack>

            <Paper sx={{ p: { xs: 2.5, sm: 3 } }}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={2}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'flex-start' }}
                sx={{ mb: 2 }}
              >
                <Typography variant="h6">Resumen de stock</Typography>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1}
                  sx={{ width: { xs: '100%', sm: 'auto' } }}
                >
                  <Button
                    variant="contained"
                    onClick={openPurchaseDialog}
                    disabled={pageBusy}
                  >
                    Registrar entrada de material
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={openMovementDialog}
                    disabled={pageBusy}
                  >
                    Registrar movimiento
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={openMinimumStockDialog}
                    disabled={pageBusy}
                  >
                    Configurar stock mínimo
                  </Button>
                  {canConfigureUnitCost && (
                    <Button
                      variant="outlined"
                      onClick={openUnitCostDialog}
                      disabled={pageBusy}
                    >
                      Configurar costo unitario
                    </Button>
                  )}
                </Stack>
              </Stack>

              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField
                    label={
                      item.plotterPaperRoll
                        ? 'Stock disponible'
                        : 'Stock actual'
                    }
                  >
                    <Typography variant="h5">
                      {formatStockWithUnit(item.stock, item.unitOfMeasure)}
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Unidad">
                    <Typography>
                      {formatUnitOfMeasureLabel(item.unitOfMeasure)} (
                      {item.unitOfMeasure})
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Stock mínimo">
                    {monitoringDisabled ? (
                      <Typography color="text.secondary">
                        Monitoreo de stock mínimo no configurado
                      </Typography>
                    ) : (
                      <Typography>
                        {formatStockWithUnit(
                          item.minimumStock,
                          item.unitOfMeasure
                        )}
                      </Typography>
                    )}
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField
                    label={
                      item.plotterPaperRoll
                        ? 'Costo por metro'
                        : 'Costo unitario'
                    }
                  >
                    <Typography>
                      {formatUnitCostLabel(item.unitCost, item.unitOfMeasure)}
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Estado">
                    <Chip
                      label={statusChip.label}
                      color={statusChip.color}
                      size="small"
                    />
                  </DetailField>
                </Grid>
              </Grid>

              {item.lowStock && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  Este material está en stock bajo.
                </Alert>
              )}
            </Paper>

            <Stack spacing={2}>
              <Typography variant="h6">Historial de movimientos</Typography>

              {movementsFailed && (
                <Alert severity="error">
                  No fue posible obtener el historial de movimientos.
                </Alert>
              )}

              {!movementsFailed && movements.length === 0 && (
                <Paper sx={{ p: { xs: 3, sm: 4 } }}>
                  <Stack spacing={1.5} alignItems="center" sx={{ py: 1 }}>
                    <Inventory2OutlinedIcon
                      color="action"
                      sx={{ fontSize: 40 }}
                    />
                    <Typography color="text.secondary">
                      No hay movimientos registrados.
                    </Typography>
                  </Stack>
                </Paper>
              )}

              {!movementsFailed && movements.length > 0 && (
                <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
                  <Table>
                    <MovementsTableHead />
                    <TableBody>
                      {movements.map((movement) => (
                        <TableRow key={movement.movementId} hover>
                          <TableCell>
                            {formatInventoryMovementDateTime(
                              movement.movementDate
                            )}
                          </TableCell>
                          <TableCell>
                            {formatInventoryMovementType(movement.movementType)}
                          </TableCell>
                          <TableCell>
                            {formatInventoryMovementQuantity(
                              movement.quantity,
                              movement.movementType
                            )}
                          </TableCell>
                          <TableCell>{movement.unitOfMeasure}</TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatInventoryMovementSourceLabel(
                                movement.sourceType,
                                movement.sourceId
                              )}
                            </Typography>
                            {formatInventoryMovementSourceId(
                              movement.sourceType,
                              movement.sourceId
                            ) &&
                              (movement.sourceType === 'PLOTTER' ? (
                                <Link
                                  component={RouterLink}
                                  to={`/plotter/jobs/${movement.sourceId}`}
                                  variant="caption"
                                  underline="hover"
                                >
                                  Ver trabajo
                                </Link>
                              ) : (
                                <Typography
                                  variant="caption"
                                  color="text.secondary"
                                  display="block"
                                >
                                  ID:{' '}
                                  {formatInventoryMovementSourceId(
                                    movement.sourceType,
                                    movement.sourceId
                                  )}
                                </Typography>
                              ))}
                          </TableCell>
                          <TableCell>
                            {formatMovementMoney(movement.unitCost)}
                          </TableCell>
                          <TableCell>
                            {formatMovementMoney(movement.totalCost)}
                          </TableCell>
                          <TableCell>
                            {formatInventoryNumber(movement.resultingStock)}
                          </TableCell>
                          <TableCell>{movement.observation || '—'}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Stack>
          </>
        )}
      </Stack>

      <UpdateInventoryMinimumStockDialog
        open={minimumStockDialogOpen}
        onClose={closeMinimumStockDialog}
        onSubmit={handleUpdateMinimumStock}
        submitting={updatingMinimumStock}
        errorMessage={minimumStockError}
        currentMinimumStock={item?.minimumStock}
      />

      {canConfigureUnitCost && (
        <UpdateInventoryUnitCostDialog
          open={unitCostDialogOpen}
          onClose={closeUnitCostDialog}
          onSubmit={handleUpdateUnitCost}
          submitting={updatingUnitCost}
          errorMessage={unitCostError}
          currentUnitCost={item?.unitCost}
          unitOfMeasure={item?.unitOfMeasure}
        />
      )}

      <RegisterInventoryMovementDialog
        open={movementDialogOpen}
        onClose={closeMovementDialog}
        onSubmit={handleRegisterMovement}
        submitting={registeringMovement}
        errorMessage={movementError}
        currentStock={item?.stock}
        unitOfMeasure={item?.unitOfMeasure}
      />

      <RegisterInventoryPurchaseDialog
        open={purchaseDialogOpen}
        lockedItem={item}
        onClose={closePurchaseDialog}
        onSubmit={handleRegisterPurchase}
        submitting={purchasing}
        errorMessage={purchaseError}
      />

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

export default InventoryDetailPage
