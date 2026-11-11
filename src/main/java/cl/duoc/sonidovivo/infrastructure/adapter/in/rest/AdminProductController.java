package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.ProductUseCase;
import cl.duoc.sonidovivo.application.port.in.ProductUseCase.CreateProductCommand;
import cl.duoc.sonidovivo.application.port.in.ProductUseCase.UpdateProductCommand;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import cl.duoc.sonidovivo.domain.model.StockQuantity;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductCreateRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductStatusUpdateDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductUpdateRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ReviewCreateDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.StockAdjustmentDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller managing administrative product catalog, stock, status, and review operations.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private static final Set<String> RESERVED_SEARCH_PARAMS = Set.of("q", "query", "categoria");

    private final ProductUseCase productUseCase;

    public AdminProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado
    ) {
        Category category = parseOptionalCategory(categoria);
        ProductStatus status = parseOptionalStatus(estado);
        List<ProductResponseDTO> products = productUseCase.listProductsPaged(page, size, category, status)
                .stream()
                .map(ProductResponseDTO::fromDomain)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoria,
            @RequestParam Map<String, String> allQueryParams
    ) {
        String effectiveQuery = (q != null && !q.isBlank()) ? q : query;
        Category category = parseOptionalCategory(categoria);

        Map<String, Object> specFilters = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : allQueryParams.entrySet()) {
            if (!RESERVED_SEARCH_PARAMS.contains(entry.getKey())
                    && entry.getValue() != null
                    && !entry.getValue().isBlank()) {
                specFilters.put(entry.getKey(), entry.getValue().trim());
            }
        }

        List<ProductResponseDTO> products = productUseCase.searchProducts(effectiveQuery, category, specFilters)
                .stream()
                .map(ProductResponseDTO::fromDomain)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable String codigo) {
        Product product = productUseCase.getProductByCode(ProductCode.of(codigo));
        return ResponseEntity.ok(ProductResponseDTO.fromDomain(product));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateRequestDTO request) {
        CreateProductCommand command = new CreateProductCommand(
                ProductCode.of(request.codigo()),
                request.nombre(),
                Category.fromString(request.categoria()),
                MoneyClp.of(request.precioClp()),
                StockQuantity.of(request.stockDisponible()),
                request.especificaciones(),
                request.tags()
        );
        Product created = productUseCase.createProduct(command);
        URI location = URI.create("/api/v1/admin/products/" + created.getCode().value());
        return ResponseEntity.created(location).body(ProductResponseDTO.fromDomain(created));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String codigo,
            @Valid @RequestBody ProductUpdateRequestDTO request
    ) {
        UpdateProductCommand command = new UpdateProductCommand(
                request.nombre(),
                Category.fromString(request.categoria()),
                MoneyClp.of(request.precioClp()),
                StockQuantity.of(request.stockDisponible()),
                request.especificaciones(),
                request.tags()
        );
        Product updated = productUseCase.updateProduct(ProductCode.of(codigo), command);
        return ResponseEntity.ok(ProductResponseDTO.fromDomain(updated));
    }

    @PatchMapping("/{codigo}/stock")
    public ResponseEntity<ProductResponseDTO> adjustStock(
            @PathVariable String codigo,
            @Valid @RequestBody StockAdjustmentDTO request
    ) {
        Product updated = productUseCase.adjustStockAtomic(
                ProductCode.of(codigo),
                request.delta(),
                request.motivo()
        );
        return ResponseEntity.ok(ProductResponseDTO.fromDomain(updated));
    }

    @PatchMapping("/{codigo}/status")
    public ResponseEntity<ProductResponseDTO> changeStatus(
            @PathVariable String codigo,
            @Valid @RequestBody ProductStatusUpdateDTO request
    ) {
        Product updated = productUseCase.changeStatus(
                ProductCode.of(codigo),
                ProductStatus.fromString(request.estado())
        );
        return ResponseEntity.ok(ProductResponseDTO.fromDomain(updated));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<ProductResponseDTO> softDeleteProduct(@PathVariable String codigo) {
        Product discontinued = productUseCase.changeStatus(
                ProductCode.of(codigo),
                ProductStatus.DESCONTINUADO
        );
        return ResponseEntity.ok(ProductResponseDTO.fromDomain(discontinued));
    }

    @PostMapping("/{codigo}/reviews")
    public ResponseEntity<ProductResponseDTO> addReview(
            @PathVariable String codigo,
            @Valid @RequestBody ReviewCreateDTO request
    ) {
        Review review = Review.create(request.cliente(), request.calificacion(), request.comentario());
        Product updated = productUseCase.addReview(ProductCode.of(codigo), review);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponseDTO.fromDomain(updated));
    }

    private static Category parseOptionalCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Category.fromString(value);
    }

    private static ProductStatus parseOptionalStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ProductStatus.fromString(value);
    }
}
