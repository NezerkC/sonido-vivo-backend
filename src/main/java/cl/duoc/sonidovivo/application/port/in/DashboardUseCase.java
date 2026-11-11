package cl.duoc.sonidovivo.application.port.in;

import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.DashboardMetrics;

/**
 * Inbound port defining administrative dashboard metrics and batch pricing operations.
 */
public interface DashboardUseCase {

    DashboardMetrics getInventoryMetrics();

    BatchPriceAdjustmentResult applyBatchPriceAdjustment(BatchPriceAdjustmentCommand command);

    /**
     * Command specifying a percentage price adjustment, optional category filter, and audit reason.
     */
    record BatchPriceAdjustmentCommand(
            double percentage,
            Category category,
            String reason
    ) {}

    /**
     * Summary returned after executing a batch price adjustment operation.
     */
    record BatchPriceAdjustmentResult(
            int updatedCount,
            double appliedPercentage,
            Category targetCategory,
            String reason
    ) {}
}
