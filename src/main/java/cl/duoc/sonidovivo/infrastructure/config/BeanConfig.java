package cl.duoc.sonidovivo.infrastructure.config;

import cl.duoc.sonidovivo.application.port.in.CustomerUseCase;
import cl.duoc.sonidovivo.application.port.in.DashboardUseCase;
import cl.duoc.sonidovivo.application.port.in.OracleIntegrationUseCase;
import cl.duoc.sonidovivo.application.port.in.ProductUseCase;
import cl.duoc.sonidovivo.application.service.CustomerService;
import cl.duoc.sonidovivo.application.service.DashboardService;
import cl.duoc.sonidovivo.application.service.OracleIntegrationService;
import cl.duoc.sonidovivo.application.service.ProductService;
import cl.duoc.sonidovivo.domain.port.out.CustomerRepositoryPort;
import cl.duoc.sonidovivo.domain.port.out.DashboardMetricsPort;
import cl.duoc.sonidovivo.domain.port.out.OracleSalesPort;
import cl.duoc.sonidovivo.domain.port.out.ProductRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration wiring pure Java application services with outbound persistence adapters.
 */
@Configuration
public class BeanConfig {

    @Bean
    public ProductUseCase productUseCase(ProductRepositoryPort productRepositoryPort) {
        return new ProductService(productRepositoryPort);
    }

    @Bean
    public CustomerUseCase customerUseCase(CustomerRepositoryPort customerRepositoryPort) {
        return new CustomerService(customerRepositoryPort);
    }

    @Bean
    public DashboardUseCase dashboardUseCase(
            DashboardMetricsPort dashboardMetricsPort,
            ProductRepositoryPort productRepositoryPort
    ) {
        return new DashboardService(dashboardMetricsPort, productRepositoryPort);
    }

    @Bean
    public OracleIntegrationUseCase oracleIntegrationUseCase(
            OracleSalesPort oracleSalesPort,
            ProductRepositoryPort productRepositoryPort,
            CustomerRepositoryPort customerRepositoryPort
    ) {
        return new OracleIntegrationService(
                oracleSalesPort,
                productRepositoryPort,
                customerRepositoryPort
        );
    }
}
