package spring.web;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import services.EmfBuilder;

@Configuration
public class PersistenceTestConfiguration {
    @Bean
    @Profile("test")
    public EntityManagerFactory createEmf() {
        return new EmfBuilder()
                .memory()
                .withDropAndCreateDDL()
                .build();
    }
}
