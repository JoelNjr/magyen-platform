import { Card, CardContent, Skeleton, Typography } from '@mui/material'

export default function MetricCard({ title, value, loading, emphasize }) {
  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
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
              color: emphasize === 'positive'
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
