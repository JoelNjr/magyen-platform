package com.magyen.platform.administration.presentation.auth.mapper;

import com.magyen.platform.administration.application.dto.AuthenticateUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticateUserResult;
import com.magyen.platform.administration.application.dto.GetAuthenticatedUserResult;
import com.magyen.platform.administration.presentation.auth.request.LoginRequest;
import com.magyen.platform.administration.presentation.auth.response.AuthenticatedUserResponse;
import com.magyen.platform.administration.presentation.auth.response.LoginResponse;

import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para autenticación.
 */
public class AuthPresentationMapper {

    public AuthenticateUserCommand toCommand(LoginRequest request) {
        Objects.requireNonNull(request, "LoginRequest must not be null");
        return new AuthenticateUserCommand(request.username(), request.password());
    }

    public LoginResponse toResponse(AuthenticateUserResult result) {
        Objects.requireNonNull(result, "AuthenticateUserResult must not be null");
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresInSeconds(),
                result.userId(),
                result.username(),
                result.role().name()
        );
    }

    public AuthenticatedUserResponse toResponse(GetAuthenticatedUserResult result) {
        Objects.requireNonNull(result, "GetAuthenticatedUserResult must not be null");
        return new AuthenticatedUserResponse(
                result.userId(),
                result.username(),
                result.role().name(),
                result.enabled()
        );
    }
}
