package com.magyen.platform.commercial.infrastructure.persistence.mapper;

import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.infrastructure.persistence.entity.SellerEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link Seller} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class SellerPersistenceMapper {

    public SellerEntity toEntity(Seller seller) {
        Objects.requireNonNull(seller, "Seller must not be null");

        SellerEntity sellerEntity = new SellerEntity();
        sellerEntity.setId(seller.getId());
        sellerEntity.setName(seller.getName());
        sellerEntity.setActive(seller.isActive());
        return sellerEntity;
    }

    public Seller toDomain(SellerEntity sellerEntity) {
        Objects.requireNonNull(sellerEntity, "Seller entity must not be null");

        return Seller.reconstitute(
                sellerEntity.getId(),
                sellerEntity.getName(),
                sellerEntity.isActive()
        );
    }
}
