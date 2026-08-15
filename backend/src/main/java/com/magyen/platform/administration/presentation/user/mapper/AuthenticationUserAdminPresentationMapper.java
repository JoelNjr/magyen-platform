package com.magyen.platform.administration.presentation.user.mapper;

import com.magyen.platform.administration.application.dto.ActivateAuthenticationUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.ChangeAuthenticationUserRoleCommand;
import com.magyen.platform.administration.application.dto.CreateAuthenticationUserCommand;
import com.magyen.platform.administration.application.dto.DeactivateAuthenticationUserCommand;
import com.magyen.platform.administration.application.dto.ListAuthenticationUsersResult;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import com.magyen.platform.administration.presentation.user.request.ChangeAuthenticationUserRoleRequest;
import com.magyen.platform.administration.presentation.user.request.CreateAuthenticationUserRequest;
import com.magyen.platform.administration.presentation.user.response.AuthenticationUserResponse;
import com.magyen.platform.administration.presentation.user.response.GetAuthenticationUsersResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para usuarios internos.
 */
public class AuthenticationUserAdminPresentationMapper {

    public CreateAuthenticationUserCommand toCreateCommand(CreateAuthenticationUserRequest request) {
        Objects.requireNonNull(request, "CreateAuthenticationUserRequest must not be null");
        return new CreateAuthenticationUserCommand(
                request.username(),
                request.password(),
                parseRole(request.role())
        );
    }

    public ActivateAuthenticationUserCommand toActivateCommand(UUID userId) {
        return new ActivateAuthenticationUserCommand(userId);
    }

    public DeactivateAuthenticationUserCommand toDeactivateCommand(UUID userId) {
        return new DeactivateAuthenticationUserCommand(userId);
    }

    public ChangeAuthenticationUserRoleCommand toChangeRoleCommand(
            UUID userId,
            ChangeAuthenticationUserRoleRequest request
    ) {
        Objects.requireNonNull(request, "ChangeAuthenticationUserRoleRequest must not be null");
        return new ChangeAuthenticationUserRoleCommand(userId, parseRole(request.role()));
    }

    public AuthenticationUserResponse toResponse(AuthenticationUserView view) {
        Objects.requireNonNull(view, "Authentication user view must not be null");
        return new AuthenticationUserResponse(
                view.id().toString(),
                view.username(),
                view.role().name(),
                view.enabled()
        );
    }

    public GetAuthenticationUsersResponse toResponse(ListAuthenticationUsersResult result) {
        Objects.requireNonNull(result, "ListAuthenticationUsersResult must not be null");
        List<AuthenticationUserResponse> users = result.users().stream()
                .map(this::toResponse)
                .toList();
        return new GetAuthenticationUsersResponse(users);
    }

    private static AuthenticationRole parseRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new AdministrationDomainException("Role must be ADMIN or OPERATOR");
        }
        try {
            return AuthenticationRole.valueOf(roleName.trim());
        } catch (IllegalArgumentException exception) {
            throw new AdministrationDomainException("Role must be ADMIN or OPERATOR");
        }
    }
}
