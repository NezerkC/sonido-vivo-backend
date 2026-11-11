package cl.duoc.sonidovivo.domain.exception;

/**
 * Thrown when a price adjustment percentage or resulting price violates margin bounds.
 */
public class InvalidPriceMarginException extends RuntimeException {

    public InvalidPriceMarginException(String message) {
        super(message);
    }

    public InvalidPriceMarginException(String message, Throwable cause) {
        super(message, cause);
    }
}
