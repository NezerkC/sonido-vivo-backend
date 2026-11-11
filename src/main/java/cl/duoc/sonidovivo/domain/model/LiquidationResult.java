package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.time.LocalDate;

/**
 * Summary returned after executing customer liquidation via {@code SP_LIQUIDAR_CLIENTES}.
 */
public record LiquidationResult(
        int processedCustomers,
        long totalLiquidatedClp,
        LocalDate processDate,
        String statusMessage
) {

    public LiquidationResult {
        if (processedCustomers < 0) {
            throw new InvalidDomainDataException("Processed customers cannot be negative: " + processedCustomers);
        }
        if (totalLiquidatedClp < 0) {
            throw new InvalidDomainDataException("Total liquidated CLP cannot be negative: " + totalLiquidatedClp);
        }
        if (processDate == null) {
            throw new InvalidDomainDataException("Process date must not be null");
        }
        if (statusMessage == null || statusMessage.isBlank()) {
            throw new InvalidDomainDataException("Status message must not be blank");
        }
        statusMessage = statusMessage.trim();
    }
}
