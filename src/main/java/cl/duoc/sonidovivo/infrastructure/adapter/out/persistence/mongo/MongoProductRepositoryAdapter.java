package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo;

import cl.duoc.sonidovivo.domain.exception.DuplicateProductCodeException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.Review;
import cl.duoc.sonidovivo.domain.model.StockQuantity;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document.MongoProductDocument;
import cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document.ResenaEmbeddable;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * MongoDB outbound adapter implementing {@link ProductRepositoryPort} with atomic {@link MongoTemplate} mutations.
 */
@Repository
public class MongoProductRepositoryAdapter implements ProductRepositoryPort {

    private static final String FIELD_ID = "_id";
    private static final String FIELD_CODE = "codigo";
    private static final String FIELD_NAME = "nombre";
    private static final String FIELD_CATEGORY = "categoria";
    private static final String FIELD_PRICE = "precio_clp";
    private static final String FIELD_STOCK = "stock_disponible";
    private static final String FIELD_STATUS = "estado";
    private static final String FIELD_SPECS_PREFIX = "especificaciones.";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_REVIEWS = "resenas";
    private static final String FIELD_LAST_SALE = "ultima_venta";
    private static final String FIELD_DISCONTINUED_AT = "fecha_baja";
    private static final String FIELD_UPDATED_AT = "updated_at";

    private final SpringDataMongoProductRepository springDataRepository;
    private final MongoTemplate mongoTemplate;

