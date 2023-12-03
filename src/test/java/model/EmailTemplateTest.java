package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class EmailTemplateTest {

	@Test
	public void emailTemplateGeneratedOk() {
		var e = new NewSaleEmailTemplate(100.9f, "emolinari",
				Set.of(2, 6, 1, 9, 3), "movie name", "08-04-2024 05:30");

		var body = new StringBuilder();
		body.append("Hello emolinari!");
		body.append(System.lineSeparator());
		body.append("You have new tickets!");
		body.append(System.lineSeparator());
		body.append("Here are the details of your booking: ");
		body.append(System.lineSeparator());
		body.append("Movie: movie name");
		body.append(System.lineSeparator());
		body.append("Seats: 1,2,3,6,9");
		body.append(System.lineSeparator());
		body.append("Show time: 08-04-2024 05:30");
		body.append(System.lineSeparator());
		body.append("Total paid: 100.9");
		assertEquals(body.toString(), e.body());
	}
}
