package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.DashboardUseCase;
import cl.duoc.sonidovivo.application.port.in.DashboardUseCase.BatchPriceAdjustmentCommand;
import cl.duoc.sonidovivo.application.port.in.DashboardUseCase.BatchPriceAdjustmentResult;
import cl.duoc.sonidovivo.application.port.in.OracleIntegrationUseCase;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.DashboardMetrics;
import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.BatchPriceAdjustmentResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.DashboardMetricsResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.PriceAdjustmentDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.SaleRequestDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller serving dashboard KPIs, batch pricing operations, and Oracle PL/SQL endpoints.
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
        DashboardMetrics metrics = dashboardUseCase.getInventoryMetrics();
        return ResponseEntity.ok(DashboardMetricsResponseDTO.fromDomain(metrics));
    }

    @PostMapping("/operations/price-adjustment")
    public ResponseEntity<BatchPriceAdjustmentResponseDTO> applyPriceAdjustment(
            @Valid @RequestBody PriceAdjustmentDTO request
    ) {
        Category targetCategory = (request.categoria() == null || request.categoria().isBlank())
                ? null
                : Category.fromString(request.categoria());

        BatchPriceAdjustmentCommand command = new BatchPriceAdjustmentCommand(
                request.porcentaje(),
                targetCategory,
                request.motivo()
        );
        BatchPriceAdjustmentResult result = dashboardUseCase.applyBatchPriceAdjustment(command);
        return ResponseEntity.ok(BatchPriceAdjustmentResponseDTO.fromResult(result));
    }

    @PostMapping("/operations/sales")
    public ResponseEntity<SaleResult> registerSale(@Valid @RequestBody SaleRequestDTO request) {
        SaleCommand command = new SaleCommand(
                request.customerId(),
                ProductCode.of(request.productCode()),
                request.quantity(),
                MoneyClp.of(request.unitPriceClp())
        );
        SaleResult result = oracleIntegrationUseCase.registerSale(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/operations/liquidations")
    public ResponseEntity<LiquidationResult> runCustomerLiquidation(
            @RequestParam(required = false) LocalDate processDate
    ) {
        LocalDate effectiveDate = processDate != null ? processDate : LocalDate.now();
        return ResponseEntity.ok(oracleIntegrationUseCase.runCustomerLiquidation(effectiveDate));
    }

    @GetMapping("/operations/discount")
    public ResponseEntity<Map<String, Long>> calculateDiscount(
            @RequestParam String customerId,
            @RequestParam long grossAmountClp
    ) {
        long discountClp = oracleIntegrationUseCase.calculateDiscount(customerId, grossAmountClp);
        return ResponseEntity.ok(Map.of("discountClp", discountClp));
    }
}
