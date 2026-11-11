package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.util.regex.Pattern;

/**
 * Immutable product SKU code matching {@code ^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$}.
 */
public record ProductCode(String value) {

    public static final String REGEX = "^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public ProductCode {
        if (value == null || value.isBlank() || !PATTERN.matcher(value).matches()) {
            throw new InvalidDomainDataException(
                    "Invalid product code format (expected pattern like G-FND-STRAT-01): " + value
            );
        }
    }

    public static ProductCode of(String value) {
        return new ProductCode(value);
    }
}
