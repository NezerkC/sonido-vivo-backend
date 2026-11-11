package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for appending a customer review to a product.
 */
public record ReviewCreateDTO(
        @NotBlank(message = "Customer name is required")
        @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
        String cliente,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Minimum rating is 1")
        @Max(value = 5, message = "Maximum rating is 5")
        Integer calificacion,

        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        String comentario
) {
}
