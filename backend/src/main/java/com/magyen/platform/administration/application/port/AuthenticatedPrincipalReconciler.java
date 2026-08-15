package com.magyen.platform.administration.application.port;

import java.util.Optional;

/**
 * Reconcilia el principal del JWT con el estado persistido.
 * <p>
 * Un token firmado no es suficiente si el usuario fue deshabilitado
 * o su rol cambió después de la emisión.
 */
public interface AuthenticatedPrincipalReconciler {

    Optional<AuthenticatedPrincipal> reconcile(AuthenticatedPrincipal tokenPrincipal);
}
