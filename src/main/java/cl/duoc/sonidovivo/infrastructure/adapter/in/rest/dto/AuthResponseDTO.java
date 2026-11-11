package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * JWT authentication response returned upon successful admin login.
 */
public record AuthResponseDTO(
        String token,
        String tokenType,
        long expiresInMs,
        String username,
        List<String> roles
) {
}
