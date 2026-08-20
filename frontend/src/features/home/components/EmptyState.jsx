import { Box, Paper, Stack, Typography } from '@mui/material'

export default function EmptyState({ icon, title, message, action, plain = false }) {
  const content = (
    <Stack spacing={1.5} alignItems="center">
      {icon}
      {title ? (
        <Typography variant="h6" component="p" sx={{ fontWeight: 600 }}>
          {title}
        </Typography>
      ) : null}
      {message ? (
        <Typography color="text.secondary" textAlign="center">
          {message}
        </Typography>
      ) : null}
      {action}
    </Stack>
  )

  if (plain) {
    return (
      <Box sx={{ p: { xs: 2, sm: 3 }, textAlign: 'center' }}>{content}</Box>
    )
  }

  return (
    <Paper variant="outlined" sx={{ p: { xs: 3, sm: 4 }, textAlign: 'center' }}>
      {content}
    </Paper>
  )
}
