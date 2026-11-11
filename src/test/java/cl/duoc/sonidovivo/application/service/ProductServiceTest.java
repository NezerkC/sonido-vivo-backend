package cl.duoc.sonidovivo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.sonidovivo.application.port.in.ProductUseCase.CreateProductCommand;
import cl.duoc.sonidovivo.application.port.in.ProductUseCase.UpdateProductCommand;
import cl.duoc.sonidovivo.domain.exception.DuplicateProductCodeException;
import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import cl.duoc.sonidovivo.domain.model.StockQuantity;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepositoryPort);
    }

    private Product sampleProduct(int priceClp, int stock) {
        return Product.create(
                ProductCode.of("G-FND-STRAT-01"),
                "Fender Stratocaster Player",
                Category.CUERDAS,
                MoneyClp.of(priceClp),
                StockQuantity.of(stock),
                Map.of("tipo", "guitarra electrica", "cuerdas", 6),
                List.of("fender", "electrica")
        );
    }

    @Test
    @DisplayName("createProduct persists new product when SKU code is unique")
    void createProductHappyPath() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        CreateProductCommand command = new CreateProductCommand(
                code,
                "Fender Stratocaster Player",
                Category.CUERDAS,
                MoneyClp.of(899_990),
                StockQuantity.of(5),
                Map.of("tipo", "guitarra electrica"),
                List.of("fender", "electrica")
        );

        when(productRepositoryPort.existsByCode(code)).thenReturn(false);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.createProduct(command);

        assertEquals(code, created.getCode());
        assertEquals(ProductStatus.ACTIVO, created.getStatus());
        assertEquals(5, created.getStock().value());
        verify(productRepositoryPort).existsByCode(code);
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct rejects duplicate SKU code with DuplicateProductCodeException")
    void createProductRejectsDuplicateCode() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        CreateProductCommand command = new CreateProductCommand(
                code,
                "Fender Stratocaster Player",
                Category.CUERDAS,
                MoneyClp.of(899_990),
                StockQuantity.of(5),
                Map.of(),
                List.of()
        );

        when(productRepositoryPort.existsByCode(code)).thenReturn(true);

        assertThrows(DuplicateProductCodeException.class, () -> productService.createProduct(command));
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("getProductByCode returns product or throws ProductNotFoundException")
    void getProductByCodeBehavior() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product expected = sampleProduct(899_990, 4);
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(expected));

        Product found = productService.getProductByCode(code);
        assertEquals(expected, found);

        ProductCode missing = ProductCode.of("AU-SHU-SM58-01");
        when(productRepositoryPort.findByCode(missing)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductByCode(missing));
    }

    @Test
    @DisplayName("listProductsPaged validates pagination arguments and delegates filters")
    void listProductsPagedValidationAndDelegation() {
        Product expected = sampleProduct(500_000, 3);
        when(productRepositoryPort.findAllPaged(0, 10, Category.CUERDAS, ProductStatus.ACTIVO))
                .thenReturn(List.of(expected));

        List<Product> result = productService.listProductsPaged(0, 10, Category.CUERDAS, ProductStatus.ACTIVO);

        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));
        assertThrows(InvalidDomainDataException.class,
                () -> productService.listProductsPaged(-1, 10, null, null));
        assertThrows(InvalidDomainDataException.class,
                () -> productService.listProductsPaged(0, 0, null, null));
    }

    @Test
    @DisplayName("searchProducts normalizes blank query and null specification filters")
    void searchProductsNormalizesInputs() {
        Product expected = sampleProduct(500_000, 3);
        when(productRepositoryPort.search(null, Category.CUERDAS, Map.of()))
                .thenReturn(List.of(expected));

        List<Product> result = productService.searchProducts("   ", Category.CUERDAS, null);

        assertEquals(1, result.size());
        verify(productRepositoryPort).search(null, Category.CUERDAS, Map.of());
    }

    @Test
    @DisplayName("updateProduct updates existing product details and persists changes")
    void updateProductHappyPath() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 5);
        UpdateProductCommand command = new UpdateProductCommand(
                "Fender Stratocaster American Pro II",
                Category.CUERDAS,
                MoneyClp.of(1_499_990),
                StockQuantity.of(2),
                Map.of("tipo", "guitarra electrica", "origen", "USA"),
                List.of("fender", "usa")
        );

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.save(existing)).thenReturn(existing);

        Product updated = productService.updateProduct(code, command);

        assertEquals("Fender Stratocaster American Pro II", updated.getName());
        assertEquals(1_499_990, updated.getPrice().amount());
        assertEquals(2, updated.getStock().value());
        verify(productRepositoryPort).save(existing);
    }

    @Test
    @DisplayName("adjustStockAtomic decrements stock to zero, marks AGOTADO, and syncs status")
    void adjustStockAtomicDecrementToOutOfStock() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 3);

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.adjustStockAtomic(code, -3)).thenReturn(true);

        Product adjusted = productService.adjustStockAtomic(code, -3, "VENTA_DIRECTA_MOSTRADOR");

        assertEquals(0, adjusted.getStock().value());
        assertEquals(ProductStatus.AGOTADO, adjusted.getStatus());
        verify(productRepositoryPort).adjustStockAtomic(code, -3);
        verify(productRepositoryPort).updateStatus(code, ProductStatus.AGOTADO);
    }

    @Test
    @DisplayName("adjustStockAtomic increments out-of-stock product and reactivates ACTIVO status")
    void adjustStockAtomicReplenishReactivatesProduct() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 0);
        assertEquals(ProductStatus.AGOTADO, existing.getStatus());

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.adjustStockAtomic(code, 5)).thenReturn(true);

        Product adjusted = productService.adjustStockAtomic(code, 5, "INGRESO_PROVEEDOR");

        assertEquals(5, adjusted.getStock().value());
        assertEquals(ProductStatus.ACTIVO, adjusted.getStatus());
        verify(productRepositoryPort).adjustStockAtomic(code, 5);
        verify(productRepositoryPort).updateStatus(code, ProductStatus.ACTIVO);
    }

    @Test
    @DisplayName("adjustStockAtomic throws InsufficientStockException when requested decrement exceeds stock")
    void adjustStockAtomicThrowsInsufficientStockFromDomain() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 2);

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));

        assertThrows(
                InsufficientStockException.class,
                () -> productService.adjustStockAtomic(code, -5, "VENTA_DIRECTA_MOSTRADOR")
        );
        verify(productRepositoryPort, never()).adjustStockAtomic(any(), anyInt());
    }

    @Test
    @DisplayName("adjustStockAtomic throws InsufficientStockException when concurrent atomic update returns false")
    void adjustStockAtomicThrowsInsufficientStockOnConcurrentFailure() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 5);

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.adjustStockAtomic(code, -4)).thenReturn(false);

        assertThrows(
                InsufficientStockException.class,
                () -> productService.adjustStockAtomic(code, -4, "VENTA_DIRECTA_MOSTRADOR")
        );
        verify(productRepositoryPort, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("adjustStockAtomic rejects zero delta, blank reason, and discontinued products")
    void adjustStockAtomicRejectsInvalidInputs() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");

        assertThrows(InvalidDomainDataException.class,
                () -> productService.adjustStockAtomic(code, 0, "VENTA_DIRECTA_MOSTRADOR"));
        assertThrows(InvalidDomainDataException.class,
                () -> productService.adjustStockAtomic(code, -1, "   "));
        verifyNoInteractions(productRepositoryPort);

        Product discontinued = sampleProduct(899_990, 5);
        discontinued.changeStatus(ProductStatus.DESCONTINUADO);
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(discontinued));

        assertThrows(InvalidDomainDataException.class,
                () -> productService.adjustStockAtomic(code, -1, "VENTA_DIRECTA_MOSTRADOR"));
        verify(productRepositoryPort, never()).adjustStockAtomic(any(), anyInt());
    }

    @Test
    @DisplayName("changeStatus updates lifecycle status and sets discontinuedAt for soft delete")
    void changeStatusHappyPath() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 5);
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));

        Product updated = productService.changeStatus(code, ProductStatus.DESCONTINUADO);

        assertEquals(ProductStatus.DESCONTINUADO, updated.getStatus());
        assertNotNull(updated.getDiscontinuedAt());
        verify(productRepositoryPort).updateStatus(code, ProductStatus.DESCONTINUADO);
    }

    @Test
    @DisplayName("addReview appends review to domain aggregate and delegates persistence")
    void addReviewHappyPath() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        Product existing = sampleProduct(899_990, 5);
        Review review = Review.create("Camila", 5, "Sonido impecable");
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(existing));

        Product updated = productService.addReview(code, review);

        assertEquals(1, updated.getReviews().size());
        assertEquals(review, updated.getReviews().get(0));
        verify(productRepositoryPort).addReview(code, review);
    }
}
