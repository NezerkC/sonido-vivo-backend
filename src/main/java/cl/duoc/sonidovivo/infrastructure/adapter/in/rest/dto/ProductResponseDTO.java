package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.Review;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO representing a catalog product aligned with {@code CONTRACT_FULLSTACK_REST.md}.
 */
public record ProductResponseDTO(
        String codigo,
        String nombre,
        String categoria,
        @JsonProperty("precio_clp")
        int precioClp,
        @JsonProperty("stock_disponible")
        int stockDisponible,
        String estado,
        Map<String, Object> especificaciones,
        List<String> tags,
        List<ResenaDTO> resenas,
        @JsonProperty("ultima_venta")
        Instant ultimaVenta,
        @JsonProperty("fecha_baja")
        Instant fechaBaja,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponseDTO fromDomain(Product product) {
        List<ResenaDTO> resenas = product.getReviews().stream()
                .map(ResenaDTO::fromDomain)
                .toList();

        return new ProductResponseDTO(
                product.getCode().value(),
                product.getName(),
                product.getCategory().getDisplayName(),
                product.getPrice().amount(),
                product.getStock().value(),
                product.getStatus().name(),
                product.getSpecifications(),
                product.getTags(),
                resenas,
                product.getLastSaleAt(),
                product.getDiscontinuedAt(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public record ResenaDTO(
            String cliente,
            int calificacion,
            String comentario,
            Instant fecha
    ) {
        public static ResenaDTO fromDomain(Review review) {
            return new ResenaDTO(
                    review.customer(),
                    review.rating(),
                    review.comment(),
                    review.date()
            );
        }
    }
}
