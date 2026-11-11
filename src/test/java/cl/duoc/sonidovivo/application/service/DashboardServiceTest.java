package cl.duoc.sonidovivo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.sonidovivo.application.port.in.DashboardUseCase.BatchPriceAdjustmentCommand;
import cl.duoc.sonidovivo.application.port.in.DashboardUseCase.BatchPriceAdjustmentResult;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.InvalidPriceMarginException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.CategoryMetric;
import cl.duoc.sonidovivo.domain.model.DashboardMetrics;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.port.out.DashboardMetricsPort;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardMetricsPort dashboardMetricsPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(dashboardMetricsPort, productRepositoryPort);
    }

    @Test
    @DisplayName("getInventoryMetrics returns aggregated dashboard metrics from outbound port")
    void getInventoryMetricsHappyPath() {
        DashboardMetrics expected = DashboardMetrics.fromCategories(List.of(
                new CategoryMetric("Cuerdas", 12, 6_000_000L, 500_000.0),
                new CategoryMetric("Audio Pro", 5, 2_500_000L, 500_000.0)
        ));
        when(dashboardMetricsPort.getInventoryMetrics()).thenReturn(expected);

        DashboardMetrics result = dashboardService.getInventoryMetrics();

        assertEquals(8_500_000L, result.totalWarehouseValueClp());
        assertEquals(17, result.totalUnits());
        assertEquals(2, result.activeCategories());
        verify(dashboardMetricsPort).getInventoryMetrics();
    }

    @Test
    @DisplayName("applyBatchPriceAdjustment delegates category filter and global adjustment cleanly")
    void applyBatchPriceAdjustmentHappyPath() {
        BatchPriceAdjustmentCommand categoryCmd = new BatchPriceAdjustmentCommand(
                15.5,
                Category.CUERDAS,
                "Reajuste IPC proveedor cuerdas"
        );
        when(productRepositoryPort.applyBatchPriceAdjustment(15.5, Category.CUERDAS)).thenReturn(8);

        BatchPriceAdjustmentResult categoryResult = dashboardService.applyBatchPriceAdjustment(categoryCmd);

        assertEquals(8, categoryResult.updatedCount());
        assertEquals(15.5, categoryResult.appliedPercentage());
        assertEquals(Category.CUERDAS, categoryResult.targetCategory());
        assertEquals("Reajuste IPC proveedor cuerdas", categoryResult.reason());
        verify(productRepositoryPort).applyBatchPriceAdjustment(15.5, Category.CUERDAS);

        BatchPriceAdjustmentCommand globalCmd = new BatchPriceAdjustmentCommand(
                -10.0,
                null,
                "Liquidacion temporada"
        );
        when(productRepositoryPort.applyBatchPriceAdjustment(-10.0, null)).thenReturn(20);

        BatchPriceAdjustmentResult globalResult = dashboardService.applyBatchPriceAdjustment(globalCmd);

        assertEquals(20, globalResult.updatedCount());
        assertEquals(-10.0, globalResult.appliedPercentage());
        assertNull(globalResult.targetCategory());
        verify(productRepositoryPort).applyBatchPriceAdjustment(-10.0, null);
    }

    @Test
    @DisplayName("applyBatchPriceAdjustment accepts exact boundary margins -50% and +100%")
    void applyBatchPriceAdjustmentAcceptsBoundaryMargins() {
        when(productRepositoryPort.applyBatchPriceAdjustment(-50.0, null)).thenReturn(4);
        when(productRepositoryPort.applyBatchPriceAdjustment(100.0, Category.AUDIO_PRO)).thenReturn(2);

        BatchPriceAdjustmentResult minResult = dashboardService.applyBatchPriceAdjustment(
                new BatchPriceAdjustmentCommand(-50.0, null, "Descuento maximo permitido")
        );
        BatchPriceAdjustmentResult maxResult = dashboardService.applyBatchPriceAdjustment(
                new BatchPriceAdjustmentCommand(100.0, Category.AUDIO_PRO, "Incremento maximo permitido")
        );

        assertEquals(4, minResult.updatedCount());
        assertEquals(2, maxResult.updatedCount());
    }

    @Test
    @DisplayName("applyBatchPriceAdjustment rejects invalid margins and blank reasons without touching repository")
    void applyBatchPriceAdjustmentRejectsInvalidMarginsAndReason() {
        BatchPriceAdjustmentCommand belowMin = new BatchPriceAdjustmentCommand(-50.01, null, "Exceso descuento");
        BatchPriceAdjustmentCommand aboveMax = new BatchPriceAdjustmentCommand(100.01, null, "Exceso alza");
        BatchPriceAdjustmentCommand nanMargin = new BatchPriceAdjustmentCommand(Double.NaN, null, "NaN");
        BatchPriceAdjustmentCommand infMargin = new BatchPriceAdjustmentCommand(Double.POSITIVE_INFINITY, null, "Inf");
        BatchPriceAdjustmentCommand blankReason = new BatchPriceAdjustmentCommand(10.0, null, "   ");

        assertThrows(InvalidPriceMarginException.class, () -> dashboardService.applyBatchPriceAdjustment(belowMin));
        assertThrows(InvalidPriceMarginException.class, () -> dashboardService.applyBatchPriceAdjustment(aboveMax));
        assertThrows(InvalidPriceMarginException.class, () -> dashboardService.applyBatchPriceAdjustment(nanMargin));
        assertThrows(InvalidPriceMarginException.class, () -> dashboardService.applyBatchPriceAdjustment(infMargin));
        assertThrows(InvalidDomainDataException.class, () -> dashboardService.applyBatchPriceAdjustment(blankReason));
        assertThrows(InvalidDomainDataException.class, () -> dashboardService.applyBatchPriceAdjustment(null));

        verifyNoInteractions(productRepositoryPort);
    }

    @Test
    @DisplayName("applyBatchPriceAdjustment propagates CLP floor/ceiling violations and verifies HALF_UP rounding")
    void batchPriceAdjustmentRoundingAndFloorCeilingMargins() {
        // Verify domain MoneyClp HALF_UP rounding rules used for Chilean Peso (CLP) adjustments
        assertEquals(1_161, MoneyClp.of(1_005).adjustByPercentage(15.5).amount()); // 1005 * 1.155 = 1160.775 -> 1161
        assertEquals(1_001, MoneyClp.of(1_000).adjustByPercentage(0.05).amount()); // 1000 * 1.0005 = 1000.5 -> 1001
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(1_500).adjustByPercentage(-50.0)); // 750 < 1000
        assertThrows(InvalidPriceMarginException.class, () -> MoneyClp.of(40_000_000).adjustByPercentage(50.0)); // 60M > 50M

        // Verify repository-level floor/ceiling margin exception propagates through DashboardService
        BatchPriceAdjustmentCommand command = new BatchPriceAdjustmentCommand(
                -50.0,
                Category.ACCESORIOS,
                "Descuento que rompe piso minimo de $1.000 CLP"
        );
        when(productRepositoryPort.applyBatchPriceAdjustment(-50.0, Category.ACCESORIOS))
                .thenThrow(new InvalidPriceMarginException("Adjusted price 750 CLP violates allowed price bounds"));

        assertThrows(InvalidPriceMarginException.class, () -> dashboardService.applyBatchPriceAdjustment(command));
    }
}
