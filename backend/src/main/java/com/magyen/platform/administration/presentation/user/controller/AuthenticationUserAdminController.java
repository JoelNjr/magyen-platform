package com.magyen.platform.administration.presentation.user.controller;

import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.ListAuthenticationUsersResult;
import com.magyen.platform.administration.application.usecase.ActivateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.ChangeAuthenticationUserRoleUseCase;
import com.magyen.platform.administration.application.usecase.CreateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.DeactivateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.ListAuthenticationUsersUseCase;
import com.magyen.platform.administration.presentation.user.mapper.AuthenticationUserAdminPresentationMapper;
import com.magyen.platform.administration.presentation.user.request.ChangeAuthenticationUserRoleRequest;
import com.magyen.platform.administration.presentation.user.request.CreateAuthenticationUserRequest;
import com.magyen.platform.administration.presentation.user.response.AuthenticationUserResponse;
import com.magyen.platform.administration.presentation.user.response.GetAuthenticationUsersResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la administración interna de usuarios. Solo ADMIN en el filtro de seguridad.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AuthenticationUserAdminController {

    private final ListAuthenticationUsersUseCase listAuthenticationUsersUseCase;
    private final CreateAuthenticationUserUseCase createAuthenticationUserUseCase;
    private final ActivateAuthenticationUserUseCase activateAuthenticationUserUseCase;
    private final DeactivateAuthenticationUserUseCase deactivateAuthenticationUserUseCase;
    private final ChangeAuthenticationUserRoleUseCase changeAuthenticationUserRoleUseCase;
    private final AuthenticationUserAdminPresentationMapper authenticationUserAdminPresentationMapper;

    public AuthenticationUserAdminController(
            ListAuthenticationUsersUseCase listAuthenticationUsersUseCase,
            CreateAuthenticationUserUseCase createAuthenticationUserUseCase,
            ActivateAuthenticationUserUseCase activateAuthenticationUserUseCase,
            DeactivateAuthenticationUserUseCase deactivateAuthenticationUserUseCase,
            ChangeAuthenticationUserRoleUseCase changeAuthenticationUserRoleUseCase,
            AuthenticationUserAdminPresentationMapper authenticationUserAdminPresentationMapper
    ) {
        this.listAuthenticationUsersUseCase = listAuthenticationUsersUseCase;
        this.createAuthenticationUserUseCase = createAuthenticationUserUseCase;
        this.activateAuthenticationUserUseCase = activateAuthenticationUserUseCase;
        this.deactivateAuthenticationUserUseCase = deactivateAuthenticationUserUseCase;
        this.changeAuthenticationUserRoleUseCase = changeAuthenticationUserRoleUseCase;
        this.authenticationUserAdminPresentationMapper = authenticationUserAdminPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetAuthenticationUsersResponse> listUsers() {
        ListAuthenticationUsersResult result = listAuthenticationUsersUseCase.execute();
        return ResponseEntity.ok(authenticationUserAdminPresentationMapper.toResponse(result));
    }

    @PostMapping
    public ResponseEntity<AuthenticationUserResponse> createUser(
            @RequestBody CreateAuthenticationUserRequest request
    ) {
        AuthenticationUserView result = createAuthenticationUserUseCase.execute(
                authenticationUserAdminPresentationMapper.toCreateCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationUserAdminPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<AuthenticationUserResponse> activateUser(@PathVariable UUID userId) {
        AuthenticationUserView result = activateAuthenticationUserUseCase.execute(
                authenticationUserAdminPresentationMapper.toActivateCommand(userId)
        );
        return ResponseEntity.ok(authenticationUserAdminPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<AuthenticationUserResponse> deactivateUser(@PathVariable UUID userId) {
        AuthenticationUserView result = deactivateAuthenticationUserUseCase.execute(
                authenticationUserAdminPresentationMapper.toDeactivateCommand(userId)
        );
        return ResponseEntity.ok(authenticationUserAdminPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AuthenticationUserResponse> changeRole(
            @PathVariable UUID userId,
            @RequestBody ChangeAuthenticationUserRoleRequest request
    ) {
        AuthenticationUserView result = changeAuthenticationUserRoleUseCase.execute(
                authenticationUserAdminPresentationMapper.toChangeRoleCommand(userId, request)
        );
        return ResponseEntity.ok(authenticationUserAdminPresentationMapper.toResponse(result));
    }
}
