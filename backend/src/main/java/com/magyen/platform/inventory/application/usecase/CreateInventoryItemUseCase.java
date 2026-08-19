package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryUnitOfMeasure;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.MaterialCodeGenerator;
import com.magyen.platform.inventory.domain.PaperRollNumberGenerator;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Crea un material de inventario con código generado y, si aplica, una sola compra.
 */
public class CreateInventoryItemUseCase {

    private static final String DEFAULT_PAPER_NAME = "Papel Plotter";
    private static final String DEFAULT_PAPER_CATEGORY = "PAPER";
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final InventoryItemRepository inventoryItemRepository;
    private final PaperRollNumberGenerator paperRollNumberGenerator;
    private final MaterialCodeGenerator materialCodeGenerator;
    private final RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    public CreateInventoryItemUseCase(
            InventoryItemRepository inventoryItemRepository,
            PaperRollNumberGenerator paperRollNumberGenerator,
            MaterialCodeGenerator materialCodeGenerator,
            RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.paperRollNumberGenerator = Objects.requireNonNull(
                paperRollNumberGenerator,
                "Paper roll number generator must not be null"
        );
        this.materialCodeGenerator = Objects.requireNonNull(
                materialCodeGenerator,
                "Material code generator must not be null"
        );
        this.registerInventoryPurchaseUseCase = Objects.requireNonNull(
                registerInventoryPurchaseUseCase,
                "Register inventory purchase use case must not be null"
        );
    }

    @Transactional
    public CreateInventoryItemResult execute(CreateInventoryItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        InventoryMaterialType materialType = resolveMaterialType(command.materialType());
        boolean paperRoll = materialType == InventoryMaterialType.PAPER || command.plotterPaperRoll();
        InventoryAcquisitionCommand acquisition = command.acquisition();

        if (paperRoll && materialType != InventoryMaterialType.PAPER) {
            throw new InventoryDomainException("Plotter paper rolls require material type PAPER");
        }

        String name = resolveName(command.name(), materialType);
        String category = resolveCategory(command.category(), materialType);
        String unitOfMeasure = paperRoll
                ? InventoryUnitOfMeasure.METER.getCode()
                : requireUnitOfMeasure(command.unitOfMeasure());
        String paperRollNumber = paperRoll ? paperRollNumberGenerator.nextPaperRollNumber() : null;
        MaterialCode materialCode = resolveMaterialCode(materialType);
        BigDecimal initialStock = acquisition == null ? requireStock(command.stock()) : BigDecimal.ZERO;
        BigDecimal unitCost = resolveCreateUnitCost(command, acquisition, materialType);

        InventoryItem inventoryItem = InventoryItem.create(
                materialCode,
                name,
                category,
                unitOfMeasure,
                initialStock,
                command.minimumStock(),
                command.description(),
                unitCost,
                materialType,
                paperRollNumber
        );

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        if (acquisition != null) {
            registerInitialPurchase(savedInventoryItem, acquisition, materialType, command.stock());
            savedInventoryItem = inventoryItemRepository.findById(savedInventoryItem.getId())
                    .orElse(savedInventoryItem);
        }

        return toResult(savedInventoryItem);
    }

    private MaterialCode resolveMaterialCode(InventoryMaterialType materialType) {
        if (materialType == InventoryMaterialType.PAPER) {
            return inventoryItemRepository.findFirstByMaterialType(InventoryMaterialType.PAPER)
                    .map(InventoryItem::getMaterialCode)
                    .orElseGet(materialCodeGenerator::nextMaterialCode);
        }

        MaterialCode generated = materialCodeGenerator.nextMaterialCode();
        if (inventoryItemRepository.existsNonPaperWithCode(generated)) {
            throw new InventoryDomainException(
                    "An inventory item already exists with code: " + generated.getValue()
            );
        }
        return generated;
    }

