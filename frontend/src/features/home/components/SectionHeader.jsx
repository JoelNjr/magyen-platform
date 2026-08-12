import { Stack, Typography } from '@mui/material'

const PRIORITY_VARIANTS = {
  primary: 'h4',
  secondary: 'h5',
  standard: 'h5',
  context: 'h6',
}

/**
 * @param {{ title: string, actions?: import('react').ReactNode, priority?: 'primary' | 'secondary' | 'standard' | 'context', subtitle?: string }} props
 */
export default function SectionHeader({
  title,
  actions,
  priority = 'standard',
  subtitle,
}) {
  const variant = PRIORITY_VARIANTS[priority] || 'h5'

  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      justifyContent="space-between"
      alignItems={{ xs: 'stretch', sm: 'flex-start' }}
    >
      <Stack spacing={0.5} sx={{ minWidth: 0 }}>
        <Typography variant={variant} component="h2" fontWeight={priority === 'primary' ? 700 : 600}>
          {title}
        </Typography>
        {subtitle ? (
          <Typography variant="body2" color="text.secondary">
            {subtitle}
          </Typography>
        ) : null}
      </Stack>
      {actions}
    </Stack>
  )
}
