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

    private static final String DERBY_CLIENT_PERSISTENCE_UNIT = "derby-client-cinema";
    private static final String DERBY_EMBEDDED_PERSISTENCE_UNIT = "derby-inmemory-cinema";

    @Bean(name = "entityManagerFactory")
    @Profile("default")
    public LocalEntityManagerFactoryBean createEntityManagerFactory() {
        LocalEntityManagerFactoryBean factory = new LocalEntityManagerFactoryBean();
        factory.setPersistenceUnitName(DERBY_EMBEDDED_PERSISTENCE_UNIT);
        return factory;
    }
}
