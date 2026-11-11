package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.oracle;

import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.domain.port.out.OracleSalesPort;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

/**
 * Oracle PL/SQL outbound adapter invoking {@code PKG_SONIDO_VIVO} routines via {@link SimpleJdbcCall}.
 */
@Repository
public class OraclePlSqlAdapter implements OracleSalesPort {

    private static final String PACKAGE_NAME = "PKG_SONIDO_VIVO";
    private static final String PROC_REGISTER_SALE = "SP_REGISTRAR_VENTA";
    private static final String PROC_LIQUIDATE_CUSTOMERS = "SP_LIQUIDAR_CLIENTES";
    private static final String FUNC_CALCULATE_DISCOUNT = "FN_CALCULAR_DESCUENTO";

    private final JdbcTemplate jdbcTemplate;

    public OraclePlSqlAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SaleResult registerSale(SaleCommand command) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE_NAME)
                .withProcedureName(PROC_REGISTER_SALE)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_CUSTOMER_ID", Types.VARCHAR),
                        new SqlParameter("P_PRODUCT_CODE", Types.VARCHAR),
                        new SqlParameter("P_QUANTITY", Types.NUMERIC),
                        new SqlParameter("P_UNIT_PRICE", Types.NUMERIC),
                        new SqlOutParameter("P_SALE_REFERENCE", Types.VARCHAR),
                        new SqlOutParameter("P_GROSS_TOTAL", Types.NUMERIC),
                        new SqlOutParameter("P_DISCOUNT_CLP", Types.NUMERIC),
                        new SqlOutParameter("P_NET_TOTAL", Types.NUMERIC)
                );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("P_CUSTOMER_ID", command.customerId())
                .addValue("P_PRODUCT_CODE", command.productCode().value())
                .addValue("P_QUANTITY", command.quantity())
                .addValue("P_UNIT_PRICE", command.unitPrice().amount());

        Map<String, Object> out = call.execute(params);

        String saleReference = asStringOrDefault(out.get("P_SALE_REFERENCE"), "SV-" + UUID.randomUUID());
        long grossTotal = asLongOrDefault(out.get("P_GROSS_TOTAL"), command.grossTotalClp());
        long discountClp = asLongOrDefault(out.get("P_DISCOUNT_CLP"), 0L);
        long netTotal = asLongOrDefault(out.get("P_NET_TOTAL"), Math.max(0L, grossTotal - discountClp));

        return new SaleResult(
                saleReference,
                command.customerId(),
                command.productCode(),
                command.quantity(),
                grossTotal,
                discountClp,
                netTotal,
                Instant.now()
        );
    }

    @Override
    public LiquidationResult liquidateCustomers(LocalDate processDate) {
        LocalDate effectiveDate = processDate != null ? processDate : LocalDate.now();
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE_NAME)
                .withProcedureName(PROC_LIQUIDATE_CUSTOMERS)
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_PROCESS_DATE", Types.DATE),
                        new SqlOutParameter("P_PROCESSED_CUSTOMERS", Types.NUMERIC),
                        new SqlOutParameter("P_TOTAL_LIQUIDATED", Types.NUMERIC),
                        new SqlOutParameter("P_STATUS_MESSAGE", Types.VARCHAR)
                );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("P_PROCESS_DATE", Date.valueOf(effectiveDate));

        Map<String, Object> out = call.execute(params);

        int processedCustomers = (int) asLongOrDefault(out.get("P_PROCESSED_CUSTOMERS"), 0L);
        long totalLiquidated = asLongOrDefault(out.get("P_TOTAL_LIQUIDATED"), 0L);
        String statusMessage = asStringOrDefault(out.get("P_STATUS_MESSAGE"), "Customer liquidation completed");

        return new LiquidationResult(processedCustomers, totalLiquidated, effectiveDate, statusMessage);
    }

    @Override
    public long calculateDiscount(String customerId, long grossAmountClp) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE_NAME)
                .withFunctionName(FUNC_CALCULATE_DISCOUNT)
                .withoutProcedureColumnMetaDataAccess()
                .withReturnValue()
                .declareParameters(
                        new SqlOutParameter("RETURN", Types.NUMERIC),
                        new SqlParameter("P_CUSTOMER_ID", Types.VARCHAR),
                        new SqlParameter("P_GROSS_AMOUNT", Types.NUMERIC)
                );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("P_CUSTOMER_ID", customerId)
                .addValue("P_GROSS_AMOUNT", grossAmountClp);

        BigDecimal discount = call.executeFunction(BigDecimal.class, params);
        return discount != null ? discount.longValue() : 0L;
    }

    private static long asLongOrDefault(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return fallback;
    }

    private static String asStringOrDefault(Object value, String fallback) {
        if (value instanceof String str && !str.isBlank()) {
            return str.trim();
        }
        return fallback;
    }
}
