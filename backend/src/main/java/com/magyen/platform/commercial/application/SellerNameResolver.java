package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el nombre legible de un vendedor a partir de su identidad estable.
 * <p>
 * No es un caso de uso; solo evita acoplar lecturas comerciales al texto libre.
 */
public class SellerNameResolver {

    private final SellerRepository sellerRepository;

    public SellerNameResolver(SellerRepository sellerRepository) {
        this.sellerRepository = Objects.requireNonNull(sellerRepository, "Seller repository must not be null");
    }

    public Seller requireActiveSeller(UUID sellerId) {
        Objects.requireNonNull(sellerId, "Seller id must not be null");

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));

        if (!seller.isActive()) {
            throw new IllegalArgumentException("Seller is not active: " + sellerId);
        }

        return seller;
    }

    public String resolveName(UUID sellerId) {
        if (sellerId == null) {
            return null;
        }

        return sellerRepository.findById(sellerId)
                .map(Seller::getName)
                .orElse(null);
    }

    public Map<UUID, String> resolveNames(Collection<UUID> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return Map.of();
        }

        return sellerRepository.findAll().stream()
                .filter(seller -> sellerIds.contains(seller.getId()))
                .collect(Collectors.toMap(Seller::getId, Seller::getName, (left, right) -> left));
    }

    public Function<UUID, String> nameLookup(Collection<UUID> sellerIds) {
        Map<UUID, String> names = resolveNames(sellerIds);
        return sellerId -> names.get(sellerId);
    }
}
