package cl.duoc.sonidovivo.application.port.in;

import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import java.time.LocalDate;

/**
 * Inbound port orchestrating polyglot sales and customer liquidation operations against Oracle PL/SQL.
 */
public interface OracleIntegrationUseCase {

    SaleResult registerSale(SaleCommand command);

    LiquidationResult runCustomerLiquidation(LocalDate processDate);

    long calculateDiscount(String customerId, long grossAmountClp);
}
