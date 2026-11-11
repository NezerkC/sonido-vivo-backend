package cl.duoc.sonidovivo.domain.exception;

/**
 * Thrown when input data or a value object invariant violates domain rules.
 */
public class InvalidDomainDataException extends RuntimeException {

    public InvalidDomainDataException(String message) {
        super(message);
    }

    public InvalidDomainDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
