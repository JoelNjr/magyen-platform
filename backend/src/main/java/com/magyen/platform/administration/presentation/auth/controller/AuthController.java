package com.magyen.platform.administration.presentation.auth.controller;

import com.magyen.platform.administration.application.dto.AuthenticateUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticateUserResult;
import com.magyen.platform.administration.application.dto.GetAuthenticatedUserQuery;
import com.magyen.platform.administration.application.dto.GetAuthenticatedUserResult;
import com.magyen.platform.administration.application.port.AuthenticatedPrincipal;
import com.magyen.platform.administration.application.usecase.AuthenticateUserUseCase;
import com.magyen.platform.administration.application.usecase.GetAuthenticatedUserUseCase;
import com.magyen.platform.administration.presentation.auth.mapper.AuthPresentationMapper;
import com.magyen.platform.administration.presentation.auth.request.LoginRequest;
import com.magyen.platform.administration.presentation.auth.response.AuthenticatedUserResponse;
import com.magyen.platform.administration.presentation.auth.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el contrato REST mínimo de autenticación.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;
    private final AuthPresentationMapper authPresentationMapper;

    public AuthController(
            AuthenticateUserUseCase authenticateUserUseCase,
            GetAuthenticatedUserUseCase getAuthenticatedUserUseCase,
            AuthPresentationMapper authPresentationMapper
    ) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.getAuthenticatedUserUseCase = getAuthenticatedUserUseCase;
        this.authPresentationMapper = authPresentationMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AuthenticateUserCommand command = authPresentationMapper.toCommand(request);
        AuthenticateUserResult result = authenticateUserUseCase.execute(command);
        return ResponseEntity.ok(authPresentationMapper.toResponse(result));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(Authentication authentication) {
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) authentication.getPrincipal();
        GetAuthenticatedUserResult result = getAuthenticatedUserUseCase.execute(
                new GetAuthenticatedUserQuery(principal.userId())
        );
        return ResponseEntity.ok(authPresentationMapper.toResponse(result));
    }
}
