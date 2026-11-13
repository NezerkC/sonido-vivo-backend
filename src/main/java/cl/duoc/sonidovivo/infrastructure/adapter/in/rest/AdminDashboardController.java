package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.DashboardUseCase;
import cl.duoc.sonidovivo.application.port.in.OracleIntegrationUseCase;
import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.BatchPriceAdjustmentResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.DashboardMetricsResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.PriceAdjustmentDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.SaleRequestDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller serving dashboard KPIs, batch pricing operations, and Oracle PL/SQL endpoints.
 *
 * <p>Responsable asignado: <strong>Dylan Sandoval (@dysandoval)</strong></p>
 * <p>Documentación y contrato: {@code docs/contracts/CONTRACT_FULLSTACK_REST.md}</p>
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final DashboardUseCase dashboardUseCase;
    private final OracleIntegrationUseCase oracleIntegrationUseCase;

    public AdminDashboardController(
            DashboardUseCase dashboardUseCase,
            OracleIntegrationUseCase oracleIntegrationUseCase
    ) {
        this.dashboardUseCase = dashboardUseCase;
        this.oracleIntegrationUseCase = oracleIntegrationUseCase;
    }

    @GetMapping("/dashboard/metrics")
    public ResponseEntity<DashboardMetricsResponseDTO> getMetrics() {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar dashboardUseCase.getInventoryMetrics().
        // 2. Mapear a DashboardMetricsResponseDTO.
        // 3. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("getMetrics pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping("/operations/price-adjustment")
    public ResponseEntity<BatchPriceAdjustmentResponseDTO> applyPriceAdjustment(
            @Valid @RequestBody PriceAdjustmentDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Parsear categoría opcional y construir BatchPriceAdjustmentCommand.
        // 2. Invocar dashboardUseCase.applyBatchPriceAdjustment(command).
        // 3. Retornar ResponseEntity.ok(BatchPriceAdjustmentResponseDTO.fromResult(result)).
        throw new UnsupportedOperationException("applyPriceAdjustment pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping("/operations/sales")
    public ResponseEntity<SaleResult> registerSale(@Valid @RequestBody SaleRequestDTO request) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Construir SaleCommand (customerId, ProductCode, quantity, MoneyClp).
        // 2. Invocar oracleIntegrationUseCase.registerSale(command).
        // 3. Retornar ResponseEntity.status(HttpStatus.CREATED).body(result).
        throw new UnsupportedOperationException("registerSale pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping("/operations/liquidations")
    public ResponseEntity<LiquidationResult> runCustomerLiquidation(
            @RequestParam(required = false) LocalDate processDate
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Normalizar fecha efectiva (LocalDate.now() si es nula).
        // 2. Invocar oracleIntegrationUseCase.runCustomerLiquidation(effectiveDate).
        // 3. Retornar ResponseEntity.ok(resultado).
        throw new UnsupportedOperationException("runCustomerLiquidation pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @GetMapping("/operations/discount")
    public ResponseEntity<Map<String, Long>> calculateDiscount(
            @RequestParam String customerId,
            @RequestParam long grossAmountClp
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar oracleIntegrationUseCase.calculateDiscount(customerId, grossAmountClp).
        // 2. Retornar ResponseEntity.ok(Map.of("discountClp", discountClp)).
        throw new UnsupportedOperationException("calculateDiscount pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }
}
