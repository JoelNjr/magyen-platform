package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.InMemoryProductionReferenceImageStorage;
import com.magyen.platform.production.application.ProductionReferenceImageFixtures;
import com.magyen.platform.production.application.ProductionReferenceImageInspector;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionDocumentPdfResult;
import com.magyen.platform.production.application.dto.ProductionMaterialCostSummary;
import com.magyen.platform.production.application.dto.RemoveProductionReferenceImageCommand;
import com.magyen.platform.production.application.dto.ReplaceProductionReferenceImageCommand;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionReferenceImage;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.infrastructure.pdf.OpenPdfProductionDocumentAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionReferenceImageUseCaseTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private GetProductionOrderUseCase getProductionOrderUseCase;

    private InMemoryProductionReferenceImageStorage storage;
    private ReplaceProductionReferenceImageUseCase replaceUseCase;
    private RemoveProductionReferenceImageUseCase removeUseCase;
    private GenerateProductionOrderPdfUseCase generatePdfUseCase;

    @BeforeEach
    void setUp() {
        storage = new InMemoryProductionReferenceImageStorage();
        replaceUseCase = new ReplaceProductionReferenceImageUseCase(
                productionOrderRepository,
                storage,
                new ProductionReferenceImageInspector(),
                "test"
        );
        removeUseCase = new RemoveProductionReferenceImageUseCase(productionOrderRepository, storage);
        generatePdfUseCase = new GenerateProductionOrderPdfUseCase(
                getProductionOrderUseCase,
                new OpenPdfProductionDocumentAdapter(),
                storage
        );
    }

    @Test
    void uploadsValidImageAndStoresGeneratedKey() throws Exception {
        ProductionOrder productionOrder = productionOrder();
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));
        when(productionOrderRepository.save(productionOrder)).thenReturn(productionOrder);

        replaceUseCase.execute(new ReplaceProductionReferenceImageCommand(
                productionOrder.getId(),
                "uniforme.jpg",
                "image/jpeg",
                ProductionReferenceImageFixtures.jpegBytes()
        ));

        assertNotNull(productionOrder.getReferenceImage());
        assertTrue(storage.contains(productionOrder.getReferenceImage().getObjectKey()));
        assertTrue(productionOrder.getReferenceImage().getObjectKey().startsWith("test/production-orders/"));
        assertFalse(productionOrder.getReferenceImage().getObjectKey().contains("uniforme.jpg"));
    }

    @Test
    void rejectsInvalidFormatBeforeUploading() {
        ProductionOrder productionOrder = productionOrder();
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));

        assertThrows(
                IllegalArgumentException.class,
                () -> replaceUseCase.execute(new ReplaceProductionReferenceImageCommand(
                        productionOrder.getId(),
                        "archivo.gif",
                        "image/gif",
                        "GIF89a".getBytes()
                ))
        );

        assertNull(productionOrder.getReferenceImage());
        assertEquals(0, storage.size());
        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    void replacesExistingImageAfterPersistingTheNewKey() throws Exception {
        ProductionOrder productionOrder = productionOrder();
        productionOrder.attachReferenceImage(ProductionReferenceImage.of(
                "test/production-orders/" + productionOrder.getId() + "/old.jpg",
                "image/jpeg"
        ));
        storage.put(productionOrder.getReferenceImage().getObjectKey(), new byte[]{1}, "image/jpeg");
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));
        when(productionOrderRepository.save(productionOrder)).thenReturn(productionOrder);

        replaceUseCase.execute(new ReplaceProductionReferenceImageCommand(
                productionOrder.getId(),
                "nuevo.png",
                "image/png",
                ProductionReferenceImageFixtures.pngBytes()
        ));

        assertEquals(1, storage.size());
        assertTrue(storage.contains(productionOrder.getReferenceImage().getObjectKey()));
        assertTrue(productionOrder.getReferenceImage().getObjectKey().endsWith(".png"));
    }

    @Test
    void doesNotChangeOrderWhenStoragePutFails() throws Exception {
        ProductionOrder productionOrder = productionOrder();
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));
        storage.failOnPut(new IllegalStateException("R2 unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> replaceUseCase.execute(new ReplaceProductionReferenceImageCommand(
                        productionOrder.getId(),
                        "uniforme.jpg",
                        "image/jpeg",
                        ProductionReferenceImageFixtures.jpegBytes()
                ))
        );

        assertNull(productionOrder.getReferenceImage());
        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    void removesImageFromStorageAndClearsReference() {
        ProductionOrder productionOrder = productionOrder();
        String objectKey = "test/production-orders/" + productionOrder.getId() + "/old.jpg";
        productionOrder.attachReferenceImage(ProductionReferenceImage.of(objectKey, "image/jpeg"));
        storage.put(objectKey, new byte[]{1}, "image/jpeg");
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));
        when(productionOrderRepository.save(productionOrder)).thenReturn(productionOrder);

        removeUseCase.execute(new RemoveProductionReferenceImageCommand(productionOrder.getId()));

        assertNull(productionOrder.getReferenceImage());
        assertFalse(storage.contains(objectKey));
        verify(productionOrderRepository).save(productionOrder);
    }

    @Test
    void doesNotClearReferenceWhenStorageDeleteFails() {
        ProductionOrder productionOrder = productionOrder();
        String objectKey = "test/production-orders/" + productionOrder.getId() + "/old.jpg";
        productionOrder.attachReferenceImage(ProductionReferenceImage.of(objectKey, "image/jpeg"));
        when(productionOrderRepository.findById(productionOrder.getId())).thenReturn(Optional.of(productionOrder));
        storage.failOnDelete(new IllegalStateException("R2 unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> removeUseCase.execute(new RemoveProductionReferenceImageCommand(productionOrder.getId()))
        );

        assertNotNull(productionOrder.getReferenceImage());
        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    void generatePdfWithoutImageKeepsWorking() throws Exception {
        GetProductionOrderResult result = productionOrderResult(null);
        when(getProductionOrderUseCase.execute(any())).thenReturn(result);

        ProductionDocumentPdfResult pdf = generatePdfUseCase.execute(new GetProductionOrderCommand(result.productionOrderId()));
        String text = pdfText(pdf.content());

        assertTrue(text.contains("ORDEN DE PRODUCCIÓN"));
        assertFalse(text.contains("Imagen de referencia"));
    }

    @Test
    void generatePdfIncludesImageWhenStorageReturnsBytes() throws Exception {
        GetProductionOrderResult result = productionOrderResult("test/key.png");
        storage.put("test/key.png", ProductionReferenceImageFixtures.pngBytes(), "image/png");
        when(getProductionOrderUseCase.execute(any())).thenReturn(result);

        ProductionDocumentPdfResult pdf = generatePdfUseCase.execute(new GetProductionOrderCommand(result.productionOrderId()));
        String text = pdfText(pdf.content());

        assertTrue(text.contains("Imagen de referencia"));
    }

    @Test
    void generatePdfContinuesWhenStorageGetFails() throws Exception {
        GetProductionOrderResult result = productionOrderResult("test/key.png");
        storage.failOnGet(new IllegalStateException("R2 unavailable"));
        when(getProductionOrderUseCase.execute(any())).thenReturn(result);

        ProductionDocumentPdfResult pdf = generatePdfUseCase.execute(new GetProductionOrderCommand(result.productionOrderId()));
        String text = pdfText(pdf.content());

        assertTrue(text.contains("ORDEN DE PRODUCCIÓN"));
        assertFalse(text.contains("Imagen de referencia"));
    }

    private static ProductionOrder productionOrder() {
        return ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 20),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );
    }

    private static GetProductionOrderResult productionOrderResult(String objectKey) {
        return new GetProductionOrderResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "14",
                "Uniformes",
                UUID.randomUUID(),
                "Cliente",
                LocalDate.of(2026, 8, 20),
                ProductionStatus.CREATED,
                ProductionPriority.NORMAL,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                new ProductionMaterialCostSummary(null, 0, 0, 0),
                null,
                null,
                objectKey != null,
                objectKey
        );
    }

    private static String pdfText(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        } finally {
            reader.close();
        }
    }
}
