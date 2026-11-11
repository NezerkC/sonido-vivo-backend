package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.util.regex.Pattern;

/**
 * Immutable email address validated against a strict RFC-compliant pattern.
 */
public record EmailAddress(String value) {

    public static final String REGEX =
            "^(?=.{1,254}$)(?=.{1,64}@)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public EmailAddress {
        if (value == null || value.isBlank() || !PATTERN.matcher(value).matches()) {
            throw new InvalidDomainDataException("Invalid email address: " + value);
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
