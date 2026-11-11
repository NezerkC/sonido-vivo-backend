package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.util.List;

/**
 * Aggregated inventory KPIs and per-category breakdown for the admin dashboard.
 */
public record DashboardMetrics(
        long totalWarehouseValueClp,
        int totalUnits,
        int activeCategories,
        List<CategoryMetric> categories
) {

    public DashboardMetrics {
        if (totalWarehouseValueClp < 0) {
            throw new InvalidDomainDataException(
                    "Total warehouse value cannot be negative: " + totalWarehouseValueClp
            );
        }
        if (totalUnits < 0) {
            throw new InvalidDomainDataException("Total units cannot be negative: " + totalUnits);
        }
        if (activeCategories < 0) {
            throw new InvalidDomainDataException("Active categories cannot be negative: " + activeCategories);
        }
        categories = categories == null ? List.of() : List.copyOf(categories);
    }

    public static DashboardMetrics fromCategories(List<CategoryMetric> categories) {
        List<CategoryMetric> safeCategories = categories == null ? List.of() : List.copyOf(categories);
        long totalValue = safeCategories.stream().mapToLong(CategoryMetric::totalValueClp).sum();
        int units = safeCategories.stream().mapToInt(CategoryMetric::totalUnits).sum();
        return new DashboardMetrics(totalValue, units, safeCategories.size(), safeCategories);
    }
}
