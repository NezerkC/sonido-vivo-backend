package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request payload for {@code POST /api/v1/auth/login}.
 */
public record LoginRequestDTO(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
