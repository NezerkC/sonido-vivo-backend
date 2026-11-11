package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.oracle;

import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.model.EmailAddress;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Oracle JPA outbound adapter implementing {@link CustomerRepositoryPort}.
 */
@Repository
public class OracleCustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final SpringDataOracleCustomerRepository jpaRepository;

    public OracleCustomerRepositoryAdapter(SpringDataOracleCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        OracleCustomerEntity entity = new OracleCustomerEntity();
        entity.setId(customer.getId() != null ? customer.getId() : UUID.randomUUID().toString());
        entity.setRut(customer.getRut());
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail().value());
        entity.setPhone(customer.getPhone());
        entity.setCommune(customer.getCommune());
        entity.setRegion(customer.getRegion());

        OracleCustomerEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByRut(String rut) {
        return jpaRepository.findByRut(rut).map(this::toDomain);
    }

    @Override
    public boolean existsByRut(String rut) {
        return jpaRepository.existsByRut(rut);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Customer toDomain(OracleCustomerEntity entity) {
        return Customer.reconstitute(
                entity.getId(),
                entity.getRut(),
                entity.getFirstName(),
                entity.getLastName(),
                EmailAddress.of(entity.getEmail()),
                entity.getPhone(),
                entity.getCommune(),
                entity.getRegion()
        );
    }
}
