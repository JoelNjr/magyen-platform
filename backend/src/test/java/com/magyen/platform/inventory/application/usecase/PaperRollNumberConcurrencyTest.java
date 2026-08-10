package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PaperRollNumberConcurrencyTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Test
    void concurrentPaperRollCreationProducesUniqueRpNumbers() throws Exception {
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<CreateInventoryItemResult>> tasks = new ArrayList<>();

        for (int index = 0; index < threads; index++) {
            tasks.add(() -> createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                    "CNRP-" + UUID.randomUUID().toString().substring(0, 8),
                    "Papel concurrente",
                    "PAPER",
                    "METER",
                    new BigDecimal("100.0000"),
                    new BigDecimal("20.0000"),
                    null,
                    new BigDecimal("4500.00"),
                    "PAPER",
                    true
            )));
        }

        List<Future<CreateInventoryItemResult>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        Set<String> rollNumbers = new HashSet<>();
        for (Future<CreateInventoryItemResult> future : futures) {
            CreateInventoryItemResult result = future.get();
            assertTrue(result.plotterPaperRoll());
            assertTrue(rollNumbers.add(result.paperRollNumber()),
                    "Duplicate RP number: " + result.paperRollNumber());
        }

        assertEquals(threads, rollNumbers.size());
    }
}
