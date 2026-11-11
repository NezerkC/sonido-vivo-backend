package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo;

import cl.duoc.sonidovivo.domain.model.CategoryMetric;
import cl.duoc.sonidovivo.domain.model.DashboardMetrics;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.port.out.DashboardMetricsPort;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

/**
 * MongoDB aggregation pipeline adapter calculating valorized inventory metrics by category.
 */
@Repository
public class MongoMetricsRepositoryAdapter implements DashboardMetricsPort {

    private static final String COLLECTION_PRODUCTS = "productos";

    private final MongoTemplate mongoTemplate;

    public MongoMetricsRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public DashboardMetrics getInventoryMetrics() {
        MatchOperation matchActive = Aggregation.match(
                Criteria.where("estado").is(ProductStatus.ACTIVO.name())
        );

        GroupOperation groupByCategory = Aggregation.group("categoria")
                .sum("stock_disponible").as("totalUnits")
                .sum(ArithmeticOperators.Multiply.valueOf("precio_clp").multiplyBy("stock_disponible"))
                .as("totalValueClp")
                .avg("precio_clp").as("averagePrice");

        ProjectionOperation projectFields = Aggregation.project()
                .and("_id").as("category")
                .and("totalUnits").as("totalUnits")
                .and("totalValueClp").as("totalValueClp")
                .and("averagePrice").as("averagePrice");

        SortOperation sortByTotalValueDesc = Aggregation.sort(Sort.Direction.DESC, "totalValueClp");

        Aggregation aggregation = Aggregation.newAggregation(
                matchActive,
                groupByCategory,
                projectFields,
                sortByTotalValueDesc
        );

        AggregationResults<CategoryAggregationProjection> results = mongoTemplate.aggregate(
                aggregation,
                COLLECTION_PRODUCTS,
                CategoryAggregationProjection.class
        );

        List<CategoryMetric> categories = results.getMappedResults().stream()
                .map(row -> new CategoryMetric(
                        row.getCategory(),
                        row.getTotalUnits(),
                        row.getTotalValueClp(),
                        row.getAveragePrice()
                ))
                .toList();

        return DashboardMetrics.fromCategories(categories);
    }

    private static class CategoryAggregationProjection {

        private String category;
        private int totalUnits;
        private long totalValueClp;
        private double averagePrice;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getTotalUnits() {
            return totalUnits;
        }

        public void setTotalUnits(int totalUnits) {
            this.totalUnits = totalUnits;
        }

        public long getTotalValueClp() {
            return totalValueClp;
        }

        public void setTotalValueClp(long totalValueClp) {
            this.totalValueClp = totalValueClp;
        }

        public double getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(double averagePrice) {
            this.averagePrice = averagePrice;
        }
    }
}
