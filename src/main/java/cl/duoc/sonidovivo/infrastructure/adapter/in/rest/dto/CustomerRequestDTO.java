package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering or updating a customer profile.
 */
public record CustomerRequestDTO(
        @NotBlank(message = "RUT is required")
        @Pattern(
                regexp = "^\\d{1,2}(?:\\.?\\d{3}){2}-[0-9Kk]$",
                message = "Invalid Chilean RUT format"
        )
        String rut,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 254, message = "Email cannot exceed 254 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(min = 6, max = 25, message = "Phone must be between 6 and 25 characters")
        String phone,

        @NotBlank(message = "Commune is required")
        @Size(min = 2, max = 100, message = "Commune must be between 2 and 100 characters")
        String commune,

        @NotBlank(message = "Region is required")
        @Size(min = 2, max = 100, message = "Region must be between 2 and 100 characters")
        String region
) {
}
