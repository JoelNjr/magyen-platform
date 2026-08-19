import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import { Button, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material'
import {
  formatMonthPeriodLabel,
  getCalendarMonthRange,
  getMonthOptions,
  getSelectableYears,
  monthPeriodFromYearMonth,
  shiftCalendarMonth,
} from './monthPeriod'

function MonthPeriodNavigator({ fromDate, onPeriodChange, disabled = false }) {
  const current = getCalendarMonthRange(fromDate ? new Date(`${fromDate}T00:00:00`) : new Date())
  const years = getSelectableYears()
  const months = getMonthOptions()

  function applyPeriod(period) {
    onPeriodChange(period)
  }

  return (
    <Paper variant="outlined" sx={{ p: 2 }}>
      <Stack
        direction={{ xs: 'column', md: 'row' }}
        spacing={2}
        alignItems={{ xs: 'stretch', md: 'center' }}
        justifyContent="space-between"
      >
        <Stack spacing={0.5}>
          <Typography variant="subtitle2" color="text.secondary">
            Período activo
          </Typography>
          <Typography variant="h6">{formatMonthPeriodLabel(current.fromDate)}</Typography>
        </Stack>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1.5}
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Button
            variant="outlined"
            startIcon={<ChevronLeftIcon />}
            disabled={disabled}
            onClick={() => applyPeriod(shiftCalendarMonth(current.fromDate, -1))}
          >
            Mes anterior
          </Button>
          <TextField
            select
            label="Mes"
            size="small"
            value={current.month}
            disabled={disabled}
            sx={{ minWidth: { xs: 0, sm: 160 }, width: { xs: '100%', sm: 'auto' } }}
            onChange={(event) =>
              applyPeriod(monthPeriodFromYearMonth(current.year, Number(event.target.value)))
            }
          >
            {months.map((month) => (
              <MenuItem key={month.value} value={month.value}>
                {month.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Año"
            size="small"
            value={current.year}
            disabled={disabled}
            sx={{ minWidth: { xs: 0, sm: 110 }, width: { xs: '100%', sm: 'auto' } }}
            onChange={(event) =>
              applyPeriod(monthPeriodFromYearMonth(Number(event.target.value), current.month))
            }
          >
            {years.map((year) => (
              <MenuItem key={year} value={year}>
                {year}
              </MenuItem>
            ))}
          </TextField>
          <Button
            variant="outlined"
            endIcon={<ChevronRightIcon />}
            disabled={disabled}
            onClick={() => applyPeriod(shiftCalendarMonth(current.fromDate, 1))}
          >
            Mes siguiente
          </Button>
          <Button
            disabled={disabled}
            onClick={() => applyPeriod(getCalendarMonthRange())}
          >
            Mes actual
          </Button>
        </Stack>
      </Stack>
    </Paper>
  )
}

export default MonthPeriodNavigator
