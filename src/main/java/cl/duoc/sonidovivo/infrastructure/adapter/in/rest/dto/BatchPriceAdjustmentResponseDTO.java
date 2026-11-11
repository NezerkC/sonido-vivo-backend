package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import cl.duoc.sonidovivo.application.port.in.DashboardUseCase.BatchPriceAdjustmentResult;

/**
 * Response DTO summarizing the outcome of a batch percentage price adjustment.
 */
public record BatchPriceAdjustmentResponseDTO(
        int updatedCount,
        double appliedPercentage,
        String targetCategory,
        String reason
) {

    public static BatchPriceAdjustmentResponseDTO fromResult(BatchPriceAdjustmentResult result) {
        return new BatchPriceAdjustmentResponseDTO(
                result.updatedCount(),
                result.appliedPercentage(),
                result.targetCategory() != null ? result.targetCategory().getDisplayName() : null,
                result.reason()
        );
    }
}
