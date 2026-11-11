package cl.duoc.sonidovivo.domain.port.out;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Outbound persistence port for the product catalog.
 */
public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findByCode(ProductCode code);

    boolean existsByCode(ProductCode code);

    /**
     * Lists products with pagination. {@code category} and {@code status} are optional filters (nullable).
     */
    List<Product> findAllPaged(int page, int size, Category category, ProductStatus status);

    /**
     * Searches products by optional text query, category, and dynamic specification attributes.
     */
    List<Product> search(String query, Category category, Map<String, Object> specificationFilters);

    /**
     * Atomically applies a positive or negative stock delta.
     * For negative deltas, fails (returns {@code false}) if resulting stock would drop below 0.
     */
    boolean adjustStockAtomic(ProductCode code, int delta);

    default boolean decrementStockAtomic(ProductCode code, int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainDataException("Decrement quantity must be positive: " + quantity);
        }
        return adjustStockAtomic(code, -quantity);
    }

    void addReview(ProductCode code, Review review);

    void updateStatus(ProductCode code, ProductStatus status);

    /**
     * Applies a percentage price adjustment to active products.
     * When {@code category} is {@code null}, all categories are updated.
     */
    int applyBatchPriceAdjustment(double percentage, Category category);
}
