import { Box } from '@mui/material'
import { MAGYEN_LOGO_PUBLIC_PATH } from '../theme/magyenColors'

export default function MagyenLogo({ size = 40, alt = 'Confecciones Magyen' }) {
  return (
    <Box
      component="img"
      src={MAGYEN_LOGO_PUBLIC_PATH}
      alt={alt}
      sx={{
        width: size,
        height: size,
        objectFit: 'contain',
        display: 'block',
        flexShrink: 0,
      }}
    />
  )
}
