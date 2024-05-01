package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class VerifyArchitectureTest {

    @Test
    public void dataLayerShouldOnlyDependOnJava() {
        JavaClasses importedClasses = new ClassFileImporter().withImportOption(
                        new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
                .importPackages("data..", "services..", "spring..", "main");
        classes().that().resideInAPackage("data..").should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("java..",
                        "javax..",
                        "lombok..",
                        "jakarta..", "data..")
                .check(importedClasses);
    }

    @Test
    public void servicesShouldOnlyDependOnServicesApiAndData() {
        JavaClasses importedClasses = new ClassFileImporter().withImportOption(
                        new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
                .importPackages("data..", "services..", "spring..", "main");
        classes().that().resideInAPackage("services").should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("services.api", "services", "java..",
                        "javax..", "jakarta..", "data..", "dev.paseto..")
                .check(importedClasses);
    }
	
    @Test
    public void webPackageShouldOnlyDependOnServicesApi() {
        JavaClasses importedClasses = new ClassFileImporter().withImportOption(
                        new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
                .importPackages("services..", "spring..", "main");
        classes().that().resideInAPackage("spring.web").should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("services.api", "spring.web", "java..",
                        "javax..",
                        "org.springframework..")
                .check(importedClasses);
    }
}
