package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.port.CommercialSellerEmployeeInfo;
import com.magyen.platform.commercial.application.port.CommercialSellerEmployeePort;
import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Resuelve el nombre legible de un vendedor a partir de su identidad estable.
 * <p>
 * La fuente de verdad para selección nueva es Finance {@code PayrollEmployee}.
 * La tabla leftover {@code sellers} solo cubre lecturas históricas.
 */
public class SellerNameResolver {

    private final CommercialSellerEmployeePort commercialSellerEmployeePort;
    private final SellerRepository leftoverSellerRepository;

    public SellerNameResolver(
            CommercialSellerEmployeePort commercialSellerEmployeePort,
            SellerRepository leftoverSellerRepository
    ) {
        this.commercialSellerEmployeePort = Objects.requireNonNull(
                commercialSellerEmployeePort,
                "Commercial seller employee port must not be null"
        );
        this.leftoverSellerRepository = Objects.requireNonNull(
                leftoverSellerRepository,
                "Leftover seller repository must not be null"
        );
    }

    public CommercialSellerEmployeeInfo requireEligibleSeller(UUID sellerId) {
        Objects.requireNonNull(sellerId, "Seller id must not be null");
        return commercialSellerEmployeePort.requireEligibleSeller(sellerId);
    }

    public String resolveName(UUID sellerId) {
        if (sellerId == null) {
            return null;
        }

        return commercialSellerEmployeePort.findEmployeeDisplayName(sellerId)
                .or(() -> leftoverSellerRepository.findById(sellerId).map(Seller::getName))
                .orElse(null);
    }

    public Map<UUID, String> resolveNames(Collection<UUID> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, String> names = new HashMap<>(commercialSellerEmployeePort.findEmployeeDisplayNames(sellerIds));
        leftoverSellerRepository.findAll().stream()
                .filter(seller -> sellerIds.contains(seller.getId()))
                .forEach(seller -> names.putIfAbsent(seller.getId(), seller.getName()));
        return Map.copyOf(names);
    }

    public Function<UUID, String> nameLookup(Collection<UUID> sellerIds) {
        Map<UUID, String> names = resolveNames(sellerIds);
        return sellerId -> names.get(sellerId);
    }
}
