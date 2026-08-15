package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.ListAuthenticationUsersResult;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;

import java.util.List;
import java.util.Objects;

/**
 * Lista usuarios internos ordenados por username.
 */
public class ListAuthenticationUsersUseCase {

    private final AuthenticationUserRepository authenticationUserRepository;

    public ListAuthenticationUsersUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    public ListAuthenticationUsersResult execute() {
        List<AuthenticationUserView> users = authenticationUserRepository.findAllOrderByUsername().stream()
                .map(ListAuthenticationUsersUseCase::toView)
                .toList();
        return new ListAuthenticationUsersResult(users);
    }

    private static AuthenticationUserView toView(AuthenticationUser authenticationUser) {
        return new AuthenticationUserView(
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole(),
                authenticationUser.isEnabled()
        );
    }
}
