package cl.duoc.sonidovivo.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.InvalidPriceMarginException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

    @ParameterizedTest
    @ValueSource(strings = {"G-FND-STRAT-01", "AU-SHU-SM58-01", "ABCD-12345-ABCDEF1234-99"})
    @DisplayName("ProductCode accepts valid SKU patterns")
    void productCodeAcceptsValidPatterns(String code) {
        ProductCode productCode = assertDoesNotThrow(() -> ProductCode.of(code));
        assertEquals(code, productCode.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "g-fnd-strat-01", "G-FND-STRAT-1", "G-FND-STRAT-001", "ABCDE-12-AB-01", "G-FND-STRAT-01 "})
    @DisplayName("ProductCode rejects invalid SKU patterns")
    void productCodeRejectsInvalidPatterns(String invalidCode) {
        assertThrows(InvalidDomainDataException.class, () -> ProductCode.of(invalidCode));
    }

    @Test
    @DisplayName("ProductCode rejects null")
    void productCodeRejectsNull() {
        assertThrows(InvalidDomainDataException.class, () -> ProductCode.of(null));
    }

    @Test
    @DisplayName("MoneyClp enforces [1000, 50000000] bounds")
    void moneyClpEnforcesBounds() {
        assertEquals(1_000, MoneyClp.of(1_000).amount());
        assertEquals(50_000_000, MoneyClp.of(50_000_000).amount());

        assertThrows(InvalidDomainDataException.class, () -> MoneyClp.of(999));
        assertThrows(InvalidDomainDataException.class, () -> MoneyClp.of(0));
        assertThrows(InvalidDomainDataException.class, () -> MoneyClp.of(-500));
        assertThrows(InvalidDomainDataException.class, () -> MoneyClp.of(50_000_001));
    }

    @Test
    @DisplayName("MoneyClp adjusts by percentage with HALF_UP rounding and validates margin bounds")
    void moneyClpAdjustByPercentageAndMargins() {
        assertEquals(11_400, MoneyClp.of(10_000).adjustByPercentage(14.0).amount());
        assertEquals(1_161, MoneyClp.of(1_005).adjustByPercentage(15.5).amount());
        assertEquals(1_000, MoneyClp.of(2_000).adjustByPercentage(-50.0).amount());
        assertEquals(50_000_000, MoneyClp.of(25_000_000).adjustByPercentage(100.0).amount());

        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(10_000).adjustByPercentage(-50.01));
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(10_000).adjustByPercentage(100.01));
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(10_000).adjustByPercentage(Double.NaN));
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(10_000).adjustByPercentage(Double.POSITIVE_INFINITY));
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(1_500).adjustByPercentage(-50.0));
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(40_000_000).adjustByPercentage(50.0));
    }

    @Test
    @DisplayName("MoneyClp multiplyBy prevents negative multiplier")
    void moneyClpMultiplyBy() {
        assertEquals(45_000L, MoneyClp.of(15_000).multiplyBy(3));
        assertEquals(0L, MoneyClp.of(15_000).multiplyBy(0));
        assertThrows(InvalidDomainDataException.class, () -> MoneyClp.of(15_000).multiplyBy(-1));
    }

    @Test
    @DisplayName("StockQuantity validates non-negative stock and decrement/increment invariants")
    void stockQuantityInvariants() {
        assertThrows(InvalidDomainDataException.class, () -> StockQuantity.of(-1));

        StockQuantity stock = StockQuantity.of(10);
        assertFalse(stock.isOutOfStock());

        StockQuantity afterSale = stock.decrement(4);
        assertEquals(6, afterSale.value());

        StockQuantity emptied = afterSale.decrement(6);
        assertTrue(emptied.isOutOfStock());

        assertThrows(InsufficientStockException.class, () -> emptied.decrement(1));
        assertThrows(InsufficientStockException.class, () -> stock.decrement(11));
        assertThrows(InvalidDomainDataException.class, () -> stock.decrement(0));
        assertThrows(InvalidDomainDataException.class, () -> stock.decrement(-2));

        assertEquals(15, stock.increment(5).value());
        assertThrows(InvalidDomainDataException.class, () -> stock.increment(0));
        assertThrows(InvalidDomainDataException.class, () -> stock.increment(-3));
        assertThrows(InvalidDomainDataException.class, () -> StockQuantity.of(Integer.MAX_VALUE).increment(1));

        assertEquals(7, stock.adjust(-3).value());
        assertEquals(13, stock.adjust(3).value());
        assertThrows(InvalidDomainDataException.class, () -> stock.adjust(0));
        assertThrows(InsufficientStockException.class, () -> stock.adjust(Integer.MIN_VALUE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin@sonidovivo.cl", "victor.martinez+test@duoc.cl", "a@b.co"})
    @DisplayName("EmailAddress accepts strict valid emails")
    void emailAddressAcceptsValid(String validEmail) {
        assertEquals(validEmail, EmailAddress.of(validEmail).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "  ", "plainaddress", "@example.com", "user@",
            "user@.com", ".user@example.com", "user..name@example.com",
            "user@example", "user@-example.com", "user@example-.com",
            "user name@example.com", "user@example.c"
    })
    @DisplayName("EmailAddress rejects malformed emails")
    void emailAddressRejectsInvalid(String invalidEmail) {
        assertThrows(InvalidDomainDataException.class, () -> EmailAddress.of(invalidEmail));
    }

    @Test
    @DisplayName("EmailAddress rejects null")
    void emailAddressRejectsNull() {
        assertThrows(InvalidDomainDataException.class, () -> EmailAddress.of(null));
    }

    @Test
    @DisplayName("Category and ProductStatus resolve enum and display names")
    void categoryAndProductStatusResolution() {
        assertEquals(Category.AUDIO_PRO, Category.fromString("Audio Pro"));
        assertEquals(Category.AUDIO_PRO, Category.fromString("AUDIO_PRO"));
        assertEquals(Category.PERCUSION, Category.fromString("Percusión"));
        assertEquals("Cuerdas", Category.CUERDAS.getDisplayName());
        assertThrows(InvalidDomainDataException.class, () -> Category.fromString("Invalid"));
        assertThrows(InvalidDomainDataException.class, () -> Category.fromString(null));

        assertEquals(ProductStatus.ACTIVO, ProductStatus.fromString("activo"));
        assertEquals(ProductStatus.DESCONTINUADO, ProductStatus.fromString("DESCONTINUADO"));
        assertThrows(InvalidDomainDataException.class, () -> ProductStatus.fromString("UNKNOWN"));
    }

    @Test
    @DisplayName("Review validates reviewer, rating 1..5, comment length, and date")
    void reviewInvariants() {
        Review valid = Review.create("Camila", 5, "Excelente sonido");
        assertEquals("Camila", valid.customer());
        assertEquals(5, valid.rating());

        assertThrows(InvalidDomainDataException.class, () -> Review.create("A", 5, "Short name"));
        assertThrows(InvalidDomainDataException.class, () -> Review.create("Camila", 0, "Bad rating"));
        assertThrows(InvalidDomainDataException.class, () -> Review.create("Camila", 6, "Bad rating"));
        assertThrows(InvalidDomainDataException.class, () -> Review.create("Camila", 4, "x".repeat(501)));
        assertThrows(InvalidDomainDataException.class, () -> new Review("Camila", 4, "Ok", null));
        assertDoesNotThrow(() -> new Review("Camila", 1, null, Instant.now()));
    }
}
