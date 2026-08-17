package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;
import com.magyen.platform.commercial.infrastructure.persistence.entity.SellerEntity;
import com.magyen.platform.commercial.infrastructure.persistence.mapper.SellerPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link SellerRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaSellerRepository implements SellerRepository {

    private final SpringDataSellerJpaRepository springDataSellerJpaRepository;
    private final SellerPersistenceMapper sellerPersistenceMapper;

    public JpaSellerRepository(
            SpringDataSellerJpaRepository springDataSellerJpaRepository,
            SellerPersistenceMapper sellerPersistenceMapper
    ) {
        this.springDataSellerJpaRepository = Objects.requireNonNull(
                springDataSellerJpaRepository,
                "Spring Data Seller JPA repository must not be null"
        );
        this.sellerPersistenceMapper = Objects.requireNonNull(
                sellerPersistenceMapper,
                "Seller persistence mapper must not be null"
        );
    }

    @Override
    public Seller save(Seller seller) {
        Objects.requireNonNull(seller, "Seller must not be null");

        SellerEntity sellerEntity = sellerPersistenceMapper.toEntity(seller);
        SellerEntity savedSellerEntity = springDataSellerJpaRepository.save(sellerEntity);
        return sellerPersistenceMapper.toDomain(savedSellerEntity);
    }

    @Override
    public Optional<Seller> findById(UUID id) {
        Objects.requireNonNull(id, "Seller id must not be null");

        return springDataSellerJpaRepository.findById(id)
                .map(sellerPersistenceMapper::toDomain);
    }

    @Override
    public List<Seller> findAll() {
        return springDataSellerJpaRepository.findAll().stream()
                .map(sellerPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Seller> findAllActive() {
        return springDataSellerJpaRepository.findByActiveTrue().stream()
                .map(sellerPersistenceMapper::toDomain)
                .toList();
    }
}
