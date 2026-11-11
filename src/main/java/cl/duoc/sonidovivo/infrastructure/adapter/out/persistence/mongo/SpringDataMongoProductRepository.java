package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo;

import cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document.MongoProductDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for basic CRUD and SKU lookups on {@code productos}.
 */
public interface SpringDataMongoProductRepository extends MongoRepository<MongoProductDocument, String> {

    Optional<MongoProductDocument> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
