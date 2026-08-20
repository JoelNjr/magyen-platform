import { useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import CategoryOutlinedIcon from '@mui/icons-material/CategoryOutlined'
import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Paper,
  Skeleton,
  Snackbar,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material'
import {
  activateAdminCatalogEntry,
  createAdminCatalogEntry,
  deactivateAdminCatalogEntry,
  getAdminCatalogs,
} from '../services/adminCatalogsService'
import PageHeader from '../../../layout/PageHeader'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

const CATALOG_TABS = [
  { id: 'garments', path: 'garments', label: 'Prendas' },
  { id: 'fabrics', path: 'fabrics', label: 'Telas' },
  { id: 'collars', path: 'collars', label: 'Cuellos' },
  { id: 'sleeves', path: 'sleeves', label: 'Mangas' },
]

function resolveApiErrorMessage(error, fallbackMessage) {
  const status = error?.response?.status
  if (status === 409) {
    return 'Ya existe un valor con ese nombre en este catálogo.'
  }
  return error?.response?.data?.message || fallbackMessage
}

function CatalogTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Nombre</TableCell>
        <TableCell sx={headerCellSx}>Estado</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acciones
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function AdminCatalogsPage() {
  const [catalogs, setCatalogs] = useState({
    garments: [],
    fabrics: [],
    collars: [],
    sleeves: [],
  })
  const [tab, setTab] = useState('garments')
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [newName, setNewName] = useState('')
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [entryPendingDeactivation, setEntryPendingDeactivation] = useState(null)
  const [busyEntryId, setBusyEntryId] = useState(null)
  const [actionError, setActionError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const currentTab = CATALOG_TABS.find((item) => item.id === tab) ?? CATALOG_TABS[0]
  const entries = useMemo(
    () => (Array.isArray(catalogs[currentTab.id]) ? catalogs[currentTab.id] : []),
    [catalogs, currentTab.id]
  )

  async function loadCatalogs() {
    setLoading(true)
    setFailed(false)
    try {
      const data = await getAdminCatalogs()
      setCatalogs({
        garments: Array.isArray(data?.garments) ? data.garments : [],
        fabrics: Array.isArray(data?.fabrics) ? data.fabrics : [],
        collars: Array.isArray(data?.collars) ? data.collars : [],
        sleeves: Array.isArray(data?.sleeves) ? data.sleeves : [],
      })
      setLoading(false)
    } catch {
      setCatalogs({ garments: [], fabrics: [], collars: [], sleeves: [] })
      setFailed(true)
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCatalogs()
  }, [])

  function openCreateDialog() {
    if (creating || busyEntryId) {
      return
    }
    setNewName('')
    setCreateError('')
    setCreateOpen(true)
  }

  function closeCreateDialog() {
    if (creating) {
      return
    }
    setCreateOpen(false)
    setCreateError('')
    setNewName('')
  }

  async function handleCreate() {
    const name = newName.trim()
    if (!name) {
      setCreateError('El nombre es obligatorio.')
      return
    }

    setCreating(true)
    setCreateError('')
    try {
      await createAdminCatalogEntry(currentTab.path, name)
      await loadCatalogs()
      setCreateOpen(false)
      setNewName('')
      setSuccessMessage(`${currentTab.label}: valor creado.`)
      setSuccessOpen(true)
    } catch (error) {
      setCreateError(resolveApiErrorMessage(error, 'No fue posible crear el valor del catálogo.'))
    } finally {
      setCreating(false)
    }
  }

  async function handleActivate(entry) {
    if (busyEntryId) {
      return
    }
    setBusyEntryId(entry.catalogEntryId)
    setActionError('')
    try {
      await activateAdminCatalogEntry(currentTab.path, entry.catalogEntryId)
      await loadCatalogs()
      setSuccessMessage(`${entry.name} quedó activo.`)
      setSuccessOpen(true)
    } catch (error) {
      setActionError(resolveApiErrorMessage(error, 'No fue posible activar el valor.'))
    } finally {
      setBusyEntryId(null)
    }
  }

  async function handleDeactivate() {
    if (!entryPendingDeactivation || busyEntryId) {
      return
    }
    const entry = entryPendingDeactivation
    setBusyEntryId(entry.catalogEntryId)
    setActionError('')
    try {
      await deactivateAdminCatalogEntry(currentTab.path, entry.catalogEntryId)
      await loadCatalogs()
      setEntryPendingDeactivation(null)
      setSuccessMessage(`${entry.name} quedó inactivo. Los registros históricos siguen visibles.`)
      setSuccessOpen(true)
    } catch (error) {
      setActionError(resolveApiErrorMessage(error, 'No fue posible desactivar el valor.'))
    } finally {
      setBusyEntryId(null)
    }
  }

  return (
    <>
      <Stack spacing={3}>
        <PageHeader
          title="Catálogos"
          icon={<CategoryOutlinedIcon color="action" />}
          subtitle="Estos catálogos alimentan cotizaciones y pedidos. Una tela de catálogo no crea inventario ni gastos en Finanzas."
          actions={
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDialog}
            disabled={loading || creating}
          >
            Nuevo valor
          </Button>
          }
        />

        {failed && (
          <Alert severity="error">
            No fue posible cargar los catálogos. Intente de nuevo.
          </Alert>
        )}
        {actionError && <Alert severity="error">{actionError}</Alert>}

        <Paper>
          <Tabs
            value={tab}
            onChange={(_event, value) => setTab(value)}
            variant="scrollable"
            scrollButtons="auto"
          >
            {CATALOG_TABS.map((item) => (
              <Tab key={item.id} value={item.id} label={item.label} />
            ))}
          </Tabs>

          {loading ? (
            <Stack spacing={1} sx={{ p: 2 }}>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <Skeleton key={index} variant="rectangular" height={36} />
              ))}
            </Stack>
          ) : entries.length === 0 ? (
            <Stack sx={{ p: 3 }}>
              <Typography color="text.secondary">
                No hay valores en {currentTab.label.toLowerCase()}. Cree el primero para que
                Magyen pueda usarlo en cotizaciones.
              </Typography>
            </Stack>
          ) : (
            <TableContainer>
              <Table>
                <CatalogTableHead />
                <TableBody>
                  {entries.map((entry) => (
                    <TableRow key={entry.catalogEntryId} hover>
                      <TableCell>{entry.name}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={entry.active ? 'Activo' : 'Inactivo'}
                          color={entry.active ? 'success' : 'default'}
                          variant={entry.active ? 'filled' : 'outlined'}
                        />
                      </TableCell>
                      <TableCell align="right">
                        {entry.active ? (
                          <Button
                            type="button"
                            onClick={() => setEntryPendingDeactivation(entry)}
                            disabled={Boolean(busyEntryId)}
                          >
                            Desactivar
                          </Button>
                        ) : (
                          <Button
                            type="button"
                            onClick={() => handleActivate(entry)}
                            disabled={Boolean(busyEntryId)}
                          >
                            Activar
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      </Stack>

      <Dialog open={createOpen} onClose={closeCreateDialog} fullWidth maxWidth="sm">
        <DialogTitle>Nuevo valor — {currentTab.label}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {createError && <Alert severity="error">{createError}</Alert>}
            <TextField
              label="Nombre"
              value={newName}
              onChange={(event) => {
                setNewName(event.target.value)
                setCreateError('')
              }}
              fullWidth
              disabled={creating}
              autoFocus
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button type="button" onClick={closeCreateDialog} disabled={creating}>
            Cancelar
          </Button>
          <Button type="button" variant="contained" onClick={handleCreate} disabled={creating}>
            {creating ? 'Creando...' : 'Crear'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={Boolean(entryPendingDeactivation)}
        onClose={() => {
          if (!busyEntryId) {
            setEntryPendingDeactivation(null)
          }
        }}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>Desactivar valor</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Desactivar «{entryPendingDeactivation?.name}»? No se elimina. Los pedidos y
            cotizaciones históricos seguirán mostrando este valor, pero no podrá
            seleccionarse en registros nuevos.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            type="button"
            onClick={() => setEntryPendingDeactivation(null)}
            disabled={Boolean(busyEntryId)}
          >
            Cancelar
          </Button>
          <Button
            type="button"
            color="warning"
            variant="contained"
            onClick={handleDeactivate}
            disabled={Boolean(busyEntryId)}
          >
            Desactivar
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        message={successMessage}
      />
    </>
  )
}

export default AdminCatalogsPage
