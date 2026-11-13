package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.ProductUseCase;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductCreateRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductStatusUpdateDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ProductUpdateRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ReviewCreateDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.StockAdjustmentDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
 *
 * <p>Responsable asignado: <strong>Dylan Sandoval (@dysandoval)</strong></p>
 * <p>Documentación y contrato: {@code docs/contracts/CONTRACT_FULLSTACK_REST.md}</p>
 */
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

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
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Parsear categoría y estado opcionales.
        // 2. Invocar productUseCase.listProductsPaged(page, size, category, status).
        // 3. Mapear a ProductResponseDTO y retornar ResponseEntity.ok(lista).
        throw new UnsupportedOperationException("listProducts pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoria,
            @RequestParam Map<String, String> allQueryParams
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Extraer filtros de especificaciones dinámicas (marca, modelo, etc.).
        // 2. Invocar productUseCase.searchProducts(query, category, specFilters).
        // 3. Mapear a ProductResponseDTO y retornar ResponseEntity.ok(lista).
        throw new UnsupportedOperationException("searchProducts pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable String codigo) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar productUseCase.getProductByCode(ProductCode.of(codigo)).
        // 2. Retornar ResponseEntity.ok(ProductResponseDTO.fromDomain(product)).
        throw new UnsupportedOperationException("getProduct pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateRequestDTO request) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Mapear request a CreateProductCommand.
        // 2. Invocar productUseCase.createProduct(command).
        // 3. Retornar ResponseEntity.created(...).body(dto).
        throw new UnsupportedOperationException("createProduct pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable String codigo,
            @Valid @RequestBody ProductUpdateRequestDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Mapear request a UpdateProductCommand.
        // 2. Invocar productUseCase.updateProduct(codigo, command).
        // 3. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("updateProduct pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PatchMapping("/{codigo}/status")
    public ResponseEntity<ProductResponseDTO> updateStatus(
            @PathVariable String codigo,
            @Valid @RequestBody ProductStatusUpdateDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar productUseCase.updateStatus(ProductCode.of(codigo), ProductStatus.valueOf(request.estado().toUpperCase())).
        // 2. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("updateStatus pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping("/{codigo}/stock")
    public ResponseEntity<ProductResponseDTO> adjustStock(
            @PathVariable String codigo,
            @Valid @RequestBody StockAdjustmentDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar productUseCase.adjustStock(ProductCode.of(codigo), request.delta(), request.motivo()).
        // 2. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("adjustStock pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping("/{codigo}/reviews")
    public ResponseEntity<ProductResponseDTO> addReview(
            @PathVariable String codigo,
            @Valid @RequestBody ReviewCreateDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Construir Review de dominio e invocar productUseCase.addReview(ProductCode.of(codigo), review).
        // 2. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("addReview pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String codigo) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar productUseCase.deleteProduct(ProductCode.of(codigo)).
        // 2. Retornar ResponseEntity.noContent().build().
        throw new UnsupportedOperationException("deleteProduct pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }
}
