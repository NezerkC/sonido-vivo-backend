package cl.duoc.sonidovivo.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural fitness functions enforcing Hexagonal Architecture boundaries
 * for the Sonido Vivo backend.
 */
@AnalyzeClasses(
        packages = "cl.duoc.sonidovivo",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

    private static final DescribedPredicate<JavaClass> INFRASTRUCTURE_CONTROLLERS =
            resideInAPackage("..infrastructure..")
                    .and(annotatedWith(RestController.class)
                            .or(annotatedWith(Controller.class))
                            .or(simpleNameEndingWith("Controller")));

    @ArchTest
    static final ArchRule domainShouldNotDependOnForbiddenPackages =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "..springframework..",
                            "jakarta..",
                            "..jakarta..",
                            "org.bson..",
                            "..bson..",
                            "com.mongodb..",
                            "..mongodb..",
                            "oracle..",
                            "com.oracle..",
                            "..oracle..",
                            "..application..",
                            "..infrastructure.."
                    )
                    .as("Domain layer must stay framework-agnostic and independent of application/infrastructure");

    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructureOrSpringWeb =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure..",
                            "org.springframework.web..",
                            "..springframework.web.."
                    )
                    .as("Application layer must not depend on infrastructure or Spring Web");

    @ArchTest
    static final ArchRule infrastructureControllersShouldResideInAdapterInRest =
            classes()
                    .that(INFRASTRUCTURE_CONTROLLERS)
                    .should().resideInAPackage("..adapter.in.rest..")
                    .as("Infrastructure controllers must reside inside ..adapter.in.rest..");
}
