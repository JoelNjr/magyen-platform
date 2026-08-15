package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticateUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticateUserResult;
import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AuthenticationFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class AuthenticateUserUseCaseTest {

    @Autowired
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Autowired
    private AuthenticationUserRepository authenticationUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void authenticatesWithValidCredentials() {
        String username = uniqueUsername("valid");
        String password = "correct-password";
        AuthenticationUser saved = saveUser(username, password, true);

        AuthenticateUserResult result = authenticateUserUseCase.execute(
                new AuthenticateUserCommand(username, password)
        );

        assertEquals(saved.getId(), result.userId());
        assertEquals(username, result.username());
        assertEquals("Bearer", result.tokenType());
        assertNotNull(result.accessToken());
        assertFalse(result.accessToken().isBlank());
        assertEquals(AuthenticationRole.OPERATOR, result.role());
    }

    @Test
    void rejectsInvalidPassword() {
        String username = uniqueUsername("wrong-password");
        saveUser(username, "correct-password", true);

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authenticateUserUseCase.execute(new AuthenticateUserCommand(username, "incorrect-password"))
        );

        assertEquals(AuthenticationFailedException.DEFAULT_MESSAGE, exception.getMessage());
    }

    @Test
    void rejectsUnknownUsername() {
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authenticateUserUseCase.execute(
                        new AuthenticateUserCommand("unknown-" + UUID.randomUUID(), "any-password")
                )
        );

        assertEquals(AuthenticationFailedException.DEFAULT_MESSAGE, exception.getMessage());
    }

    @Test
    void rejectsDisabledUser() {
        String username = uniqueUsername("disabled");
        saveUser(username, "correct-password", false);

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authenticateUserUseCase.execute(new AuthenticateUserCommand(username, "correct-password"))
        );

        assertEquals(AuthenticationFailedException.DEFAULT_MESSAGE, exception.getMessage());
    }

    private AuthenticationUser saveUser(String username, String rawPassword, boolean enabled) {
        return authenticationUserRepository.save(AuthenticationUser.create(
                username,
                passwordHasher.hash(rawPassword),
                enabled,
                AuthenticationRole.OPERATOR
        ));
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
