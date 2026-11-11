package cl.duoc.sonidovivo.application.port.in;

import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.model.EmailAddress;
import java.util.List;

/**
 * Inbound port defining customer management use cases.
 */
public interface CustomerUseCase {

    List<Customer> listCustomers();

    Customer getCustomerById(String customerId);

    Customer registerCustomer(RegisterCustomerCommand command);

    Customer updateCustomer(String customerId, UpdateCustomerCommand command);

    /**
     * Command carrying the attributes required to register a new customer.
     */
    record RegisterCustomerCommand(
            String rut,
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {}

    /**
     * Command carrying updated profile details for an existing customer.
     */
    record UpdateCustomerCommand(
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {}
}
