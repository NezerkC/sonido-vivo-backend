package cl.duoc.sonidovivo.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.InvalidPriceMarginException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductDomainTest {

    private Product sampleProduct(int priceClp, int stock) {
        return Product.create(
                ProductCode.of("G-FND-STRAT-01"),
                "Fender Stratocaster Player",
                Category.CUERDAS,
                MoneyClp.of(priceClp),
                StockQuantity.of(stock),
                Map.of("tipo", "guitarra electrica", "cuerdas", 6),
                List.of("fender", "electrica", "fender")
        );
    }

    @Test
    @DisplayName("Product creation derives initial status and deduplicates tags")
    void productCreationInvariants() {
        Product active = sampleProduct(899_990, 5);
        assertEquals(ProductStatus.ACTIVO, active.getStatus());
        assertEquals(List.of("fender", "electrica"), active.getTags());
        assertEquals(4_499_950L, active.calculateInventoryValueClp());
        assertNotNull(active.getCreatedAt());

        Product outOfStock = sampleProduct(899_990, 0);
        assertEquals(ProductStatus.AGOTADO, outOfStock.getStatus());

        assertThrows(InvalidDomainDataException.class, () -> Product.create(
                ProductCode.of("G-FND-STRAT-01"),
                "AB",
                Category.CUERDAS,
                MoneyClp.of(10_000),
                StockQuantity.of(1),
                Map.of(),
                List.of()
        ));
    }

    @Test
    @DisplayName("Product stock decrement updates stock, sets AGOTADO at zero, and rejects overselling")
    void productStockDecrementRules() {
        Product product = sampleProduct(500_000, 5);

        product.decrementStock(3);
        assertEquals(2, product.getStock().value());
        assertEquals(ProductStatus.ACTIVO, product.getStatus());
        assertNotNull(product.getLastSaleAt());

        assertThrows(InsufficientStockException.class, () -> product.decrementStock(3));
        assertEquals(2, product.getStock().value());

        product.decrementStock(2);
        assertEquals(0, product.getStock().value());
        assertEquals(ProductStatus.AGOTADO, product.getStatus());

        product.incrementStock(4);
        assertEquals(4, product.getStock().value());
        assertEquals(ProductStatus.ACTIVO, product.getStatus());

        product.adjustStock(-1);
        assertEquals(3, product.getStock().value());
    }

    @Test
    @DisplayName("Discontinued product forbids stock adjustments and records discontinuedAt")
    void discontinuedProductRules() {
        Product product = sampleProduct(500_000, 5);
        product.changeStatus(ProductStatus.DESCONTINUADO);

        assertEquals(ProductStatus.DESCONTINUADO, product.getStatus());
        assertNotNull(product.getDiscontinuedAt());
        assertThrows(InvalidDomainDataException.class, () -> product.decrementStock(1));
        assertThrows(InvalidDomainDataException.class, () -> product.incrementStock(1));

        product.changeStatus(ProductStatus.ACTIVO);
        assertNull(product.getDiscontinuedAt());
    }

    @Test
    @DisplayName("Product price adjustment enforces margin percentages and CLP bounds without mutating on failure")
    void productPriceAdjustmentMarginRules() {
        Product product = sampleProduct(100_000, 3);
        product.adjustPrice(15.5);
        assertEquals(115_500, product.getPrice().amount());

        assertThrows(InvalidPriceMarginException.class, () -> product.adjustPrice(-50.5));
        assertEquals(115_500, product.getPrice().amount());

        assertThrows(InvalidPriceMarginException.class, () -> product.adjustPrice(100.5));
        assertEquals(115_500, product.getPrice().amount());

        Product lowPriceProduct = sampleProduct(1_200, 10);
        assertThrows(InvalidPriceMarginException.class, () -> lowPriceProduct.adjustPrice(-50.0));
        assertEquals(1_200, lowPriceProduct.getPrice().amount());

        Product highPriceProduct = sampleProduct(40_000_000, 1);
        assertThrows(InvalidPriceMarginException.class, () -> highPriceProduct.adjustPrice(50.0));
        assertEquals(40_000_000, highPriceProduct.getPrice().amount());
    }

    @Test
    @DisplayName("Product adds reviews and exposes unmodifiable collections")
    void productReviewsAndDefensiveCopies() {
        Product product = sampleProduct(150_000, 2);
        product.addReview(Review.create("Matias", 5, "Excelente instrumento"));

        assertEquals(1, product.getReviews().size());
        assertThrows(UnsupportedOperationException.class, () -> product.getReviews().clear());
        assertThrows(UnsupportedOperationException.class, () -> product.getTags().add("hack"));
        assertThrows(UnsupportedOperationException.class, () -> product.getSpecifications().put("k", "v"));
    }
}
