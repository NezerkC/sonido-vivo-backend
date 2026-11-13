package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.application.port.in.CustomerUseCase;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.CustomerRequestDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
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
 *
 * <p>Responsable asignado: <strong>Dylan Sandoval (@dysandoval)</strong></p>
 * <p>Documentación y contrato: {@code docs/contracts/CONTRACT_FULLSTACK_REST.md}</p>
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
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar customerUseCase.listCustomers().
        // 2. Mapear entidades a CustomerResponseDTO.
        // 3. Retornar ResponseEntity.ok(lista).
        throw new UnsupportedOperationException("listCustomers pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable String id) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Invocar customerUseCase.getCustomerById(id).
        // 2. Mapear a CustomerResponseDTO.
        // 3. Retornar ResponseEntity.ok(dto).
        throw new UnsupportedOperationException("getCustomer pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> registerCustomer(@Valid @RequestBody CustomerRequestDTO request) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Construir RegisterCustomerCommand desde request (validando RUT chileno y email).
        // 2. Invocar customerUseCase.registerCustomer(command).
        // 3. Retornar ResponseEntity.created(URI.create("/api/v1/admin/customers/" + created.getId())).body(CustomerResponseDTO.fromDomain(created)).
        throw new UnsupportedOperationException("registerCustomer pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequestDTO request
    ) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Construir UpdateCustomerCommand desde request.
        // 2. Invocar customerUseCase.updateCustomer(id, command).
        // 3. Retornar ResponseEntity.ok(CustomerResponseDTO.fromDomain(updated)).
        throw new UnsupportedOperationException("updateCustomer pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }
}
