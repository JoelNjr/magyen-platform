/** MUI `md` starts at 900px. Below that the sidebar becomes a temporary drawer. */
export const COMPACT_NAVIGATION_MAX_WIDTH = 899

export const PERMANENT_DRAWER_WIDTH = 240

export const TEMPORARY_DRAWER_WIDTH = 'min(280px, 86vw)'

export function isCompactNavigationWidth(viewportWidth) {
  return Number(viewportWidth) <= COMPACT_NAVIGATION_MAX_WIDTH
}
