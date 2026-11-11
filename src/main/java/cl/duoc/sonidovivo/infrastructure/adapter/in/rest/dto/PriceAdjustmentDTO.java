package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for batch percentage price adjustments across active catalog items.
 */
public record PriceAdjustmentDTO(
        @NotNull(message = "Percentage is required")
        @DecimalMin(value = "-50.0", message = "Discount cannot exceed 50%")
        @DecimalMax(value = "100.0", message = "Increase cannot exceed 100%")
        Double porcentaje,

        String categoria,

        @NotBlank(message = "Audit reason is required")
        String motivo
) {
}
