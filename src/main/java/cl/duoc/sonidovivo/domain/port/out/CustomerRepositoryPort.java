package cl.duoc.sonidovivo.domain.port.out;

import cl.duoc.sonidovivo.domain.model.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for customer management.
 */
public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findById(String id);

    Optional<Customer> findByRut(String rut);

    boolean existsByRut(String rut);

    List<Customer> findAll();
}
