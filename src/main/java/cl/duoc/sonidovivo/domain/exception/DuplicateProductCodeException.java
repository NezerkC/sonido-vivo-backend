package cl.duoc.sonidovivo.domain.exception;

/**
 * Thrown when attempting to register a product with an already existing product code.
 */
public class DuplicateProductCodeException extends RuntimeException {

    public DuplicateProductCodeException(String message) {
        super(message);
    }

    public DuplicateProductCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
