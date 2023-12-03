package spring.main;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

/**
 * This is required to use the persistence.xml
 */
@Configuration
public class PersistenceConfiguration {

	private static final String PROD_PERSISTENCE_UNIT = "derby-cinema";
	private static final String TEST_PERSISTENCE_UNIT = "test-derby-cinema";

	@Bean(name = "entityManagerFactory")
	@Profile("default")
	public LocalEntityManagerFactoryBean createEntityManagerFactory() {
		LocalEntityManagerFactoryBean factory = new LocalEntityManagerFactoryBean();
		factory.setPersistenceUnitName(PROD_PERSISTENCE_UNIT);
		return factory;
	}

	@Bean(name = "entityManagerFactory")
	@Profile("test")
	public LocalEntityManagerFactoryBean createTestEntityManagerFactory() {
		LocalEntityManagerFactoryBean factory = new LocalEntityManagerFactoryBean();
		factory.setPersistenceUnitName(TEST_PERSISTENCE_UNIT);
		return factory;
	}
}
