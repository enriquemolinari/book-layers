package services;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailTemplateTest {
    @Test
    public void emailTemplateGeneratedOk() {
        var e = new NewSaleEmailTemplate(100.9f, "emolinari",
                Set.of(2, 6, 1, 9, 3), "movie name", "08-04-2024 05:30");

        String body = "Hello emolinari!" +
                System.lineSeparator() +
                "You have new tickets!" +
                System.lineSeparator() +
                "Here are the details of your booking: " +
                System.lineSeparator() +
                "Movie: movie name" +
                System.lineSeparator() +
                "Seats: 1,2,3,6,9" +
                System.lineSeparator() +
                "Show time: 08-04-2024 05:30" +
                System.lineSeparator() +
                "Total paid: 100.9";
        assertEquals(body, e.body());
    }
}
