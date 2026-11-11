package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating a product's lifecycle status (including soft discontinuation).
 */
public record ProductStatusUpdateDTO(
        @NotBlank(message = "Product status is required")
        @Pattern(
                regexp = "^(ACTIVO|AGOTADO|DESCONTINUADO)$",
                message = "Status must be ACTIVO, AGOTADO, or DESCONTINUADO"
        )
        String estado
) {
}
