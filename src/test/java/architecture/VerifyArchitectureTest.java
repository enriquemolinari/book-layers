package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class VerifyArchitectureTest {

	@Test
	public void modelShouldOnlyDependOnModelApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("model..", "spring..", "main");
		classes().that().resideInAPackage("model").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("model.api", "model", "java..", "javax..",
						"lombok..",
						"jakarta..")
				.check(importedClasses);
	}

	@Test
	public void modelMailShouldOnlyDependOnModelApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("model..", "spring..", "main");
		classes().that().resideInAPackage("model.mail").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("model.api", "model.mail", "java..",
						"javax..")
				.check(importedClasses);
	}

	@Test
	public void modelTokenShouldOnlyDependOnModelApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("model..", "spring..", "main");
		classes().that().resideInAPackage("model.token").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("model.api", "model.token", "java..",
						"javax..", "dev.paseto..")
				.check(importedClasses);
	}

	@Test
	public void modelPaymentShouldOnlyDependOnModelApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("model..", "spring..", "main");
		classes().that().resideInAPackage("model.payment").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("model.api", "model.payment", "java..",
						"javax..")
				.check(importedClasses);
	}

	@Test
	public void webPackagesOutSideModelShouldOnlyDependOnModelApi() {
		JavaClasses importedClasses = new ClassFileImporter().withImportOption(
				new com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests())
				.importPackages("model..", "spring..", "main");
		classes().that().resideInAPackage("spring.web").should()
				.onlyDependOnClassesThat()
				.resideInAnyPackage("model.api", "spring.web", "java..",
						"javax..",
						"org.springframework..")
				.check(importedClasses);
	}
}
