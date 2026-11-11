package cl.duoc.sonidovivo.application.service;

import cl.duoc.sonidovivo.application.port.in.ProductUseCase;
import cl.duoc.sonidovivo.domain.exception.DuplicateProductCodeException;
import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pure Java application service implementing product catalog and inventory use cases.
 */
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    public ProductService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = Objects.requireNonNull(
                productRepositoryPort,
                "ProductRepositoryPort must not be null"
        );
    }

    @Override
    public Product createProduct(CreateProductCommand command) {
        requireNonNull(command, "CreateProductCommand");
        requireNonNull(command.code(), "Product code");

        if (productRepositoryPort.existsByCode(command.code())) {
            throw new DuplicateProductCodeException(
                    "Product code already exists: " + command.code().value()
            );
        }

        Product product = Product.create(
                command.code(),
                command.name(),
                command.category(),
                command.price(),
                command.stock(),
                command.specifications(),
                command.tags()
        );
        return productRepositoryPort.save(product);
    }

    @Override
    public Product getProductByCode(ProductCode code) {
        return findExistingProduct(code);
    }

    @Override
    public List<Product> listProductsPaged(int page, int size, Category category, ProductStatus status) {
        if (page < 0) {
            throw new InvalidDomainDataException("Page index must not be negative: " + page);
        }
        if (size <= 0) {
            throw new InvalidDomainDataException("Page size must be positive: " + size);
        }
        return productRepositoryPort.findAllPaged(page, size, category, status);
    }

    @Override
    public List<Product> searchProducts(
            String query,
            Category category,
            Map<String, Object> specificationFilters
    ) {
        String normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
        Map<String, Object> safeFilters = specificationFilters == null
                ? Map.of()
                : specificationFilters;
        return productRepositoryPort.search(normalizedQuery, category, safeFilters);
    }

    @Override
    public Product updateProduct(ProductCode code, UpdateProductCommand command) {
        requireNonNull(command, "UpdateProductCommand");
        Product existing = findExistingProduct(code);

        existing.updateDetails(
                command.name(),
                command.category(),
                command.price(),
                command.stock(),
                command.specifications(),
                command.tags()
        );
        return productRepositoryPort.save(existing);
    }

    @Override
    public Product adjustStockAtomic(ProductCode code, int delta, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidDomainDataException("Stock adjustment reason must not be blank");
        }
        if (delta == 0) {
            throw new InvalidDomainDataException("Stock adjustment delta must be non-zero");
        }

        Product product = findExistingProduct(code);
        ProductStatus previousStatus = product.getStatus();

        // Enforce domain invariants (discontinued check, insufficient stock, and status transition)
        product.adjustStock(delta);

        boolean updated = productRepositoryPort.adjustStockAtomic(code, delta);
        if (!updated) {
            if (delta < 0) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + code.value()
                );
            }
            throw new InvalidDomainDataException(
                    "Unable to adjust stock atomically for product: " + code.value()
            );
        }

        if (product.getStatus() != previousStatus) {
            productRepositoryPort.updateStatus(code, product.getStatus());
        }
        return product;
    }

    @Override
    public Product changeStatus(ProductCode code, ProductStatus status) {
        requireNonNull(status, "Product status");
        Product product = findExistingProduct(code);
        product.changeStatus(status);
        productRepositoryPort.updateStatus(code, status);
        return product;
    }

    @Override
    public Product addReview(ProductCode code, Review review) {
        requireNonNull(review, "Review");
        Product product = findExistingProduct(code);
        product.addReview(review);
        productRepositoryPort.addReview(code, review);
        return product;
    }

    private Product findExistingProduct(ProductCode code) {
        requireNonNull(code, "Product code");
        return productRepositoryPort.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with code: " + code.value()
                ));
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidDomainDataException(fieldName + " must not be null");
        }
        return value;
    }
}
