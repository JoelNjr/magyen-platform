import { useEffect, useState } from 'react'
import { Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { getSalesReport } from '../services/intelligenceService'
import PageHeader from '../../../layout/PageHeader'

function IntelligencePage() {
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    getSalesReport()
      .then((data) => {
        setReport(data)
        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })
  }, [])

  if (loading) {
    return <Typography>Cargando reporte de ventas...</Typography>
  }

  if (failed) {
    return <Typography>No fue posible obtener el reporte.</Typography>
  }

  return (
    <Stack spacing={4}>
      <PageHeader title="Inteligencia Operacional" />

      <Grid container spacing={4}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography>Total vendido</Typography>
              <Typography variant="h4">{report.totalSold}</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography>Cantidad de órdenes</Typography>
              <Typography variant="h4">{report.orderCount}</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography>Promedio por venta</Typography>
              <Typography variant="h4">{report.averagePerSale}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Stack>
  )
}

export default IntelligencePage
