package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateSellerCommand;
import com.magyen.platform.commercial.application.dto.CreateSellerResult;
import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la creación de un vendedor interno.
 */
public class CreateSellerUseCase {

    private final SellerRepository sellerRepository;

    public CreateSellerUseCase(SellerRepository sellerRepository) {
        this.sellerRepository = Objects.requireNonNull(sellerRepository, "Seller repository must not be null");
    }

    public CreateSellerResult execute(CreateSellerCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        String name = command.name().trim();
        ensureNameIsUnique(name);

        Seller seller = Seller.create(name);
        Seller savedSeller = sellerRepository.save(seller);

        return new CreateSellerResult(
                savedSeller.getId(),
                savedSeller.getName(),
                savedSeller.isActive()
        );
    }

    private void validateCommand(CreateSellerCommand command) {
        if (command.name() == null) {
            throw new IllegalArgumentException("Seller name must not be null");
        }

        if (command.name().isBlank()) {
            throw new IllegalArgumentException("Seller name must not be blank");
        }
    }

    private void ensureNameIsUnique(String name) {
        boolean nameAlreadyExists = sellerRepository.findAll().stream()
                .anyMatch(seller -> seller.getName().equalsIgnoreCase(name));

        if (nameAlreadyExists) {
            throw new IllegalArgumentException("Seller name already exists: " + name);
        }
    }
}
