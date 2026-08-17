import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import EngineeringOutlinedIcon from '@mui/icons-material/EngineeringOutlined'
import {
  Alert,
  Button,
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
import CreateOperatorDialog from '../components/CreateOperatorDialog'
import { createProductionOperator, getProductionOperators } from '../services/productionService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function OperatorsTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Operario</TableCell>
        <TableCell sx={headerCellSx}>Estado</TableCell>
      </TableRow>
    </TableHead>
  )
}

function OperatorsPage() {
  const navigate = useNavigate()
  const [operators, setOperators] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creatingOperator, setCreatingOperator] = useState(false)
  const [createOperatorFailed, setCreateOperatorFailed] = useState(false)
  const [successOpen, setSuccessOpen] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)

    getProductionOperators()
      .then((data) => {
        const nextOperators = Array.isArray(data?.operators) ? data.operators : []
        setOperators(nextOperators)
        setLoading(false)
      })
      .catch(() => {
        setOperators([])
        setFailed(true)
        setLoading(false)
      })
  }, [])

  function openCreateDialog() {
    if (creatingOperator) {
      return
    }

    setCreateOperatorFailed(false)
    setCreateDialogOpen(true)
  }

  function handleCreateDialogClose() {
    if (creatingOperator) {
      return
    }

    setCreateDialogOpen(false)
    setCreateOperatorFailed(false)
  }

  async function handleCreateOperator(name) {
    setCreateOperatorFailed(false)
    setCreatingOperator(true)

    try {
      const createdOperator = await createProductionOperator({ name })
      setOperators((current) => [...current, createdOperator])
      setCreateDialogOpen(false)
      setSuccessOpen(true)
    } catch {
      setCreateOperatorFailed(true)
    } finally {
      setCreatingOperator(false)
    }
  }

  return (
    <Stack spacing={3}>
      <Button
        type="button"
        variant="text"
        onClick={() => navigate('/production')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver a producción
      </Button>

      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Typography variant="h3">Operarios</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={openCreateDialog}
          disabled={loading || creatingOperator}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          Nuevo operario
        </Button>
      </Stack>

      {loading && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <OperatorsTableHead />
            <TableBody>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <TableRow key={`operator-skeleton-${index}`}>
                  <TableCell>
                    <Skeleton width="50%" />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="20%" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {!loading && failed && (
        <Alert severity="error">No fue posible obtener los operarios.</Alert>
      )}

      {!loading && !failed && operators.length === 0 && (
        <Paper sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
            <EngineeringOutlinedIcon color="action" sx={{ fontSize: 48 }} />
            <Typography variant="h6">No hay operarios registrados</Typography>
            <Typography color="text.secondary" textAlign="center">
              Crea el operario de producción que ejecutó cada trabajo. No requiere
              cuenta de acceso.
            </Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={openCreateDialog}
            >
              Nuevo operario
            </Button>
          </Stack>
        </Paper>
      )}

      {!loading && !failed && operators.length > 0 && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <OperatorsTableHead />
            <TableBody>
              {operators.map((operator) => (
                <TableRow key={operator.operatorId} hover>
                  <TableCell>
                    <Typography variant="body1">{operator.name}</Typography>
                  </TableCell>
                  <TableCell>{operator.active ? 'Activo' : 'Inactivo'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <CreateOperatorDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onCreated={handleCreateOperator}
        submitting={creatingOperator}
        error={createOperatorFailed}
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
          Operario creado correctamente.
        </Alert>
      </Snackbar>
    </Stack>
  )
}

export default OperatorsPage