    private void registerInitialPurchase(
            InventoryItem inventoryItem,
            InventoryAcquisitionCommand acquisition,
            InventoryMaterialType materialType,
            BigDecimal stockMeters
    ) {
        LocalDate purchaseDate = acquisition.purchaseDate() == null ? LocalDate.now() : acquisition.purchaseDate();

        if (materialType == InventoryMaterialType.PAPER) {
            BigDecimal rollQuantity = acquisition.quantity() == null
                    ? BigDecimal.ONE
                    : requirePositive(acquisition.quantity(), "Paper roll quantity must be greater than zero");
            BigDecimal pricePerRoll = requirePositive(
                    acquisition.unitCost(),
                    "Paper purchase price per roll must be greater than zero"
            );
            BigDecimal meters = requirePositive(stockMeters, "Paper roll meters must be greater than zero");
            BigDecimal totalExpense = rollQuantity.multiply(pricePerRoll).setScale(MONEY_SCALE, MONEY_ROUNDING);
            BigDecimal unitCostPerMeter = totalExpense.divide(meters, MONEY_SCALE, MONEY_ROUNDING);

            registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                    inventoryItem.getId(),
                    acquisition.purchaseId(),
                    meters,
                    unitCostPerMeter,
                    purchaseDate,
                    acquisition.observation(),
                    totalExpense
            ));
            return;
        }

        BigDecimal quantity = requirePositive(acquisition.quantity(), "Purchase quantity must be greater than zero");
        BigDecimal unitCost = resolvePurchaseUnitCost(acquisition, materialType, quantity);

        registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                inventoryItem.getId(),
                acquisition.purchaseId(),
                quantity,
                unitCost,
                purchaseDate,
                acquisition.observation()
        ));
    }

    private static BigDecimal resolvePurchaseUnitCost(
            InventoryAcquisitionCommand acquisition,
            InventoryMaterialType materialType,
            BigDecimal quantity
    ) {
        if (materialType == InventoryMaterialType.FABRIC) {
            return requirePositive(acquisition.unitCost(), "Fabric purchase cost per meter must be greater than zero");
        }

        if (acquisition.totalCost() != null) {
            BigDecimal total = requirePositive(acquisition.totalCost(), "Purchase total cost must be greater than zero");
            return total.divide(quantity, MONEY_SCALE, MONEY_ROUNDING);
        }

        return requirePositive(acquisition.unitCost(), "Purchase unit cost must be greater than zero");
    }

    private static BigDecimal resolveCreateUnitCost(
            CreateInventoryItemCommand command,
            InventoryAcquisitionCommand acquisition,
            InventoryMaterialType materialType
    ) {
        if (acquisition == null) {
            return command.unitCost();
        }
        if (materialType == InventoryMaterialType.PAPER
                && acquisition.unitCost() != null
                && command.stock() != null
                && command.stock().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rollQuantity = acquisition.quantity() == null ? BigDecimal.ONE : acquisition.quantity();
            BigDecimal totalExpense = rollQuantity
                    .multiply(acquisition.unitCost())
                    .setScale(MONEY_SCALE, MONEY_ROUNDING);
            return totalExpense.divide(command.stock(), MONEY_SCALE, MONEY_ROUNDING);
        }
        if (materialType == InventoryMaterialType.FABRIC) {
            return acquisition.unitCost();
        }
        if (acquisition.totalCost() != null && acquisition.quantity() != null
                && acquisition.quantity().compareTo(BigDecimal.ZERO) > 0) {
            return acquisition.totalCost().divide(acquisition.quantity(), MONEY_SCALE, MONEY_ROUNDING);
        }
        return acquisition.unitCost();
    }

    private static String resolveName(String name, InventoryMaterialType materialType) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        if (materialType == InventoryMaterialType.PAPER) {
            return DEFAULT_PAPER_NAME;
        }
        throw new IllegalArgumentException("Name must not be blank");
    }

    private static String resolveCategory(String category, InventoryMaterialType materialType) {
        if (category != null && !category.isBlank()) {
            return category.trim();
        }
        if (materialType == InventoryMaterialType.PAPER) {
            return DEFAULT_PAPER_CATEGORY;
        }
        throw new IllegalArgumentException("Category must not be blank");
    }

    private static String requireUnitOfMeasure(String unitOfMeasure) {
        if (unitOfMeasure == null || unitOfMeasure.isBlank()) {
            throw new IllegalArgumentException("Unit of measure must not be blank");
        }
        return unitOfMeasure;
    }

    private static BigDecimal requireStock(BigDecimal stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock must not be null");
        }
        return stock;
    }

    private static BigDecimal requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InventoryDomainException(message);
        }
        return value;
    }

    private static InventoryMaterialType resolveMaterialType(String materialType) {
        if (materialType == null || materialType.isBlank()) {
            return InventoryMaterialType.OTHER;
        }
        return InventoryMaterialType.of(materialType);
    }

    private CreateInventoryItemResult toResult(InventoryItem inventoryItem) {
        return new CreateInventoryItemResult(
                inventoryItem.getId(),
                inventoryItem.getMaterialCode().getValue(),
                inventoryItem.getName(),
                inventoryItem.getCategory(),
                inventoryItem.getUnitOfMeasure(),
                inventoryItem.getStock(),
                inventoryItem.getMinimumStock(),
                inventoryItem.getStatus(),
                inventoryItem.getDescription(),
                inventoryItem.isLowStock(),
                inventoryItem.getUnitCost(),
                inventoryItem.getMaterialType(),
                inventoryItem.getPaperRollNumber(),
                inventoryItem.isPlotterPaperRoll()
        );
    }
}
