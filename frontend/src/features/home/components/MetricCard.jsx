import { Card, CardContent, Skeleton, Typography } from '@mui/material'

function resolveAccentColor(emphasize, tone) {
  if (tone === 'income' || emphasize === 'positive') {
    return 'success.main'
  }
  if (tone === 'expense' || emphasize === 'negative') {
    return 'error.main'
  }
  if (tone === 'pending' || tone === 'waste') {
    return 'warning.main'
  }
  if (tone === 'external') {
    return 'info.main'
  }
  if (tone === 'internal') {
    return 'secondary.main'
  }
  return null
}

export default function MetricCard({ title, value, loading, emphasize, tone }) {
  const accent = resolveAccentColor(emphasize, tone)

  return (
    <Card
      variant="outlined"
      sx={{
        height: '100%',
        ...(accent
          ? {
              borderLeftWidth: 3,
              borderLeftColor: accent,
            }
          : {}),
      }}
    >
      <CardContent>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          {title}
        </Typography>
        {loading ? (
          <Skeleton variant="text" width="60%" height={36} />
        ) : (
          <Typography
            variant="h5"
            sx={{
              fontWeight: emphasize ? 700 : 600,
              overflowWrap: 'anywhere',
              wordBreak: 'break-word',
              color:
                emphasize === 'positive'
                  ? 'success.main'
                  : emphasize === 'negative'
                    ? 'error.main'
                    : 'text.primary',
            }}
          >
            {value}
          </Typography>
        )}
      </CardContent>
    </Card>
  )
}
