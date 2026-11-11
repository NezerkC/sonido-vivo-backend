package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import cl.duoc.sonidovivo.domain.model.Customer;

/**
 * Response DTO representing a registered customer profile.
 */
public record CustomerResponseDTO(
        String id,
        String rut,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String commune,
        String region
) {

    public static CustomerResponseDTO fromDomain(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getRut(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getEmail().value(),
                customer.getPhone(),
                customer.getCommune(),
                customer.getRegion()
        );
    }
}
