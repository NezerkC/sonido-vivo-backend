package cl.duoc.sonidovivo.application.service;

import cl.duoc.sonidovivo.application.port.in.DashboardUseCase;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.model.DashboardMetrics;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.port.out.DashboardMetricsPort;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.util.Objects;

/**
 * Pure Java application service implementing dashboard metrics and batch price adjustments.
 */
public class DashboardService implements DashboardUseCase {

    private final DashboardMetricsPort dashboardMetricsPort;
    private final ProductRepositoryPort productRepositoryPort;

    public DashboardService(
            DashboardMetricsPort dashboardMetricsPort,
            ProductRepositoryPort productRepositoryPort
    ) {
        this.dashboardMetricsPort = Objects.requireNonNull(
                dashboardMetricsPort,
                "DashboardMetricsPort must not be null"
        );
        this.productRepositoryPort = Objects.requireNonNull(
                productRepositoryPort,
                "ProductRepositoryPort must not be null"
        );
    }

    @Override
    public DashboardMetrics getInventoryMetrics() {
        return dashboardMetricsPort.getInventoryMetrics();
    }

    @Override
    public BatchPriceAdjustmentResult applyBatchPriceAdjustment(BatchPriceAdjustmentCommand command) {
        if (command == null) {
            throw new InvalidDomainDataException("BatchPriceAdjustmentCommand must not be null");
        }
        if (command.reason() == null || command.reason().isBlank()) {
            throw new InvalidDomainDataException("Price adjustment reason must not be blank");
        }

        MoneyClp.validateAdjustmentPercentage(command.percentage());

        int updatedCount = productRepositoryPort.applyBatchPriceAdjustment(
                command.percentage(),
                command.category()
        );

        return new BatchPriceAdjustmentResult(
                updatedCount,
                command.percentage(),
                command.category(),
                command.reason().trim()
        );
    }
}
