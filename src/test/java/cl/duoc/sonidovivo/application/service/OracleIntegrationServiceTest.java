package cl.duoc.sonidovivo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.sonidovivo.domain.exception.CustomerNotFoundException;
import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.domain.model.Category;
import cl.duoc.sonidovivo.domain.model.Customer;
import cl.duoc.sonidovivo.domain.model.EmailAddress;
import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.model.MoneyClp;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductCode;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.domain.model.StockQuantity;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import cl.duoc.sonidovivo.domain.port.out.OracleSalesPort;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OracleIntegrationServiceTest {

    @Mock
    private OracleSalesPort oracleSalesPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private OracleIntegrationService oracleIntegrationService;

    @BeforeEach
    void setUp() {
        oracleIntegrationService = new OracleIntegrationService(
                oracleSalesPort,
                productRepositoryPort,
                customerRepositoryPort
        );
    }

    private Customer sampleCustomer() {
        return Customer.reconstitute(
                "cust-1",
                "12.345.678-K",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@sonidovivo.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );
    }

    private Product sampleProduct(int stock) {
        return Product.create(
                ProductCode.of("G-FND-STRAT-01"),
                "Fender Stratocaster Player",
                Category.CUERDAS,
                MoneyClp.of(800_000),
                StockQuantity.of(stock),
                Map.of("tipo", "guitarra"),
                List.of("fender")
        );
    }

    @Test
    @DisplayName("registerSale decrements MongoDB stock, transitions AGOTADO at zero, and registers sale in Oracle")
    void registerSaleHappyPathAndStatusSync() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        SaleCommand command = new SaleCommand("cust-1", code, 3, MoneyClp.of(800_000));
        SaleResult expected = new SaleResult(
                "VTA-2026-0001",
                "cust-1",
                code,
                3,
                2_400_000L,
                120_000L,
                2_280_000L,
                Instant.now()
        );

        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(sampleCustomer()));
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(sampleProduct(3)));
        when(productRepositoryPort.decrementStockAtomic(code, 3)).thenReturn(true);
        when(oracleSalesPort.registerSale(command)).thenReturn(expected);

        SaleResult result = oracleIntegrationService.registerSale(command);

        assertEquals("VTA-2026-0001", result.saleReference());
        assertEquals(2_280_000L, result.netTotalClp());
        verify(productRepositoryPort).decrementStockAtomic(code, 3);
        verify(productRepositoryPort).updateStatus(code, ProductStatus.AGOTADO);
        verify(oracleSalesPort).registerSale(command);
    }

    @Test
    @DisplayName("registerSale compensates MongoDB stock and status if Oracle PL/SQL fails")
    void registerSaleCompensatesStockWhenOracleFails() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        SaleCommand command = new SaleCommand("cust-1", code, 2, MoneyClp.of(800_000));

        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(sampleCustomer()));
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(sampleProduct(2)));
        when(productRepositoryPort.decrementStockAtomic(code, 2)).thenReturn(true);
        when(oracleSalesPort.registerSale(command)).thenThrow(new IllegalStateException("Oracle ORA-20001"));

        assertThrows(IllegalStateException.class, () -> oracleIntegrationService.registerSale(command));

        verify(productRepositoryPort).updateStatus(code, ProductStatus.AGOTADO);
        verify(productRepositoryPort).adjustStockAtomic(code, 2);
        verify(productRepositoryPort).updateStatus(code, ProductStatus.ACTIVO);
    }

    @Test
    @DisplayName("registerSale rejects unknown customer, unknown product, and insufficient stock before calling Oracle")
    void registerSaleRejectsInvalidPreconditions() {
        ProductCode code = ProductCode.of("G-FND-STRAT-01");
        SaleCommand command = new SaleCommand("cust-1", code, 5, MoneyClp.of(800_000));

        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> oracleIntegrationService.registerSale(command));
        verifyNoInteractions(productRepositoryPort, oracleSalesPort);

        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(sampleCustomer()));
        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> oracleIntegrationService.registerSale(command));
        verifyNoInteractions(oracleSalesPort);

        when(productRepositoryPort.findByCode(code)).thenReturn(Optional.of(sampleProduct(2)));
        assertThrows(InsufficientStockException.class, () -> oracleIntegrationService.registerSale(command));
        verify(productRepositoryPort, never()).decrementStockAtomic(any(), org.mockito.ArgumentMatchers.anyInt());
        verifyNoInteractions(oracleSalesPort);
    }

    @Test
    @DisplayName("runCustomerLiquidation validates date and delegates to OracleSalesPort")
    void runCustomerLiquidationBehavior() {
        LocalDate processDate = LocalDate.of(2026, 11, 11);
        LiquidationResult expected = new LiquidationResult(15, 4_500_000L, processDate, "OK");
        when(oracleSalesPort.liquidateCustomers(processDate)).thenReturn(expected);

        LiquidationResult result = oracleIntegrationService.runCustomerLiquidation(processDate);

        assertEquals(15, result.processedCustomers());
        assertEquals(4_500_000L, result.totalLiquidatedClp());
        assertThrows(InvalidDomainDataException.class, () -> oracleIntegrationService.runCustomerLiquidation(null));
    }

    @Test
    @DisplayName("calculateDiscount validates customer and gross amount before calling Oracle FN_CALCULAR_DESCUENTO")
    void calculateDiscountBehavior() {
        when(customerRepositoryPort.findById("cust-1")).thenReturn(Optional.of(sampleCustomer()));
        when(oracleSalesPort.calculateDiscount("cust-1", 500_000L)).thenReturn(50_000L);

        long discount = oracleIntegrationService.calculateDiscount(" cust-1 ", 500_000L);

        assertEquals(50_000L, discount);
        assertThrows(InvalidDomainDataException.class,
                () -> oracleIntegrationService.calculateDiscount(" ", 500_000L));
        assertThrows(InvalidDomainDataException.class,
                () -> oracleIntegrationService.calculateDiscount("cust-1", 0L));
    }
}
