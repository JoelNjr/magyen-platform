import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Alert,
  Button,
  Chip,
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
import { useNavigate } from 'react-router-dom'
import CreateInventoryItemDialog from '../components/CreateInventoryItemDialog'
import RegisterInventoryPurchaseDialog from '../components/RegisterInventoryPurchaseDialog'
import {
  formatStockWithUnit,
  formatUnitCostLabel,
  getInventoryMaterialTitle,
  getInventoryStockStatusChipProps,
} from '../presentation/inventoryStatusPresentation'
import {
  createInventoryItem,
  getInventoryItems,
  registerInventoryPurchase,
} from '../services/inventoryService'
import PageHeader from '../../../layout/PageHeader'
import EmptyState from '../../home/components/EmptyState'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function InventoryTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Material</TableCell>
        <TableCell sx={headerCellSx}>Código</TableCell>
        <TableCell sx={headerCellSx}>Stock</TableCell>
        <TableCell sx={headerCellSx}>Unidad</TableCell>
        <TableCell sx={headerCellSx}>Stock mínimo</TableCell>
        <TableCell sx={headerCellSx}>Costo unitario</TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acción
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function InventoryPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [purchaseDialogOpen, setPurchaseDialogOpen] = useState(false)
  const [purchasing, setPurchasing] = useState(false)
  const [purchaseError, setPurchaseError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  async function loadInventoryItems() {
    setLoading(true)
    setFailed(false)

    try {
      const data = await getInventoryItems()
      setItems(Array.isArray(data?.items) ? data.items : [])
      setLoading(false)
    } catch {
      setItems([])
      setFailed(true)
      setLoading(false)
    }
  }

  useEffect(() => {
    loadInventoryItems()
  }, [])

  function openPurchaseDialog() {
    if (creating || purchasing) {
      return
    }
    setPurchaseError('')
    setPurchaseDialogOpen(true)
  }

  function handlePurchaseDialogClose() {
    if (purchasing) {
      return
    }
    setPurchaseDialogOpen(false)
    setPurchaseError('')
  }

  async function handleRegisterPurchase(payload) {
    if (purchasing) {
      return
    }

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
      await loadInventoryItems()
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

  function openCreateDialog() {
    if (creating || purchasing) {
      return
    }

    setCreateError('')
    setCreateDialogOpen(true)
  }

  function handleCreateDialogClose() {
    if (creating) {
      return
    }

    setCreateDialogOpen(false)
    setCreateError('')
  }

  async function handleCreateInventoryItem(payload) {
    if (creating) {
      return
    }

    setCreateError('')
    setCreating(true)

    try {
      const created = await createInventoryItem(payload)
      await loadInventoryItems()
      setCreateDialogOpen(false)
      if (created?.plotterPaperRoll && created?.paperRollNumber) {
        setSuccessMessage(`Rollo ${created.paperRollNumber} creado correctamente.`)
      } else {
        setSuccessMessage('Material creado correctamente.')
      }
      setSuccessOpen(true)
    } catch (error) {
      setCreateError(
        resolveApiErrorMessage(error, 'No fue posible crear el material.')
      )
    } finally {
      setCreating(false)
    }
  }

  return (
    <>
      <Stack spacing={3}>
        <PageHeader
          title="Inventario"
          actions={
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={1}
            sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
          >
            <Button
              variant="outlined"
              onClick={openPurchaseDialog}
              disabled={loading || creating || purchasing || items.length === 0}
            >
              Registrar entrada de material
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={openCreateDialog}
              disabled={loading || creating || purchasing}
            >
              Nuevo material
            </Button>
          </Stack>
          }
        />

        {loading && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <InventoryTableHead />
              <TableBody>
                {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                  <TableRow key={`inventory-skeleton-${index}`}>
                    <TableCell>
                      <Skeleton width="70%" />
                      <Skeleton width="40%" sx={{ mt: 0.5 }} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={90} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={70} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={50} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={70} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={90} />
                    </TableCell>
                    <TableCell align="center">
                      <Skeleton
                        width={90}
                        height={28}
                        sx={{ mx: 'auto', borderRadius: 4 }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Skeleton width={90} sx={{ ml: 'auto' }} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {!loading && failed && (
          <Alert severity="error">
            No fue posible obtener los materiales de inventario.
          </Alert>
        )}

        {!loading && !failed && items.length === 0 && (
          <EmptyState
            icon={<Inventory2OutlinedIcon color="action" sx={{ fontSize: 48 }} />}
            title="No hay materiales registrados en el inventario."
            action={
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openCreateDialog}
              >
                Nuevo material
              </Button>
            }
          />
        )}

        {!loading && !failed && items.length > 0 && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <InventoryTableHead />
              <TableBody>
                {items.map((item) => {
                  const statusChip = getInventoryStockStatusChipProps(item.lowStock)
                  const detailPath = `/inventory/${item.inventoryItemId}`
                  const minimumStockLabel =
                    item.minimumStock === null || item.minimumStock === undefined
                      ? '—'
                      : formatStockWithUnit(item.minimumStock, item.unitOfMeasure)

                  return (
                    <TableRow key={item.inventoryItemId} hover>
                      <TableCell>
                        <Typography variant="body1">
                          {getInventoryMaterialTitle(item)}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {item.materialCode}
                        </Typography>
                      </TableCell>
                      <TableCell>{item.materialCode}</TableCell>
                      <TableCell>
                        {formatStockWithUnit(item.stock, item.unitOfMeasure)}
                      </TableCell>
                      <TableCell>{item.unitOfMeasure}</TableCell>
                      <TableCell>{minimumStockLabel}</TableCell>
                      <TableCell>
                        {formatUnitCostLabel(item.unitCost, item.unitOfMeasure)}
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={statusChip.label}
                          color={statusChip.color}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => navigate(detailPath)}
                        >
                          Ver detalle
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

      <CreateInventoryItemDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onSubmit={handleCreateInventoryItem}
        submitting={creating}
        errorMessage={createError}
      />

      <RegisterInventoryPurchaseDialog
        open={purchaseDialogOpen}
        items={items}
        onClose={handlePurchaseDialogClose}
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

export default InventoryPage
