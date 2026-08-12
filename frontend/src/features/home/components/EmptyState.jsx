import { Paper, Stack, Typography } from '@mui/material'

export default function EmptyState({ icon, message }) {
  return (
    <Paper variant="outlined" sx={{ p: 3, textAlign: 'center' }}>
      <Stack spacing={1} alignItems="center">
        {icon}
        <Typography color="text.secondary">{message}</Typography>
      </Stack>
    </Paper>
  )
}
