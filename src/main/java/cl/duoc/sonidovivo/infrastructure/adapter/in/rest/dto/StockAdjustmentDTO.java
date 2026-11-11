package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for atomic stock adjustments ({@code $inc}).
 */
public record StockAdjustmentDTO(
        @NotNull(message = "Adjustment delta is required")
        Integer delta,

        @NotBlank(message = "Adjustment reason is required")
        @Pattern(
                regexp = "^(VENTA_DIRECTA_MOSTRADOR|DEVOLUCION_CLIENTE|MERMA_AVERIA|INGRESO_PROVEEDOR)$",
                message = "Unsupported stock adjustment reason"
        )
        String motivo
) {
}
