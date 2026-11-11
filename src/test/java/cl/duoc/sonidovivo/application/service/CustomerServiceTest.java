package cl.duoc.sonidovivo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.sonidovivo.application.port.in.CustomerUseCase.RegisterCustomerCommand;
import cl.duoc.sonidovivo.application.port.in.CustomerUseCase.UpdateCustomerCommand;
import cl.duoc.sonidovivo.domain.exception.CustomerNotFoundException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.model.EmailAddress;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepositoryPort);
    }

    private Customer sampleCustomer(String id) {
        return Customer.reconstitute(
                id,
                "12.345.678-K",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@sonidovivo.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );
    }

    @Test
    @DisplayName("listCustomers returns all registered customers")
    void listCustomersHappyPath() {
        Customer customer = sampleCustomer("cust-1");
        when(customerRepositoryPort.findAll()).thenReturn(List.of(customer));

        List<Customer> customers = customerService.listCustomers();

        assertEquals(1, customers.size());
        assertEquals("cust-1", customers.get(0).getId());
        verify(customerRepositoryPort).findAll();
    }

    @Test
    @DisplayName("getCustomerById returns customer or throws CustomerNotFoundException")
    void getCustomerByIdBehavior() {
        Customer customer = sampleCustomer("cust-1");
        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(customer));
        when(customerRepositoryPort.findById("cust-missing")).thenReturn(Optional.empty());

        Customer found = customerService.getCustomerById(" cust-1 ");
        assertEquals("12.345.678-K", found.getRut());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById("cust-missing"));
        assertThrows(InvalidDomainDataException.class, () -> customerService.getCustomerById("  "));
    }

    @Test
    @DisplayName("registerCustomer validates uniqueness by normalized RUT and persists customer")
    void registerCustomerHappyPath() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "12.345.678-k",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@sonidovivo.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );

        when(customerRepositoryPort.existsByRut("12.345.678-K")).thenReturn(false);
        when(customerRepositoryPort.save(any(Customer.class)))
                .thenAnswer(inv -> ((Customer) inv.getArgument(0)).withId("cust-99"));

        Customer registered = customerService.registerCustomer(command);

        assertEquals("cust-99", registered.getId());
        assertEquals("12.345.678-K", registered.getRut());
        verify(customerRepositoryPort).existsByRut("12.345.678-K");
        verify(customerRepositoryPort).save(any(Customer.class));
    }

    @Test
    @DisplayName("registerCustomer rejects duplicate RUT without persisting")
    void registerCustomerRejectsDuplicateRut() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "12.345.678-K",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@sonidovivo.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );

        when(customerRepositoryPort.existsByRut("12.345.678-K")).thenReturn(true);

        assertThrows(InvalidDomainDataException.class, () -> customerService.registerCustomer(command));
        verify(customerRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("updateCustomer updates profile fields and persists existing customer")
    void updateCustomerHappyPath() {
        Customer existing = sampleCustomer("cust-1");
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                "Victor Manuel",
                "Martinez Munoz",
                EmailAddress.of("nuevo@sonidovivo.cl"),
                "+56987654321",
                "Providencia",
                "Metropolitana"
        );

        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(existing));
        when(customerRepositoryPort.save(existing)).thenReturn(existing);

        Customer updated = customerService.updateCustomer("cust-1", command);

        assertEquals("Victor Manuel Martinez Munoz", updated.getFullName());
        assertEquals("nuevo@sonidovivo.cl", updated.getEmail().value());
        assertEquals("Providencia", updated.getCommune());
        verify(customerRepositoryPort).save(existing);
    }

    @Test
    @DisplayName("updateCustomer throws CustomerNotFoundException when id does not exist")
    void updateCustomerThrowsWhenMissing() {
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                "Victor",
                "Martinez",
                EmailAddress.of("victor@sonidovivo.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );
        assertThrows(InvalidDomainDataException.class, () -> customerService.updateCustomer(" ", command));
        verifyNoInteractions(customerRepositoryPort);

        when(customerRepositoryPort.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer("unknown", command));
        verify(customerRepositoryPort).findById("unknown");
        verify(customerRepositoryPort, never()).save(any());
    }
}
