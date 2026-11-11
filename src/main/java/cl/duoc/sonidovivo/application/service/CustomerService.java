package cl.duoc.sonidovivo.application.service;

import cl.duoc.sonidovivo.application.port.in.CustomerUseCase;
import cl.duoc.sonidovivo.domain.exception.CustomerNotFoundException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import java.util.List;
import java.util.Objects;

/**
 * Pure Java application service implementing customer management use cases.
 */
public class CustomerService implements CustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    public CustomerService(CustomerRepositoryPort customerRepositoryPort) {
        this.customerRepositoryPort = Objects.requireNonNull(
                customerRepositoryPort,
                "CustomerRepositoryPort must not be null"
        );
    }

    @Override
    public List<Customer> listCustomers() {
        return customerRepositoryPort.findAll();
    }

    @Override
    public Customer getCustomerById(String customerId) {
        String validId = requireNonBlank(customerId, "Customer id");
        return customerRepositoryPort.findById(validId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + validId
                ));
    }

    @Override
    public Customer registerCustomer(RegisterCustomerCommand command) {
        if (command == null) {
            throw new InvalidDomainDataException("RegisterCustomerCommand must not be null");
        }

        Customer newCustomer = Customer.create(
                command.rut(),
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.commune(),
                command.region()
        );

        if (customerRepositoryPort.existsByRut(newCustomer.getRut())) {
            throw new InvalidDomainDataException(
                    "Customer already exists with RUT: " + newCustomer.getRut()
            );
        }

        return customerRepositoryPort.save(newCustomer);
    }

    @Override
    public Customer updateCustomer(String customerId, UpdateCustomerCommand command) {
        String validId = requireNonBlank(customerId, "Customer id");
        if (command == null) {
            throw new InvalidDomainDataException("UpdateCustomerCommand must not be null");
        }

        Customer existing = customerRepositoryPort.findById(validId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + validId
                ));

        existing.updateProfile(
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.commune(),
                command.region()
        );

        return customerRepositoryPort.save(existing);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainDataException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
