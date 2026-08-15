package com.magyen.platform.administration.infrastructure.persistence;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.infrastructure.persistence.entity.AuthenticationUserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthenticationUserPersistenceTest {

    private static final String RAW_PASSWORD = "never-store-me-plaintext";

    @Autowired
    private AuthenticationUserRepository authenticationUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private EntityManager entityManager;

    @Test
    void neverPersistsPlaintextPassword() {
        String username = "persist-" + UUID.randomUUID();
        AuthenticationUser saved = authenticationUserRepository.save(AuthenticationUser.create(
                username,
                passwordHasher.hash(RAW_PASSWORD),
                true,
                AuthenticationRole.OPERATOR
        ));

        entityManager.flush();
        entityManager.clear();

        AuthenticationUserEntity persisted = entityManager.find(AuthenticationUserEntity.class, saved.getId());
        assertNotEquals(RAW_PASSWORD, persisted.getPasswordHash());
        assertTrue(persisted.getPasswordHash().startsWith("$2a$") || persisted.getPasswordHash().startsWith("$2b$"));
        assertFalse(persisted.getPasswordHash().contains(RAW_PASSWORD));
    }
}
