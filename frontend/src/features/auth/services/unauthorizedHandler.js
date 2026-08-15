let unauthorizedHandler = null
let redirectInProgress = false

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export function notifyUnauthorized() {
  if (redirectInProgress) {
    return
  }

  redirectInProgress = true
  unauthorizedHandler?.()
}

export function resetUnauthorizedRedirect() {
  redirectInProgress = false
}
