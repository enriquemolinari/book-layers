package spring.main;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import services.EmfBuilder;

/**
 * This is required to use the persistence.xml
 */
@Configuration
public class PersistenceConfiguration {
    
    @Bean
    @Profile("default")
    public EntityManagerFactory createEmf() {
        return new EmfBuilder()
                .memory()
                .withDropAndCreateDDL()
                .build();
    }
}
