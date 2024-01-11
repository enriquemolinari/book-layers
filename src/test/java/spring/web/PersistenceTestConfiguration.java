package spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

@Configuration
public class PersistenceTestConfiguration {
    private static final String TEST_PERSISTENCE_UNIT = "test-derby-cinema";

    @Bean(name = "entityManagerFactory")
    @Profile("test")
    public LocalEntityManagerFactoryBean createEntityManagerFactory() {
        LocalEntityManagerFactoryBean factory = new LocalEntityManagerFactoryBean();
        factory.setPersistenceUnitName(TEST_PERSISTENCE_UNIT);
        return factory;
    }
}
