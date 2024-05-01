package spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

@Configuration
public class PersistenceTestConfiguration {
    private static final String DERBY_EMBEDDED_PERSISTENCE_UNIT = "derby-inmemory-cinema";

    @Bean(name = "entityManagerFactory")
    @Profile("test")
    public LocalEntityManagerFactoryBean createEntityManagerFactory() {
        LocalEntityManagerFactoryBean factory = new LocalEntityManagerFactoryBean();
        factory.setPersistenceUnitName(DERBY_EMBEDDED_PERSISTENCE_UNIT);
        return factory;
    }
}
