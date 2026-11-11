package cl.duoc.sonidovivo.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerAndMetricsTest {

    @Test
    @DisplayName("Customer validates Chilean RUT, contact fields, and profile updates")
    void customerInvariants() {
        Customer customer = Customer.create(
                "12.345.678-k",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@duoc.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        );

        assertEquals("12.345.678-K", customer.getRut());
        assertEquals("Victor Martinez", customer.getFullName());

        customer.updateProfile(
                "Victor Manuel",
                "Martinez",
                EmailAddress.of("vmartinez@duoc.cl"),
                "+56987654321",
                "Providencia",
                "Metropolitana"
        );
        assertEquals("Victor Manuel", customer.getFirstName());
        assertEquals("Providencia", customer.getCommune());

        assertThrows(InvalidDomainDataException.class, () -> Customer.create(
                "invalid-rut",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@duoc.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        ));

        assertThrows(InvalidDomainDataException.class, () -> Customer.reconstitute(
                " ",
                "12345678-9",
                "Victor",
                "Martinez",
                EmailAddress.of("victor@duoc.cl"),
                "+56912345678",
                "Santiago",
                "Metropolitana"
        ));
    }

    @Test
    @DisplayName("DashboardMetrics aggregates category KPIs accurately")
    void dashboardMetricsAggregation() {
        List<CategoryMetric> categories = List.of(
                new CategoryMetric("Cuerdas", 10, 5_000_000L, 500_000.0),
                new CategoryMetric("Audio Pro", 4, 2_000_000L, 500_000.0)
        );
        DashboardMetrics metrics = DashboardMetrics.fromCategories(categories);

        assertEquals(7_000_000L, metrics.totalWarehouseValueClp());
        assertEquals(14, metrics.totalUnits());
        assertEquals(2, metrics.activeCategories());
        assertThrows(UnsupportedOperationException.class, () -> metrics.categories().clear());
        assertThrows(InvalidDomainDataException.class, () -> new CategoryMetric("Cuerdas", -1, 0, 0));
    }

    @Test
    @DisplayName("Oracle sale and liquidation records validate domain invariants")
    void oracleSaleAndLiquidationRecords() {
        SaleCommand command = new SaleCommand(
                "CUST-01",
                ProductCode.of("G-FND-STRAT-01"),
                2,
                MoneyClp.of(450_000)
        );
        assertEquals(900_000L, command.grossTotalClp());

        assertThrows(InvalidDomainDataException.class, () -> new SaleCommand(
                " ",
                ProductCode.of("G-FND-STRAT-01"),
                2,
                MoneyClp.of(450_000)
        ));
        assertThrows(InvalidDomainDataException.class, () -> new SaleCommand(
                "CUST-01",
                ProductCode.of("G-FND-STRAT-01"),
                0,
                MoneyClp.of(450_000)
        ));

        SaleResult result = new SaleResult(
                "VTA-0001",
                "CUST-01",
                ProductCode.of("G-FND-STRAT-01"),
                2,
                900_000L,
                45_000L,
                855_000L,
                Instant.now()
        );
        assertEquals("VTA-0001", result.saleReference());

        LiquidationResult liquidation = new LiquidationResult(
                12,
                4_200_000L,
                LocalDate.now(),
                "OK"
        );
        assertEquals(12, liquidation.processedCustomers());
        assertThrows(InvalidDomainDataException.class, () -> new LiquidationResult(-1, 0L, LocalDate.now(), "OK"));
    }
}
