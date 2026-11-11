package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import cl.duoc.sonidovivo.domain.model.DashboardMetrics;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Dashboard KPI and per-category breakdown payload matching {@code CONTRACT_FULLSTACK_REST.md}.
 */
public record DashboardMetricsResponseDTO(
        KpisDTO kpis,
        List<CategoriaMetricaDTO> desglosePorCategoria
) {

    public static DashboardMetricsResponseDTO fromDomain(DashboardMetrics metrics) {
        KpisDTO kpis = new KpisDTO(
                metrics.totalWarehouseValueClp(),
                metrics.totalUnits(),
                metrics.activeCategories()
        );
        List<CategoriaMetricaDTO> breakdown = metrics.categories().stream()
                .map(c -> new CategoriaMetricaDTO(
                        c.category(),
                        c.totalUnits(),
                        c.totalValueClp(),
                        c.averagePrice()
                ))
                .toList();
        return new DashboardMetricsResponseDTO(kpis, breakdown);
    }

    public record KpisDTO(
            long capitalTotalEnBodega,
            int unidadesTotales,
            int categoriasActivas
    ) {
    }

    public record CategoriaMetricaDTO(
            String categoria,
            @JsonProperty("total_unidades")
            int totalUnidades,
            @JsonProperty("valor_total_inventario")
            long valorTotalInventario,
            @JsonProperty("precio_promedio")
            double precioPromedio
    ) {
    }
}
