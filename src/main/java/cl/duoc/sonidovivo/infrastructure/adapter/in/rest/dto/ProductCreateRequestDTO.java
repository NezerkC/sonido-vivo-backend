package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new catalog product with polymorphic specifications.
 */
public record ProductCreateRequestDTO(
        @NotBlank(message = "Product code is required")
        @Pattern(
                regexp = "^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$",
                message = "Invalid product code format (example: G-FND-STRAT-01)"
        )
        String codigo,

        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
        String nombre,

        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = "^(Cuerdas|Audio Pro|Percusion|Teclados|Vientos|Accesorios)$",
                message = "Unsupported category"
        )
        String categoria,

        @NotNull(message = "Price in CLP is required")
        @Min(value = 1000, message = "Minimum price is 1000 CLP")
        @Max(value = 50000000, message = "Maximum price is 50000000 CLP")
        @JsonAlias("precio_clp")
        Integer precioClp,

        @NotNull(message = "Initial stock is required")
        @Min(value = 0, message = "Initial stock cannot be negative")
        @JsonAlias("stock_disponible")
        Integer stockDisponible,

        Map<String, Object> especificaciones,
        List<String> tags
) {
}
