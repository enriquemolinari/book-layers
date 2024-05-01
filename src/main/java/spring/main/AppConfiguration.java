package spring.main;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import services.Cinema;
import services.PasetoToken;
import services.PleasePayPaymentProvider;
import services.TheBestEmailProvider;
import services.api.CinemaSystem;

@Configuration
public class AppConfiguration {
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    // this secret should not be here (in source code)
    private static final String SECRET = "nXXh3Xjr2T0ofFilg3kw8BwDEyHmS6OIe4cjWUm2Sm0=";

    @Bean
    @Profile("default")
    public CinemaSystem create() {
        new SetUpDb(entityManagerFactory)
                .createSchemaAndPopulateSampleData();
        return new Cinema(entityManagerFactory, new PleasePayPaymentProvider(),
                new TheBestEmailProvider(),
                new PasetoToken(SECRET), 10 /*
         * page size
         */);
    }

}
