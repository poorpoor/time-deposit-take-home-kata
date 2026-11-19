package org.ikigaidigital.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("org.ikigaidigital");

    @Test
    @DisplayName("Domain layer stays pure Java without Spring, JPA, Jackson, application, or infrastructure dependencies")
    void domainLayerRemainsPureJava() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .or().haveFullyQualifiedName(TimeDeposit.class.getName())
            .or().haveFullyQualifiedName(TimeDepositCalculator.class.getName())
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson..",
                "org.ikigaidigital.application..",
                "org.ikigaidigital.infrastructure.."
            )
            .check(PRODUCTION_CLASSES);
    }

    @Test
    @DisplayName("Application layer does not depend on infrastructure or JPA")
    void applicationLayerDoesNotDependOnInfrastructure() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.ikigaidigital.infrastructure..",
                "jakarta.persistence.."
            )
            .check(PRODUCTION_CLASSES);
    }

    @Test
    @DisplayName("Inbound web adapter communicates only through inbound ports, never services or persistence")
    void webAdapterDependsOnlyOnInboundPorts() {
        noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.ikigaidigital.infrastructure.adapter.out.persistence..",
                "org.ikigaidigital.application.service..",
                "org.ikigaidigital.application.port.out.."
            )
            .check(PRODUCTION_CLASSES);
    }

    @Test
    @DisplayName("Outbound persistence adapter does not depend on web adapter or application services")
    void persistenceAdapterDoesNotDependOnWebOrServices() {
        noClasses()
            .that().resideInAPackage("..infrastructure.adapter.out.persistence..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.ikigaidigital.infrastructure.adapter.in.web..",
                "org.ikigaidigital.application.service..",
                "org.ikigaidigital.application.port.in.."
            )
            .check(PRODUCTION_CLASSES);
    }

    @Test
    @DisplayName("Application ports are interfaces")
    void applicationPortsAreInterfaces() {
        classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces()
            .check(PRODUCTION_CLASSES);
    }
}
