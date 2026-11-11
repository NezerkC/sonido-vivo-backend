package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;

/**
 * Aggregated inventory metric per product category.
 */
public record CategoryMetric(
        String category,
        int totalUnits,
        long totalValueClp,
        double averagePrice
) {

    public CategoryMetric {
        if (category == null || category.isBlank()) {
            throw new InvalidDomainDataException("Category metric name must not be blank");
        }
        if (totalUnits < 0) {
            throw new InvalidDomainDataException("Total units cannot be negative: " + totalUnits);
        }
        if (totalValueClp < 0) {
            throw new InvalidDomainDataException("Total value CLP cannot be negative: " + totalValueClp);
        }
        if (!Double.isFinite(averagePrice) || averagePrice < 0.0) {
            throw new InvalidDomainDataException("Average price must be finite and non-negative: " + averagePrice);
        }
        category = category.trim();
    }
}
