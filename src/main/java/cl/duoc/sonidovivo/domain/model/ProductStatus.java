package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.util.Locale;

/**
 * Product lifecycle states supporting active catalog, out-of-stock, and soft deletion.
 */
public enum ProductStatus {
    ACTIVO,
    AGOTADO,
    DESCONTINUADO;

    public static ProductStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainDataException("Product status must not be null or blank");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (ProductStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        throw new InvalidDomainDataException("Unknown product status: " + value);
    }
}