    public MongoProductRepositoryAdapter(
            SpringDataMongoProductRepository springDataRepository,
            MongoTemplate mongoTemplate
    ) {
        this.springDataRepository = springDataRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Product save(Product product) {
        MongoProductDocument document = toDocument(product);
        try {
            MongoProductDocument saved = springDataRepository.save(document);
            return toDomain(saved);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateProductCodeException(
                    "Product code already exists: " + product.getCode().value(),
                    ex
            );
        }
    }

    @Override
    public Optional<Product> findByCode(ProductCode code) {
        return springDataRepository.findByCodigo(code.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByCode(ProductCode code) {
        return springDataRepository.existsByCodigo(code.value());
    }

    @Override
    public List<Product> findAllPaged(int page, int size, Category category, ProductStatus status) {
        Query query = new Query();
        if (category != null) {
            query.addCriteria(Criteria.where(FIELD_CATEGORY).is(category.getDisplayName()));
        }
        if (status != null) {
            query.addCriteria(Criteria.where(FIELD_STATUS).is(status.name()));
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        query.with(PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, FIELD_CODE)));

        return mongoTemplate.find(query, MongoProductDocument.class).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> search(String queryText, Category category, Map<String, Object> specificationFilters) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (category != null) {
            criteriaList.add(Criteria.where(FIELD_CATEGORY).is(category.getDisplayName()));
        }
        if (queryText != null && !queryText.isBlank()) {
            String escaped = Pattern.quote(queryText.trim());
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where(FIELD_NAME).regex(escaped, "i"),
                    Criteria.where(FIELD_CODE).regex(escaped, "i"),
                    Criteria.where(FIELD_TAGS).regex(escaped, "i")
            ));
        }
        if (specificationFilters != null && !specificationFilters.isEmpty()) {
            for (Map.Entry<String, Object> entry : specificationFilters.entrySet()) {
                String rawKey = entry.getKey();
                if (rawKey == null || rawKey.isBlank()) {
                    throw new InvalidDomainDataException("Specification filter key must not be blank");
                }
                String trimmedKey = rawKey.trim();
                if (trimmedKey.startsWith(FIELD_SPECS_PREFIX)) {
                    trimmedKey = trimmedKey.substring(FIELD_SPECS_PREFIX.length());
                }
                if (trimmedKey.isBlank()
                        || trimmedKey.startsWith("$")
                        || trimmedKey.contains(".$")
                        || trimmedKey.contains("..")) {
                    throw new InvalidDomainDataException("Invalid specification filter key: " + rawKey);
                }
                if (entry.getValue() != null) {
                    criteriaList.add(Criteria.where(FIELD_SPECS_PREFIX + trimmedKey).is(entry.getValue()));
                }
            }
        }

        Query mongoQuery = new Query();
        if (!criteriaList.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criteriaList.toArray(Criteria[]::new)));
        }
        mongoQuery.with(Sort.by(Sort.Direction.ASC, FIELD_NAME));

        return mongoTemplate.find(mongoQuery, MongoProductDocument.class).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean adjustStockAtomic(ProductCode code, int delta) {
        if (delta == 0) {
            throw new InvalidDomainDataException("Stock adjustment delta must be non-zero");
        }
        Instant now = Instant.now();

        if (delta < 0) {
            long decrement = -(long) delta;
            Query query = Query.query(
                    Criteria.where(FIELD_CODE).is(code.value())
                            .and(FIELD_STATUS).ne(ProductStatus.DESCONTINUADO.name())
                            .and(FIELD_STOCK).gte(decrement)
            );
            Update update = new Update()
                    .inc(FIELD_STOCK, delta)
                    .set(FIELD_LAST_SALE, now)
                    .set(FIELD_UPDATED_AT, now);

            MongoProductDocument updated = mongoTemplate.findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    MongoProductDocument.class
            );
            if (updated == null) {
                return false;
            }
            if (updated.getStockDisponible() != null
                    && updated.getStockDisponible() == 0
                    && !ProductStatus.AGOTADO.name().equals(updated.getEstado())) {
                Query markOutOfStock = Query.query(
                        Criteria.where(FIELD_CODE).is(code.value())
                                .and(FIELD_STOCK).is(0)
                                .and(FIELD_STATUS).is(ProductStatus.ACTIVO.name())
                );
                mongoTemplate.updateFirst(
                        markOutOfStock,
                        new Update().set(FIELD_STATUS, ProductStatus.AGOTADO.name()).set(FIELD_UPDATED_AT, now),
                        MongoProductDocument.class
                );
            }
            return true;
        }

        Query query = Query.query(
                Criteria.where(FIELD_CODE).is(code.value())
                        .and(FIELD_STATUS).ne(ProductStatus.DESCONTINUADO.name())
        );
        Update update = new Update()
                .inc(FIELD_STOCK, delta)
                .set(FIELD_UPDATED_AT, now);

        MongoProductDocument updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                MongoProductDocument.class
        );
        if (updated == null) {
            return false;
        }
        if (updated.getStockDisponible() != null
                && updated.getStockDisponible() > 0
                && ProductStatus.AGOTADO.name().equals(updated.getEstado())) {
            Query reactivate = Query.query(
                    Criteria.where(FIELD_CODE).is(code.value())
                            .and(FIELD_STOCK).gt(0)
                            .and(FIELD_STATUS).is(ProductStatus.AGOTADO.name())
            );
            mongoTemplate.updateFirst(
                    reactivate,
                    new Update().set(FIELD_STATUS, ProductStatus.ACTIVO.name()).set(FIELD_UPDATED_AT, now),
                    MongoProductDocument.class
            );
        }
        return true;
    }

    @Override
    public void addReview(ProductCode code, Review review) {
        Query query = Query.query(Criteria.where(FIELD_CODE).is(code.value()));
        ResenaEmbeddable embeddable = new ResenaEmbeddable(
                review.customer(),
                review.rating(),
                review.comment(),
                review.date()
        );
        Update update = new Update()
                .push(FIELD_REVIEWS, embeddable)
                .set(FIELD_UPDATED_AT, Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, MongoProductDocument.class);
        if (result.getMatchedCount() == 0) {
            throw new ProductNotFoundException("Product not found: " + code.value());
        }
    }

    @Override
    public void updateStatus(ProductCode code, ProductStatus status) {
        Query query = Query.query(Criteria.where(FIELD_CODE).is(code.value()));
        Instant now = Instant.now();
        Update update = new Update()
                .set(FIELD_STATUS, status.name())
                .set(FIELD_UPDATED_AT, now);
        if (status == ProductStatus.DESCONTINUADO) {
            update.set(FIELD_DISCONTINUED_AT, now);
        } else {
            update.unset(FIELD_DISCONTINUED_AT);
        }

        UpdateResult result = mongoTemplate.updateFirst(query, update, MongoProductDocument.class);
        if (result.getMatchedCount() == 0) {
            throw new ProductNotFoundException("Product not found: " + code.value());
        }
    }

    @Override
    public int applyBatchPriceAdjustment(double percentage, Category category) {
        MoneyClp.validateAdjustmentPercentage(percentage);

        Query query = Query.query(Criteria.where(FIELD_STATUS).is(ProductStatus.ACTIVO.name()));
        if (category != null) {
            query.addCriteria(Criteria.where(FIELD_CATEGORY).is(category.getDisplayName()));
        }

        List<MongoProductDocument> activeProducts = mongoTemplate.find(query, MongoProductDocument.class);
        if (activeProducts.isEmpty()) {
            return 0;
        }

        // Compute and validate all adjusted prices before mutating any document.
        List<PriceTarget> priceTargets = new ArrayList<>(activeProducts.size());
        for (MongoProductDocument doc : activeProducts) {
            MoneyClp currentPrice = MoneyClp.of(doc.getPrecioClp());
            MoneyClp adjustedPrice = currentPrice.adjustByPercentage(percentage);
            priceTargets.add(new PriceTarget(doc.getId(), adjustedPrice.amount()));
        }

        Instant now = Instant.now();
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, MongoProductDocument.class);
        for (PriceTarget target : priceTargets) {
            Query byId = Query.query(Criteria.where(FIELD_ID).is(target.id()));
            Update update = new Update()
                    .set(FIELD_PRICE, target.newPriceClp())
                    .set(FIELD_UPDATED_AT, now);
            bulkOps.updateOne(byId, update);
        }

        BulkWriteResult result = bulkOps.execute();
        return result.getModifiedCount();
    }

    private MongoProductDocument toDocument(Product product) {
        MongoProductDocument doc = new MongoProductDocument();
        doc.setId(product.getId());
        doc.setCodigo(product.getCode().value());
        doc.setNombre(product.getName());
        doc.setCategoria(product.getCategory().getDisplayName());
        doc.setPrecioClp(product.getPrice().amount());
        doc.setStockDisponible(product.getStock().value());
        doc.setEstado(product.getStatus().name());
        doc.setEspecificaciones(product.getSpecifications());
        doc.setTags(product.getTags());
        doc.setResenas(product.getReviews().stream()
                .map(review -> new ResenaEmbeddable(
                        review.customer(),
                        review.rating(),
                        review.comment(),
                        review.date()
                ))
                .toList());
        doc.setUltimaVenta(product.getLastSaleAt());
        doc.setFechaBaja(product.getDiscontinuedAt());
        doc.setCreatedAt(product.getCreatedAt());
        doc.setUpdatedAt(product.getUpdatedAt());
        return doc;
    }

    private Product toDomain(MongoProductDocument doc) {
        List<Review> reviews = doc.getResenas() == null
                ? List.of()
                : doc.getResenas().stream()
                        .map(r -> new Review(r.getCliente(), r.getCalificacion(), r.getComentario(), r.getFecha()))
                        .toList();

        return Product.reconstitute(
                doc.getId(),
                ProductCode.of(doc.getCodigo()),
                doc.getNombre(),
                Category.fromString(doc.getCategoria()),
                MoneyClp.of(doc.getPrecioClp()),
                StockQuantity.of(doc.getStockDisponible()),
                ProductStatus.fromString(doc.getEstado()),
                doc.getEspecificaciones(),
                doc.getTags(),
                reviews,
                doc.getUltimaVenta(),
                doc.getFechaBaja(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    private record PriceTarget(String id, int newPriceClp) {
    }
}
