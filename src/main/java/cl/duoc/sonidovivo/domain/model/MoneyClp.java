package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.InvalidPriceMarginException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Immutable Chilean Peso (CLP) unit price constrained to [1000, 50000000].
 */
public record MoneyClp(int amount) {

    public static final int MIN_AMOUNT = 1_000;
    public static final int MAX_AMOUNT = 50_000_000;
    public static final double MIN_ADJUSTMENT_PERCENTAGE = -50.0;
    public static final double MAX_ADJUSTMENT_PERCENTAGE = 100.0;

    public MoneyClp {
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new InvalidDomainDataException(
                    "Price in CLP must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT + ", got: " + amount
            );
        }
    }

    public static MoneyClp of(int amount) {
        return new MoneyClp(amount);
    }

    /**
     * Validates that a percentage adjustment stays within allowed business margins [-50.0%, +100.0%].
     */
    public static void validateAdjustmentPercentage(double percentage) {
        if (!Double.isFinite(percentage)
                || percentage < MIN_ADJUSTMENT_PERCENTAGE
                || percentage > MAX_ADJUSTMENT_PERCENTAGE) {
            throw new InvalidPriceMarginException(
                    "Price adjustment percentage must be between "
                            + MIN_ADJUSTMENT_PERCENTAGE + "% and " + MAX_ADJUSTMENT_PERCENTAGE + "%, got: " + percentage
            );
        }
    }

    /**
     * Applies a percentage adjustment rounded to the nearest CLP integer (HALF_UP),
     * ensuring both the percentage margin and the resulting price floor/ceiling are respected.
     */
    public MoneyClp adjustByPercentage(double percentage) {
        validateAdjustmentPercentage(percentage);
        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(percentage).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );
        long adjusted = BigDecimal.valueOf(amount)
                .multiply(factor)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        if (adjusted < MIN_AMOUNT || adjusted > MAX_AMOUNT) {
            throw new InvalidPriceMarginException(
                    "Adjusted price " + adjusted + " CLP violates allowed price bounds ["
                            + MIN_AMOUNT + ", " + MAX_AMOUNT + "]"
            );
        }
        return new MoneyClp((int) adjusted);
    }

    public long multiplyBy(int quantity) {
        if (quantity < 0) {
            throw new InvalidDomainDataException("Multiplier quantity cannot be negative: " + quantity);
        }
        return (long) amount * quantity;
    }
}
