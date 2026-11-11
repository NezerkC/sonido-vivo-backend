package cl.duoc.sonidovivo.domain.port.out;

import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import java.time.LocalDate;

/**
 * Outbound port for Oracle PL/SQL transactional operations
 * ({@code SP_REGISTRAR_VENTA}, {@code SP_LIQUIDAR_CLIENTES}, {@code FN_CALCULAR_DESCUENTO}).
 */
public interface OracleSalesPort {

    SaleResult registerSale(SaleCommand command);

    LiquidationResult liquidateCustomers(LocalDate processDate);

    long calculateDiscount(String customerId, long grossAmountClp);
}
