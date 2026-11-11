package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Product aggregate root encapsulating catalog data, dynamic specifications,
 * stock invariants, pricing margin adjustments, lifecycle status, and reviews.
 */
public final class Product {

    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 150;

    private final String id;
    private final ProductCode code;
    private String name;
    private Category category;
    private MoneyClp price;
    private StockQuantity stock;
    private ProductStatus status;
    private Map<String, Object> specifications;
    private List<String> tags;
    private final List<Review> reviews;
    private Instant lastSaleAt;
    private Instant discontinuedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(
            String id,
            ProductCode code,
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            ProductStatus status,
            Map<String, Object> specifications,
            List<String> tags,
            List<Review> reviews,
            Instant lastSaleAt,
            Instant discontinuedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.code = requireNonNull(code, "Product code");
        this.name = normalizeAndValidateName(name);
        this.category = requireNonNull(category, "Category");
        this.price = requireNonNull(price, "Price");
        this.stock = requireNonNull(stock, "Stock");
        this.status = requireNonNull(status, "Product status");
        this.specifications = copySpecifications(specifications);
        this.tags = copyDistinctTags(tags);
        this.reviews = copyReviews(reviews);
        this.lastSaleAt = lastSaleAt;
        this.discontinuedAt = discontinuedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new catalog product with initial lifecycle status derived from initial stock.
     */
    public static Product create(
            ProductCode code,
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            Map<String, Object> specifications,
            List<String> tags
    ) {
        Instant now = Instant.now();
        StockQuantity nonNullStock = requireNonNull(stock, "Stock");
        ProductStatus initialStatus = nonNullStock.isOutOfStock()
                ? ProductStatus.AGOTADO
                : ProductStatus.ACTIVO;
        return new Product(
                null,
                code,
                name,
                category,
                price,
                nonNullStock,
                initialStatus,
                specifications,
                tags,
                List.of(),
                null,
                null,
                now,
                now
        );
    }

    /**
     * Reconstitutes an existing product from persistence storage.
     */
    public static Product reconstitute(
            String id,
            ProductCode code,
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            ProductStatus status,
            Map<String, Object> specifications,
            List<String> tags,
            List<Review> reviews,
            Instant lastSaleAt,
            Instant discontinuedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Product(
                id,
                code,
                name,
                category,
                price,
                stock,
                status,
                specifications,
                tags,
                reviews,
                lastSaleAt,
                discontinuedAt,
                createdAt,
                updatedAt
        );
    }

    public void decrementStock(int quantity) {
        if (status == ProductStatus.DESCONTINUADO) {
            throw new InvalidDomainDataException(
                    "Cannot decrement stock for discontinued product: " + code.value()
            );
        }
        this.stock = this.stock.decrement(quantity);
        Instant now = Instant.now();
        this.lastSaleAt = now;
        this.updatedAt = now;
        if (this.stock.isOutOfStock()) {
            this.status = ProductStatus.AGOTADO;
        }
    }

    public void incrementStock(int quantity) {
        if (status == ProductStatus.DESCONTINUADO) {
            throw new InvalidDomainDataException(
                    "Cannot increment stock for discontinued product: " + code.value()
            );
        }
        this.stock = this.stock.increment(quantity);
        this.updatedAt = Instant.now();
        if (this.status == ProductStatus.AGOTADO && !this.stock.isOutOfStock()) {
            this.status = ProductStatus.ACTIVO;
        }
    }

    public void adjustStock(int delta) {
        if (delta == 0) {
            throw new InvalidDomainDataException("Stock adjustment delta must be non-zero");
        }
        if (delta < 0) {
            long requestedDecrease = -(long) delta;
            if (requestedDecrease > Integer.MAX_VALUE) {
                throw new InvalidDomainDataException("Stock decrement quantity out of range");
            }
            decrementStock((int) requestedDecrease);
            return;
        }
        incrementStock(delta);
    }

    public void adjustPrice(double percentage) {
        this.price = this.price.adjustByPercentage(percentage);
        this.updatedAt = Instant.now();
    }

    public void changeStatus(ProductStatus newStatus) {
        ProductStatus validated = requireNonNull(newStatus, "Product status");
        Instant now = Instant.now();
        this.status = validated;
        this.discontinuedAt = (validated == ProductStatus.DESCONTINUADO) ? now : null;
        this.updatedAt = now;
    }

    public void addReview(Review review) {
        this.reviews.add(requireNonNull(review, "Review"));
        this.updatedAt = Instant.now();
    }

    public void updateDetails(
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            Map<String, Object> specifications,
            List<String> tags
    ) {
        this.name = normalizeAndValidateName(name);
        this.category = requireNonNull(category, "Category");
        this.price = requireNonNull(price, "Price");
        this.stock = requireNonNull(stock, "Stock");
        this.specifications = copySpecifications(specifications);
        this.tags = copyDistinctTags(tags);
        if (this.status != ProductStatus.DESCONTINUADO) {
            this.status = this.stock.isOutOfStock() ? ProductStatus.AGOTADO : ProductStatus.ACTIVO;
        }
        this.updatedAt = Instant.now();
    }

    public long calculateInventoryValueClp() {
        return price.multiplyBy(stock.value());
    }

    public String getId() {
        return id;
    }

    public ProductCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public MoneyClp getPrice() {
        return price;
    }

    public StockQuantity getStock() {
        return stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Map<String, Object> getSpecifications() {
        return specifications;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public Instant getLastSaleAt() {
        return lastSaleAt;
    }

    public Instant getDiscontinuedAt() {
        return discontinuedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product other)) {
            return false;
        }
        return Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidDomainDataException(fieldName + " must not be null");
        }
        return value;
    }

    private static String normalizeAndValidateName(String name) {
        if (name == null) {
            throw new InvalidDomainDataException("Product name must not be null");
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainDataException(
                    "Product name length must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH
            );
        }
        return trimmed;
    }

    private static Map<String, Object> copySpecifications(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new InvalidDomainDataException("Specification keys must not be null or blank");
            }
            copy.put(entry.getKey().trim(), entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<String> copyDistinctTags(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }

    private static List<Review> copyReviews(List<Review> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        for (Review review : source) {
            if (review == null) {
                throw new InvalidDomainDataException("Product reviews must not contain null entries");
            }
        }
        return new ArrayList<>(source);
    }
}
