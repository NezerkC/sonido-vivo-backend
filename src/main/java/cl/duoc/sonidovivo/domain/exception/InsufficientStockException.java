package cl.duoc.sonidovivo.domain.exception;

/**
 * Thrown when a stock decrement exceeds the available warehouse stock.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
