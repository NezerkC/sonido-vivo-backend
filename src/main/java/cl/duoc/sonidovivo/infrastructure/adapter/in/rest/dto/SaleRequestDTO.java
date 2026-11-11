package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for registering a sale across MongoDB inventory and Oracle PL/SQL.
 */
public record SaleRequestDTO(
        @NotBlank(message = "Customer id is required")
        String customerId,

        @NotBlank(message = "Product code is required")
        @Pattern(
                regexp = "^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$",
                message = "Invalid product code format"
        )
        String productCode,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Sale quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "Unit price in CLP is required")
        @Min(value = 1000, message = "Minimum unit price is 1000 CLP")
        @Max(value = 50000000, message = "Maximum unit price is 50000000 CLP")
        Integer unitPriceClp
) {
}
