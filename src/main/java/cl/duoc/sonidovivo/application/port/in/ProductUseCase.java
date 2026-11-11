package cl.duoc.sonidovivo.application.port.in;

import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import cl.duoc.sonidovivo.domain.model.StockQuantity;
import java.util.List;
import java.util.Map;

/**
 * Inbound port defining product catalog and inventory use cases.
 */
public interface ProductUseCase {

    Product createProduct(CreateProductCommand command);

    Product getProductByCode(ProductCode code);

    List<Product> listProductsPaged(int page, int size, Category category, ProductStatus status);

    List<Product> searchProducts(String query, Category category, Map<String, Object> specificationFilters);

    Product updateProduct(ProductCode code, UpdateProductCommand command);

    Product adjustStockAtomic(ProductCode code, int delta, String reason);

    Product changeStatus(ProductCode code, ProductStatus status);

    Product addReview(ProductCode code, Review review);

    /**
     * Command carrying the attributes required to register a new catalog product.
     */
    record CreateProductCommand(
            ProductCode code,
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            Map<String, Object> specifications,
            List<String> tags
    ) {}

    /**
     * Command carrying updated product attributes for an existing catalog item.
     */
    record UpdateProductCommand(
            String name,
            Category category,
            MoneyClp price,
            StockQuantity stock,
            Map<String, Object> specifications,
            List<String> tags
    ) {}
}
