package spring.web;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import services.Cinema;
import services.PasetoToken;
import services.api.CinemaSystem;
import spring.main.SetUpDb;

import java.time.YearMonth;

@Configuration
public class AppTestConfiguration {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    @Profile("test")
    public CinemaSystem createForTest() {
        String ANY_SECRET = "Kdj5zuBIBBgcWpv9zjKOINl2yUKUXVKO+SkOVE3VuZ4=";
        new SetUpDb(entityManagerFactory)
                .createSchemaAndPopulateSampleData();
        return new Cinema(entityManagerFactory,
                (String creditCardNumber, YearMonth expire, String securityCode,
                 float totalAmount) -> {
                },
                (String to, String subject, String body) -> {
                },
                new PasetoToken(ANY_SECRET),
                2 /*
         * page size
         */);
    }
}
