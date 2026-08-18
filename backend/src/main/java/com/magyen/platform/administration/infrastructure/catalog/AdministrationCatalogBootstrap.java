package com.magyen.platform.administration.infrastructure.catalog;

import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Siembra los valores iniciales V1 de catálogo si aún no existen.
 * <p>
 * No duplica nombres. No crea filas de Inventario ni transacciones de Finanzas.
 * Administración no depende de Comercial para estas etiquetas iniciales.
 */
public class AdministrationCatalogBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrationCatalogBootstrap.class);

    static final List<String> INITIAL_GARMENTS = List.of(
            "Camiseta",
            "Camiseta tipo polo",
            "Conjunto deportivo",
            "Conjunto de presentación",
            "Pantaloneta",
            "Otro"
    );

    static final List<String> INITIAL_FABRICS = List.of(
            "Sudáfrica",
            "Piqué",
            "Hydrotech"
    );

    static final List<String> INITIAL_COLLARS = List.of(
            "Redondo",
            "En V recto",
            "En V cruzado",
            "Tejido"
    );

    static final List<String> INITIAL_SLEEVES = List.of(
            "Manga corta sisa",
            "Manga corta rangla",
            "Manga larga sisa",
            "Manga larga rangla"
    );

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;
    private final TransactionTemplate transactionTemplate;

    public AdministrationCatalogBootstrap(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.administrationCatalogEntryRepository = administrationCatalogEntryRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seedAll());
    }

    private void seedAll() {
        seed(AdministrationCatalogKind.GARMENT, INITIAL_GARMENTS);
        seed(AdministrationCatalogKind.FABRIC, INITIAL_FABRICS);
        seed(AdministrationCatalogKind.COLLAR, INITIAL_COLLARS);
        seed(AdministrationCatalogKind.SLEEVE, INITIAL_SLEEVES);
    }

    private void seed(AdministrationCatalogKind kind, List<String> names) {
        for (String name : names) {
            if (administrationCatalogEntryRepository.findByKindAndNameIgnoreCase(kind, name).isPresent()) {
                continue;
            }
            administrationCatalogEntryRepository.save(AdministrationCatalogEntry.create(kind, name));
            LOGGER.info("Seeded administration catalog {} value", kind.name());
        }
    }
}
