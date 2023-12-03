package spring.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import model.Actor;
import model.Movie;
import model.Person;
import model.ShowTime;
import model.Theater;
import model.User;
import model.api.DateTimeProvider;
import model.api.Genre;

public class SetUpDb {

	private EntityManagerFactory emf;

	public SetUpDb(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public void createSchemaAndPopulateSampleData() {
		var em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();

			var jake = new Person("Jake", "White", "jake@mymovies.com");
			var josh = new Person("Josh", "Blue", "josh@mymovies.com");
			var nervan = new Person("Nervan", "Allister",
					"nervan@mymovies.com");
			var ernest = new Person("Ernest", "Finey", "ernest@mymovies.com");
			var enrique = new Person("Enrique", "Molinari",
					"enrique.molinari@gmail.com");
			var josefina = new Person("Josefina", "Simini",
					"jsimini@mymovies.com");
			var lucia = new Person("Lucia", "Molimini", "lu@mymovies.com");
			var nico = new Person("Nicolas", "Molimini", "nico@mymovies.com");
			var camilo = new Person("Camilo", "Fernandez", "cami@mymovies.com");
			var franco = new Person("Franco", "Elchow", "franco@mymovies.com");
			var michael = new Person("Michael", "Martinez",
					"michael@mymovies.com");
			var michell = new Person("Michell", "Orenson",
					"michell@mymovies.com");
			var craigDirector = new Person("Christopher", "Wegemen",
					"craig@mymovies.com");
			var judithDirector = new Person("Jude", "Zevele",
					"judith@mymovies.com");
			var andreDirector = new Person("Andres", "Lembert",
					"andre@mymovies.com");
			var colinDirector = new Person("Colin", "Clefferd",
					"andre@mymovies.com");

			var jakeActor = new Actor(jake, "Daniel Finne");
			var jakeActor2 = new Actor(jake, "Camilo Fernis");
			var joshActor = new Actor(josh, "Norber Carl");
			var ernestActor = new Actor(ernest, "Edward Blomsky (senior)");
			var nervanActor = new Actor(nervan, "Edward Blomsky (young)");
			var camiloActor = new Actor(camilo, "Judy");
			var francoActor = new Actor(franco, "George");
			var michaelActor = new Actor(michael, "Mike");
			var michellActor = new Actor(michell, "Teressa");

			var schoolMovie = new Movie("Rock in the School",
					"A teacher tries to teach Rock & Roll music and history "
							+ "to elementary school kids",
					109, LocalDate.now(), Set.of(Genre.COMEDY, Genre.ACTION),
					List.of(jakeActor, joshActor), List.of(colinDirector));
			var eu = new User(enrique, "emolinari", "123456789012",
					"123456789012");
			em.persist(eu);

			var nu = new User(nico, "nico", "123456789012", "123456789012");
			var lu = new User(lucia, "lucia", "123456789012", "123456789012");

			em.persist(nu);
			em.persist(lu);

			schoolMovie.rateBy(eu, 5, "Great Movie");
			schoolMovie.rateBy(nu, 5,
					"Fantastic! The actors, the music, everything is fantastic!");
			schoolMovie.rateBy(lu, 4, "I really enjoy the movie");

			em.persist(schoolMovie);

			var fishMovie = new Movie("Small Fish",
					"A caring father teaches life values while fishing.", 125,
					LocalDate.now().minusDays(1),
					Set.of(Genre.ADVENTURE, Genre.DRAMA),
					List.of(jakeActor2, ernestActor, nervanActor),
					List.of(andreDirector));

			fishMovie.rateBy(eu, 4, "Fantastic !!");

			em.persist(fishMovie);

			var ju = new User(josefina, "jsimini", "123456789012",
					"123456789012");
			em.persist(ju);

			var teaMovie = new Movie("Crash Tea", "A documentary about tea.",
					105, LocalDate.now().minusDays(3), Set.of(Genre.COMEDY),
					List.of(michaelActor, michellActor),
					List.of(judithDirector, craigDirector));
			em.persist(teaMovie);

			var runningMovie = new Movie("Running far Away",
					"José a sad person run away from his town looking for new adventures.",
					105, LocalDate.now(), Set.of(Genre.THRILLER, Genre.ACTION),
					List.of(francoActor, camiloActor), List.of(judithDirector));
			em.persist(runningMovie);

			// Seats from Theatre A
			Set<Integer> seatsA = new HashSet<>();
			for (int i = 1; i <= 30; i++) {
				seatsA.add(i);
			}

			var ta = new Theater("Theatre A", seatsA, null);

			em.persist(ta);
			em.flush();

			// Seats from Theatre B
			Set<Integer> seatsB = new HashSet<>();
			for (int i = 1; i <= 50; i++) {
				seatsB.add(i);
			}

			var tb = new Theater("Theatre B", seatsB, null);

			em.persist(tb);
			em.flush();

			var show1 = new ShowTime(DateTimeProvider.create(), fishMovie,
					LocalDateTime.now().plusDays(1), 10f, ta);
			em.persist(show1);

			var show2 = new ShowTime(DateTimeProvider.create(), fishMovie,
					LocalDateTime.now().plusDays(1).plusHours(4), 10f, ta);
			em.persist(show2);

			var show3 = new ShowTime(DateTimeProvider.create(), schoolMovie,
					LocalDateTime.now().plusDays(2).plusHours(1), 19f, tb);

			em.persist(show3);

			var show4 = new ShowTime(DateTimeProvider.create(), schoolMovie,
					LocalDateTime.now().plusDays(2).plusHours(5), 19f, tb);
			em.persist(show4);

			var show5 = new ShowTime(DateTimeProvider.create(), teaMovie,
					LocalDateTime.now().plusDays(2).plusHours(2), 19f, ta);
			em.persist(show5);

			var show6 = new ShowTime(DateTimeProvider.create(), runningMovie,
					LocalDateTime.now().plusHours(2), 19f, tb);
			em.persist(show6);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}
}
