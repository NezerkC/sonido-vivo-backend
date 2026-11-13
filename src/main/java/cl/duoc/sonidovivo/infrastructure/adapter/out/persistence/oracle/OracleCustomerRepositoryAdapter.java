package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.oracle;

import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Oracle JPA outbound adapter implementing {@link CustomerRepositoryPort}.
 *
 * <p>Responsable asignado: <strong>Danilo Sepúlveda (@Daakhen)</strong></p>
 * <p>Documentación y contrato: {@code docs/BACKEND_DATABASE_INTEGRATION.md}</p>
 */
@Repository
public class OracleCustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final SpringDataOracleCustomerRepository jpaRepository;

    public OracleCustomerRepositoryAdapter(SpringDataOracleCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // 1. Mapear Customer de dominio a OracleCustomerEntity (RUT, nombres, email, fono, comuna, región).
        // 2. Persistir entidad con jpaRepository.save(entity).
        // 3. Reconstituir y retornar Customer de dominio.
        throw new UnsupportedOperationException("Customer save pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public Optional<Customer> findById(String id) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // Buscar por ID con jpaRepository.findById(id) y mapear a dominio con toDomain.
        throw new UnsupportedOperationException("Customer findById pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public Optional<Customer> findByRut(String rut) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // Buscar por RUT con jpaRepository.findByRut(rut) y mapear a dominio.
        throw new UnsupportedOperationException("Customer findByRut pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public boolean existsByRut(String rut) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // Delegar existencia por RUT con jpaRepository.existsByRut(rut).
        throw new UnsupportedOperationException("Customer existsByRut pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public List<Customer> findAll() {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // Listar todos los registros con jpaRepository.findAll() y mapear a lista de dominio.
        throw new UnsupportedOperationException("Customer findAll pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }
}
