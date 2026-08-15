import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ManageAccountsOutlinedIcon from '@mui/icons-material/ManageAccountsOutlined'
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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import { useAuth } from '../AuthContext'
import CreateAuthenticationUserDialog from '../components/CreateAuthenticationUserDialog'
import {
  formatAuthenticationRoleLabel,
  resolveAdminUsersErrorMessage,
} from '../presentation/authPresentation'
import {
  activateAdminUser,
  changeAdminUserRole,
  createAdminUser,
  deactivateAdminUser,
  getAdminUsers,
} from '../services/adminUsersService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function UsersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Usuario</TableCell>
        <TableCell sx={headerCellSx}>Rol</TableCell>
        <TableCell sx={headerCellSx}>Estado</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acciones
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function AdminUsersPage() {
  const { identity, refreshIdentity } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [userPendingDeactivation, setUserPendingDeactivation] = useState(null)
  const [busyUserId, setBusyUserId] = useState(null)
  const [actionError, setActionError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  async function loadUsers() {
    setLoading(true)
    setFailed(false)
    try {
      const data = await getAdminUsers()
      setUsers(Array.isArray(data?.users) ? data.users : [])
      setLoading(false)
    } catch {
      setUsers([])
      setFailed(true)
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  async function refreshCurrentIdentityIfNeeded(userId) {
    if (identity?.userId === userId) {
      await refreshIdentity()
    }
  }

  function openCreateDialog() {
    if (creating || busyUserId) {
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

  async function handleCreateUser(payload) {
    if (creating) {
      return
    }

    setCreateError('')
    setCreating(true)
    try {
      await createAdminUser(payload)
      await loadUsers()
      setCreateDialogOpen(false)
      setSuccessMessage('Usuario creado.')
      setSuccessOpen(true)
    } catch (error) {
      setCreateError(
        resolveAdminUsersErrorMessage(error, 'No fue posible crear el usuario.')
      )
    } finally {
      setCreating(false)
    }
  }

  async function handleChangeRole(user) {
    if (busyUserId) {
      return
    }

    const nextRole = user.role === 'ADMIN' ? 'OPERATOR' : 'ADMIN'
    setActionError('')
    setBusyUserId(user.id)
    try {
      await changeAdminUserRole(user.id, nextRole)
      await loadUsers()
      await refreshCurrentIdentityIfNeeded(user.id)
      setSuccessMessage('Rol actualizado.')
      setSuccessOpen(true)
    } catch (error) {
      setActionError(
        resolveAdminUsersErrorMessage(error, 'No fue posible cambiar el rol.')
      )
    } finally {
      setBusyUserId(null)
    }
  }

  async function handleActivate(user) {
    if (busyUserId) {
      return
    }

    setActionError('')
    setBusyUserId(user.id)
    try {
      await activateAdminUser(user.id)
      await loadUsers()
      setSuccessMessage('Usuario activado.')
      setSuccessOpen(true)
    } catch (error) {
      setActionError(
        resolveAdminUsersErrorMessage(error, 'No fue posible activar el usuario.')
      )
    } finally {
      setBusyUserId(null)
    }
  }

  async function handleConfirmDeactivate() {
    if (!userPendingDeactivation || busyUserId) {
      return
    }

    const user = userPendingDeactivation
    setActionError('')
    setBusyUserId(user.id)
    try {
      await deactivateAdminUser(user.id)
      setUserPendingDeactivation(null)
      await loadUsers()
      await refreshCurrentIdentityIfNeeded(user.id)
      setSuccessMessage('Usuario desactivado.')
      setSuccessOpen(true)
    } catch (error) {
      setActionError(
        resolveAdminUsersErrorMessage(
          error,
          'No fue posible desactivar el usuario.'
        )
      )
    } finally {
      setBusyUserId(null)
    }
  }

  const pageBusy = creating || Boolean(busyUserId)

  return (
    <>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Typography variant="h4">Usuarios internos</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDialog}
            disabled={pageBusy}
          >
            Nuevo usuario
          </Button>
        </Stack>

        {actionError && <Alert severity="error">{actionError}</Alert>}

        {failed && (
          <Alert severity="error">No fue posible cargar los usuarios.</Alert>
        )}

        {!failed && (
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <UsersTableHead />
              <TableBody>
                {loading &&
                  Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                    <TableRow key={`skeleton-${index}`}>
                      <TableCell>
                        <Skeleton />
                      </TableCell>
                      <TableCell>
                        <Skeleton />
                      </TableCell>
                      <TableCell>
                        <Skeleton />
                      </TableCell>
                      <TableCell>
                        <Skeleton />
                      </TableCell>
                    </TableRow>
                  ))}

                {!loading && users.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4}>
                      <Stack
                        spacing={1}
                        alignItems="center"
                        sx={{ py: 4, color: 'text.secondary' }}
                      >
                        <ManageAccountsOutlinedIcon />
                        <Typography>No hay usuarios internos.</Typography>
                      </Stack>
                    </TableCell>
                  </TableRow>
                )}

                {!loading &&
                  users.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>{user.username}</TableCell>
                      <TableCell>
                        {formatAuthenticationRoleLabel(user.role)}
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={user.enabled ? 'Activo' : 'Inactivo'}
                          color={user.enabled ? 'success' : 'default'}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Stack
                          direction="row"
                          spacing={1}
                          justifyContent="flex-end"
                        >
                          <Button
                            size="small"
                            onClick={() => handleChangeRole(user)}
                            disabled={pageBusy}
                          >
                            {user.role === 'ADMIN'
                              ? 'Cambiar a operador'
                              : 'Cambiar a administrador'}
                          </Button>
                          {user.enabled ? (
                            <Button
                              size="small"
                              color="warning"
                              onClick={() => setUserPendingDeactivation(user)}
                              disabled={pageBusy}
                            >
                              Desactivar
                            </Button>
                          ) : (
                            <Button
                              size="small"
                              onClick={() => handleActivate(user)}
                              disabled={pageBusy}
                            >
                              Activar
                            </Button>
                          )}
                        </Stack>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      <CreateAuthenticationUserDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onSubmit={handleCreateUser}
        submitting={creating}
        errorMessage={createError}
      />

      <Dialog
        open={Boolean(userPendingDeactivation)}
        onClose={() => {
          if (!busyUserId) {
            setUserPendingDeactivation(null)
          }
        }}
      >
        <DialogTitle>Desactivar usuario</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Desactivar a {userPendingDeactivation?.username}? No podrá iniciar
            sesión mientras esté inactivo.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            type="button"
            onClick={() => setUserPendingDeactivation(null)}
            disabled={Boolean(busyUserId)}
          >
            Cancelar
          </Button>
          <Button
            type="button"
            color="warning"
            variant="contained"
            onClick={handleConfirmDeactivate}
            disabled={Boolean(busyUserId)}
          >
            Desactivar
          </Button>
        </DialogActions>
      </Dialog>

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

export default AdminUsersPage
