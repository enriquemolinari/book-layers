package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

@Disabled
// TODO: enable
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
						"javax..",
						"data..")
				.check(importedClasses);
	}

	@Test
	public void servicesMailShouldOnlyDependOnServicesApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("data..", "services..", "spring..", "main");
		classes().that().resideInAPackage("services.mail").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("services.api", "services.mail", "java..",
						"javax..")
				.check(importedClasses);
	}

	@Test
	public void servicesTokenShouldOnlyDependOnServicesApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("data..", "services..", "spring..", "main");
		classes().that().resideInAPackage("services.token").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("services.api", "services.token", "java..",
						"javax..", "dev.paseto..")
				.check(importedClasses);
	}

	@Test
	public void servicesPaymentShouldOnlyDependOnservicesApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("data..", "services..", "spring..", "main");
		classes().that().resideInAPackage("services.payment").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("data..", "services.api",
						"services.payment",
						"java..",
						"javax..")
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
