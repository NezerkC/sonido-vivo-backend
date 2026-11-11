package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.CustomerUseCase;
import cl.duoc.sonidovivo.application.port.in.CustomerUseCase.RegisterCustomerCommand;
import cl.duoc.sonidovivo.application.port.in.CustomerUseCase.UpdateCustomerCommand;
import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.model.EmailAddress;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.CustomerRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller managing administrative customer operations.
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    private final CustomerUseCase customerUseCase;

    public AdminCustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> listCustomers() {
        List<CustomerResponseDTO> customers = customerUseCase.listCustomers().stream()
                .map(CustomerResponseDTO::fromDomain)
                .toList();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable String id) {
        Customer customer = customerUseCase.getCustomerById(id);
        return ResponseEntity.ok(CustomerResponseDTO.fromDomain(customer));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> registerCustomer(@Valid @RequestBody CustomerRequestDTO request) {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                request.rut(),
                request.firstName(),
                request.lastName(),
                EmailAddress.of(request.email()),
                request.phone(),
                request.commune(),
                request.region()
        );
        Customer created = customerUseCase.registerCustomer(command);
        URI location = URI.create("/api/v1/admin/customers/" + created.getId());
        return ResponseEntity.created(location).body(CustomerResponseDTO.fromDomain(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequestDTO request
    ) {
        UpdateCustomerCommand command = new UpdateCustomerCommand(
                request.firstName(),
                request.lastName(),
                EmailAddress.of(request.email()),
                request.phone(),
                request.commune(),
                request.region()
        );
        Customer updated = customerUseCase.updateCustomer(id, command);
        return ResponseEntity.ok(CustomerResponseDTO.fromDomain(updated));
    }
}
