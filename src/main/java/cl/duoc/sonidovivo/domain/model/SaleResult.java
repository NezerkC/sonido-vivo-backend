package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.time.Instant;

/**
 * Domain result returned after registering a sale via Oracle PL/SQL {@code SP_REGISTRAR_VENTA}.
 */
public record SaleResult(
        String saleReference,
        String customerId,
        ProductCode productCode,
        int quantity,
        long grossTotalClp,
        long discountClp,
        long netTotalClp,
        Instant registeredAt
) {

    public SaleResult {
        if (saleReference == null || saleReference.isBlank()) {
            throw new InvalidDomainDataException("Sale reference must not be blank");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidDomainDataException("Customer id must not be blank");
        }
        if (productCode == null) {
            throw new InvalidDomainDataException("Product code must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidDomainDataException("Sale quantity must be positive: " + quantity);
        }
        if (grossTotalClp < 0 || discountClp < 0 || netTotalClp < 0) {
            throw new InvalidDomainDataException("Sale amounts cannot be negative");
        }
        if (registeredAt == null) {
            throw new InvalidDomainDataException("Registered timestamp must not be null");
        }
        saleReference = saleReference.trim();
        customerId = customerId.trim();
    }
}
