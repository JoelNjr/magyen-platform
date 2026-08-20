import { Box, Stack, Typography } from '@mui/material'

export function BrandAccentLine({ width = 36 }) {
  return (
    <Box
      sx={{
        width,
        height: 3,
        bgcolor: 'primary.main',
        borderRadius: 1,
      }}
    />
  )
}

export default function PageHeader({ title, subtitle, actions, icon }) {
  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      justifyContent="space-between"
      alignItems={{ xs: 'stretch', sm: 'center' }}
    >
      <Stack spacing={1} sx={{ minWidth: 0 }}>
        <BrandAccentLine />
        <Stack direction="row" spacing={1} alignItems="center" sx={{ minWidth: 0 }}>
          {icon}
          <Typography variant="h3" component="h1" sx={{ fontWeight: 700, minWidth: 0 }}>
            {title}
          </Typography>
        </Stack>
        {subtitle ? (
          typeof subtitle === 'string' ? (
            <Typography variant="body2" color="text.secondary">
              {subtitle}
            </Typography>
          ) : (
            subtitle
          )
        ) : null}
      </Stack>
      {actions ? (
        <Box sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}>{actions}</Box>
      ) : null}
    </Stack>
  )
}
