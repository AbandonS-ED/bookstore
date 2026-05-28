export const COVER_GRADIENTS = [
  'linear-gradient(160deg, #5D4037 0%, #3E2723 60%, #2C1A12 100%)',
  'linear-gradient(160deg, #6D4C41 0%, #4E342E 100%)',
  'linear-gradient(160deg, #1A1A2E 0%, #16213E 50%, #0F3460 100%)',
  'linear-gradient(160deg, #3D2B1F 0%, #5C3A21 100%)',
  'linear-gradient(160deg, #5C1A1A 0%, #3A0A0A 100%)',
  'linear-gradient(160deg, #1B3A2D 0%, #0D2818 100%)',
  'linear-gradient(160deg, #2C3E50 0%, #1A252F 100%)',
  'linear-gradient(160deg, #3C1F3A 0%, #2A1526 100%)',
  'linear-gradient(160deg, #8B4513 0%, #5C2D0E 100%)',
  'linear-gradient(160deg, #4A3A2A 0%, #2A1A0A 100%)',
  'linear-gradient(160deg, #2A4A3A 0%, #1A2A1A 100%)',
  'linear-gradient(160deg, #3A2A4A 0%, #2A1A3A 100%)',
]

export function getCoverGradient(id) {
  const num = typeof id === 'string' ? parseInt(id, 10) || 1 : (id || 1)
  return COVER_GRADIENTS[(num - 1) % COVER_GRADIENTS.length]
}

export function getCoverStyle(id) {
  return { background: getCoverGradient(id) }
}
