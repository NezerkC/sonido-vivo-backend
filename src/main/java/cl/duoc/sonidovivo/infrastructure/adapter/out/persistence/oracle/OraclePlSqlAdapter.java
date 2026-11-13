package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.oracle;

import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.domain.port.out.OracleSalesPort;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Oracle PL/SQL outbound adapter invoking {@code PKG_SONIDO_VIVO} routines via {@code SimpleJdbcCall}.
 *
 * <p>Responsable asignado: <strong>Danilo Sepúlveda (@Daakhen)</strong></p>
 * <p>Documentación y contrato: {@code docs/BACKEND_DATABASE_INTEGRATION.md} y {@code docs/DEPLOYMENT_ORACLE_MONGO_CT100.md}</p>
 */
@Repository
public class OraclePlSqlAdapter implements OracleSalesPort {

    public static final String PACKAGE_NAME = "PKG_SONIDO_VIVO";
    public static final String PROC_REGISTER_SALE = "SP_REGISTRAR_VENTA";
    public static final String PROC_LIQUIDATE_CUSTOMERS = "SP_LIQUIDAR_CLIENTES";
    public static final String FUNC_CALCULATE_DISCOUNT = "FN_CALCULAR_DESCUENTO";

    private final JdbcTemplate jdbcTemplate;

    public OraclePlSqlAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SaleResult registerSale(SaleCommand command) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // 1. Configurar SimpleJdbcCall con catalog PKG_SONIDO_VIVO y procedure SP_REGISTRAR_VENTA.
        // 2. Declarar parámetros IN (P_CUSTOMER_ID, P_PRODUCT_CODE, P_QUANTITY, P_UNIT_PRICE).
        // 3. Declarar parámetros OUT (P_SALE_REFERENCE, P_GROSS_TOTAL, P_DISCOUNT_CLP, P_NET_TOTAL).
        // 4. Ejecutar y retornar una instancia inmutable de SaleResult.
        throw new UnsupportedOperationException("SP_REGISTRAR_VENTA pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public LiquidationResult liquidateCustomers(LocalDate processDate) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // 1. Configurar SimpleJdbcCall con catalog PKG_SONIDO_VIVO y procedure SP_LIQUIDAR_CLIENTES.
        // 2. Declarar parámetros IN (P_PROCESS_DATE) y OUT (P_PROCESSED_CUSTOMERS, P_TOTAL_LIQUIDATED, P_STATUS_MESSAGE).
        // 3. Ejecutar y retornar LiquidationResult.
        throw new UnsupportedOperationException("SP_LIQUIDAR_CLIENTES pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }

    @Override
    public long calculateDiscount(String customerId, long grossAmountClp) {
        // TODO (@Daakhen - Danilo Sepúlveda):
        // 1. Configurar SimpleJdbcCall con catalog PKG_SONIDO_VIVO y function FN_CALCULAR_DESCUENTO.
        // 2. Ejecutar función vía call.executeFunction(BigDecimal.class, params) y retornar el monto del descuento.
        throw new UnsupportedOperationException("FN_CALCULAR_DESCUENTO pendiente de implementación por Danilo Sepúlveda (@Daakhen)");
    }
}
