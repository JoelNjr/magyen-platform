import { useEffect, useState } from 'react'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
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
import CreateInventoryItemDialog from '../components/CreateInventoryItemDialog'
import RegisterInventoryPurchaseDialog from '../components/RegisterInventoryPurchaseDialog'
import {
  formatCatalogMinimumStockLabel,
  formatCatalogUnitCostLabel,
  formatInventoryNumber,
  formatMaterialTypeLabel,
  formatPhysicalUnitCount,
  formatStockWithUnit,
  formatUnitCostLabel,
  getInventoryCatalogTitle,
  getInventoryMaterialTitle,
  getInventoryStockStatusChipProps,
} from '../presentation/inventoryStatusPresentation'
import {
  formatInventoryMovementDateTime,
  formatInventoryMovementQuantity,
  formatInventoryMovementType,
} from '../presentation/inventoryMovementPresentation'
import {
  createInventoryItem,
  getInventoryMaterial,
  getInventoryMovements,
  registerInventoryPurchase,
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

function InventoryMaterialDetailPage() {
  const { materialCode } = useParams()
  const navigate = useNavigate()

  const [material, setMaterial] = useState(null)
  const [units, setUnits] = useState([])
  const [movements, setMovements] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [movementsFailed, setMovementsFailed] = useState(false)

  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  const [purchaseDialogOpen, setPurchaseDialogOpen] = useState(false)
  const [purchasing, setPurchasing] = useState(false)
  const [purchaseError, setPurchaseError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const pageBusy = creating || purchasing
  const showUnits = Boolean(material?.paperMaterial) || units.length > 1
  const holdingUnit =
    units.find((unit) => unit.inventoryItemId === material?.stockHoldingItemId) ||
    units[0] ||
    null

  async function loadMaterial() {
    const data = await getInventoryMaterial(materialCode)
    const nextMaterial = data?.material ?? null
    const nextUnits = Array.isArray(data?.units) ? data.units : []
    setMaterial(nextMaterial)
    setUnits(nextUnits)

    const holdingItemId = nextMaterial?.stockHoldingItemId
    if (holdingItemId && !nextMaterial?.paperMaterial) {
      try {
        const movementsData = await getInventoryMovements(holdingItemId)
        setMovements(Array.isArray(movementsData?.movements) ? movementsData.movements : [])
        setMovementsFailed(false)
      } catch {
        setMovements([])
        setMovementsFailed(true)
      }
    } else {
      setMovements([])
      setMovementsFailed(false)
    }

    return data
  }

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setMovementsFailed(false)
    setMaterial(null)
    setUnits([])
    setMovements([])

    loadMaterial()
      .then(() => {
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
  }, [materialCode])

  function openCreateDialog() {
    if (pageBusy) {
      return
    }
    setCreateError('')
    setCreateDialogOpen(true)
  }

  function closeCreateDialog() {
    if (creating) {
      return
    }
    setCreateDialogOpen(false)
    setCreateError('')
  }

  async function handleCreateRoll(payload) {
    if (creating) {
      return
    }
    setCreateError('')
    setCreating(true)
    try {
      const created = await createInventoryItem(payload)
      await loadMaterial()
      setCreateDialogOpen(false)
      setSuccessMessage(
        created?.paperRollNumber
          ? `Rollo ${created.paperRollNumber} creado correctamente.`
          : 'Rollo creado correctamente.'
      )
      setSuccessOpen(true)
    } catch (error) {
      setCreateError(resolveApiErrorMessage(error, 'No fue posible crear el rollo.'))
    } finally {
      setCreating(false)
    }
  }

  function openPurchaseDialog() {
    if (pageBusy || !holdingUnit) {
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
      await registerInventoryPurchase(payload.inventoryItemId, {
        purchaseId: payload.purchaseId,
        quantity: payload.quantity,
        unitCost: payload.unitCost,
        purchaseDate: payload.purchaseDate,
        observation: payload.observation,
      })
      await loadMaterial()
      setPurchaseDialogOpen(false)
      setSuccessMessage('Entrada de material registrada. El gasto de la compra quedó en Finanzas.')
      setSuccessOpen(true)
    } catch (error) {
      setPurchaseError(
        resolveApiErrorMessage(error, 'No fue posible registrar la entrada de material.')
      )
    } finally {
      setPurchasing(false)
    }
  }

  const statusChip = getInventoryStockStatusChipProps(material?.lowStock)
  const physicalUnitLabel = formatPhysicalUnitCount(material)

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
            <Paper sx={{ p: 3 }}>
              <Grid container spacing={2}>
                {Array.from({ length: 4 }).map((_, index) => (
                  <Grid key={`inventory-material-skeleton-${index}`} size={{ xs: 12, md: 6 }}>
                    <Skeleton width="40%" />
                    <Skeleton width="60%" sx={{ mt: 1 }} />
                  </Grid>
                ))}
              </Grid>
            </Paper>
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

        {!loading && !failed && !notFound && material && (
          <>
            <Stack spacing={1}>
              <BrandAccentLine />
              <Typography variant="h3">Inventario</Typography>
              <Typography variant="h4">{getInventoryCatalogTitle(material)}</Typography>
              <Typography variant="subtitle1" color="text.secondary">
                {material.name || formatMaterialTypeLabel(material.materialType)}
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
                  {material.paperMaterial ? (
                    <Button
                      variant="contained"
                      onClick={openCreateDialog}
                      disabled={pageBusy}
                    >
                      Nuevo rollo
                    </Button>
                  ) : (
                    <>
                      <Button
                        variant="contained"
                        onClick={openPurchaseDialog}
                        disabled={pageBusy || !holdingUnit}
                      >
                        Registrar entrada de material
                      </Button>
                      {holdingUnit ? (
                        <Button
                          variant="outlined"
                          onClick={() =>
                            navigate(`/inventory/${holdingUnit.inventoryItemId}`)
                          }
                          disabled={pageBusy}
                        >
                          Ver unidad
                        </Button>
                      ) : null}
                    </>
                  )}
                </Stack>
              </Stack>

              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Stock agregado">
                    <Typography variant="h5">
                      {formatStockWithUnit(material.aggregatedStock, material.unitOfMeasure)}
                    </Typography>
                    {physicalUnitLabel ? (
                      <Typography variant="caption" color="text.secondary">
                        {physicalUnitLabel}
                      </Typography>
                    ) : null}
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Unidad">
                    <Typography>{material.unitOfMeasure}</Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Stock mínimo">
                    <Typography>{formatCatalogMinimumStockLabel(material)}</Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Costo unitario">
                    <Typography>{formatCatalogUnitCostLabel(material)}</Typography>
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

              {material.paperMaterial && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  Cada compra de papel crea un rollo físico nuevo. Plotter continúa
                  consumiendo el UUID del rollo, no el código de material.
                </Alert>
              )}

              {material.lowStock && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  Este material está en stock bajo.
                </Alert>
              )}
            </Paper>

            {showUnits && (
              <Stack spacing={2}>
                <Typography variant="h6">Unidades físicas</Typography>
                {units.length === 0 ? (
                  <Paper sx={{ p: { xs: 3, sm: 4 } }}>
                    <Typography color="text.secondary">
                      No hay unidades registradas para este material.
                    </Typography>
                  </Paper>
                ) : (
                  <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Unidad</TableCell>
                          <TableCell sx={headerCellSx}>Stock</TableCell>
                          <TableCell sx={headerCellSx}>Costo unitario</TableCell>
                          <TableCell sx={headerCellSx}>Estado</TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Acción
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {units.map((unit) => {
                          const unitStatus = getInventoryStockStatusChipProps(unit.lowStock)
                          return (
                            <TableRow key={unit.inventoryItemId} hover>
                              <TableCell>
                                <Typography variant="body2">
                                  {unit.paperRollNumber
                                    ? `Rollo ${unit.paperRollNumber}`
                                    : getInventoryMaterialTitle(unit)}
                                </Typography>
                              </TableCell>
                              <TableCell>
                                {formatStockWithUnit(unit.stock, unit.unitOfMeasure)}
                              </TableCell>
                              <TableCell>
                                {formatUnitCostLabel(unit.unitCost, unit.unitOfMeasure)}
                              </TableCell>
                              <TableCell>
                                <Chip
                                  label={unitStatus.label}
                                  color={unitStatus.color}
                                  size="small"
                                />
                              </TableCell>
                              <TableCell align="right">
                                <Button
                                  size="small"
                                  variant="outlined"
                                  onClick={() =>
                                    navigate(`/inventory/${unit.inventoryItemId}`)
                                  }
                                >
                                  Ver unidad
                                </Button>
                              </TableCell>
                            </TableRow>
                          )
                        })}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Stack>
            )}

            {!material.paperMaterial && (
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
                      <Inventory2OutlinedIcon color="action" sx={{ fontSize: 40 }} />
                      <Typography color="text.secondary">
                        No hay movimientos registrados.
                      </Typography>
                    </Stack>
                  </Paper>
                )}
                {!movementsFailed && movements.length > 0 && (
                  <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Fecha</TableCell>
                          <TableCell sx={headerCellSx}>Tipo</TableCell>
                          <TableCell sx={headerCellSx}>Cantidad</TableCell>
                          <TableCell sx={headerCellSx}>Costo unitario</TableCell>
                          <TableCell sx={headerCellSx}>Stock resultante</TableCell>
                          <TableCell sx={headerCellSx}>Observación</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {movements.map((movement) => (
                          <TableRow key={movement.movementId} hover>
                            <TableCell>
                              {formatInventoryMovementDateTime(movement.movementDate)}
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
                            <TableCell>
                              {formatUnitCostLabel(
                                movement.unitCost,
                                movement.unitOfMeasure
                              )}
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
            )}
          </>
        )}
      </Stack>

      <CreateInventoryItemDialog
        open={createDialogOpen}
        onClose={closeCreateDialog}
        onSubmit={handleCreateRoll}
        submitting={creating}
        errorMessage={createError}
        initialMaterialType="PAPER"
        lockMaterialType
      />

      <RegisterInventoryPurchaseDialog
        open={purchaseDialogOpen}
        lockedItem={holdingUnit}
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

export default InventoryMaterialDetailPage
