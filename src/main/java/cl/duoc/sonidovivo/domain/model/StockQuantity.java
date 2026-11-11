package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;

/**
 * Immutable non-negative inventory stock quantity.
 */
public record StockQuantity(int value) {

    public StockQuantity {
        if (value < 0) {
            throw new InvalidDomainDataException("Stock quantity cannot be negative: " + value);
        }
    }

    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    public static StockQuantity zero() {
        return new StockQuantity(0);
    }

    public StockQuantity decrement(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainDataException("Decrement quantity must be positive: " + quantity);
        }
        if (quantity > value) {
            throw new InsufficientStockException(
                    "Insufficient stock: requested " + quantity + ", available " + value
            );
        }
        return new StockQuantity(value - quantity);
    }

    public StockQuantity increment(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainDataException("Increment quantity must be positive: " + quantity);
        }
        long updated = (long) value + quantity;
        if (updated > Integer.MAX_VALUE) {
            throw new InvalidDomainDataException("Stock quantity overflow");
        }
        return new StockQuantity((int) updated);
    }

    public StockQuantity adjust(int delta) {
        if (delta == 0) {
            throw new InvalidDomainDataException("Stock adjustment delta must be non-zero");
        }
        if (delta < 0) {
            long requestedDecrease = -(long) delta;
            if (requestedDecrease > value) {
                throw new InsufficientStockException(
                        "Insufficient stock: requested " + requestedDecrease + ", available " + value
                );
            }
            return new StockQuantity((int) (value - requestedDecrease));
        }
        return increment(delta);
    }

    public boolean isOutOfStock() {
        return value == 0;
    }
}
