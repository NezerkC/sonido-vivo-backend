package cl.duoc.sonidovivo.application.service;

import cl.duoc.sonidovivo.application.port.in.OracleIntegrationUseCase;
import cl.duoc.sonidovivo.domain.exception.CustomerNotFoundException;
import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.domain.model.Product;
import cl.duoc.sonidovivo.domain.model.ProductStatus;
import cl.duoc.sonidovivo.domain.model.SaleCommand;
import cl.duoc.sonidovivo.domain.model.SaleResult;
import cl.duoc.sonidovivo.domain.model.LiquidationResult;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import cl.duoc.sonidovivo.domain.port.out.OracleSalesPort;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Pure Java application service coordinating polyglot sales between MongoDB inventory
 * and Oracle PL/SQL stored procedures.
 */
public class OracleIntegrationService implements OracleIntegrationUseCase {

    private final OracleSalesPort oracleSalesPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;

    public OracleIntegrationService(
            OracleSalesPort oracleSalesPort,
            ProductRepositoryPort productRepositoryPort,
            CustomerRepositoryPort customerRepositoryPort
    ) {
        this.oracleSalesPort = Objects.requireNonNull(
                oracleSalesPort,
                "OracleSalesPort must not be null"
        );
        this.productRepositoryPort = Objects.requireNonNull(
                productRepositoryPort,
                "ProductRepositoryPort must not be null"
        );
        this.customerRepositoryPort = Objects.requireNonNull(
                customerRepositoryPort,
                "CustomerRepositoryPort must not be null"
        );
    }

    @Override
    public SaleResult registerSale(SaleCommand command) {
        if (command == null) {
            throw new InvalidDomainDataException("SaleCommand must not be null");
        }

        requireExistingCustomer(command.customerId());

        Product product = productRepositoryPort.findByCode(command.productCode())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with code: " + command.productCode().value()
                ));

        ProductStatus previousStatus = product.getStatus();
        product.decrementStock(command.quantity());

        boolean decremented = productRepositoryPort.decrementStockAtomic(
                command.productCode(),
                command.quantity()
        );
        if (!decremented) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + command.productCode().value()
            );
        }

        if (product.getStatus() != previousStatus) {
            productRepositoryPort.updateStatus(command.productCode(), product.getStatus());
        }

        try {
            return oracleSalesPort.registerSale(command);
        } catch (RuntimeException ex) {
            // Compensate MongoDB inventory if Oracle PL/SQL sale registration fails
            productRepositoryPort.adjustStockAtomic(command.productCode(), command.quantity());
            if (product.getStatus() != previousStatus) {
                productRepositoryPort.updateStatus(command.productCode(), previousStatus);
            }
            throw ex;
        }
    }

    @Override
    public LiquidationResult runCustomerLiquidation(LocalDate processDate) {
        if (processDate == null) {
            throw new InvalidDomainDataException("Process date must not be null");
        }
        return oracleSalesPort.liquidateCustomers(processDate);
    }

    @Override
    public long calculateDiscount(String customerId, long grossAmountClp) {
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidDomainDataException("Customer id must not be blank");
        }
        if (grossAmountClp <= 0) {
            throw new InvalidDomainDataException(
                    "Gross amount in CLP must be positive: " + grossAmountClp
            );
        }
        String normalizedId = customerId.trim();
        requireExistingCustomer(normalizedId);
        return oracleSalesPort.calculateDiscount(normalizedId, grossAmountClp);
    }

    private void requireExistingCustomer(String customerId) {
        customerRepositoryPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId
                ));
    }
}
