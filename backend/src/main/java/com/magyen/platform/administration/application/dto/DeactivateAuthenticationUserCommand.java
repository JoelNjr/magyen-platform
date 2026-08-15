package com.magyen.platform.administration.application.dto;

import java.util.UUID;

public record DeactivateAuthenticationUserCommand(
        UUID userId
) {
}
