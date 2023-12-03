package javalin.main;

import jakarta.persistence.Persistence;
import javalin.web.CinemaSystemController;
import model.Cinema;
import model.mail.TheBestEmailProvider;
import model.payment.PleasePayPaymentProvider;
import model.token.PasetoToken;
import spring.main.SetUpDb;

public class Main {

	public static void main(String[] args) {
		// this secret should not be here
		String SECRET = "nXXh3Xjr2T0ofFilg3kw8BwDEyHmS6OIe4cjWUm2Sm0=";

		var emf = Persistence
				.createEntityManagerFactory("derby-cinema");

		new SetUpDb(emf).createSchemaAndPopulateSampleData();

		var cinema = new Cinema(emf, new PleasePayPaymentProvider(),
				new TheBestEmailProvider(),
				new PasetoToken(SECRET), 10 /* page size */);

		new CinemaSystemController(8080, cinema).start();
	}
}
