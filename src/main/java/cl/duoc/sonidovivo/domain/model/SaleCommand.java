package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;

/**
 * Domain command representing a sale to be registered in the relational PL/SQL ledger.
 */
public record SaleCommand(
        String customerId,
        ProductCode productCode,
        int quantity,
        MoneyClp unitPrice
) {

    public SaleCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidDomainDataException("Customer id must not be null or blank");
        }
        if (productCode == null) {
            throw new InvalidDomainDataException("Product code must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidDomainDataException("Sale quantity must be positive: " + quantity);
        }
        if (unitPrice == null) {
            throw new InvalidDomainDataException("Unit price must not be null");
        }
        customerId = customerId.trim();
    }

    public long grossTotalClp() {
        return unitPrice.multiplyBy(quantity);
    }
}
