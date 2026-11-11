package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized REST error payload returned by {@code GlobalExceptionHandler} and security handlers.
 */
public record ErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(Instant.now(), status, error, message, path, Map.of());
    }

    public static ErrorResponseDTO withFieldErrors(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponseDTO(
                Instant.now(),
                status,
                error,
                message,
                path,
                fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors)
        );
    }
}
