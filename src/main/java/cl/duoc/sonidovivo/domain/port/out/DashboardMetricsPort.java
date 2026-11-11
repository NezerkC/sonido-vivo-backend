package cl.duoc.sonidovivo.domain.port.out;

import cl.duoc.sonidovivo.domain.model.DashboardMetrics;

/**
 * Outbound port for aggregated inventory metrics used by the admin dashboard.
 */
public interface DashboardMetricsPort {

    DashboardMetrics getInventoryMetrics();
}
