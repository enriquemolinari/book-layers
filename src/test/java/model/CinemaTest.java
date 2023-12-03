package model;

import static model.ForTests.SUPER_MOVIE_ACTOR_CARLOS;
import static model.ForTests.SUPER_MOVIE_DIRECTOR_NAME;
import static model.ForTests.SUPER_MOVIE_NAME;
import static model.ForTests.SUPER_MOVIE_PLOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.api.AuthException;
import model.api.BusinessException;
import model.api.MovieInfo;
import model.api.Seat;

public class CinemaTest {

	private static final String JOSEUSER_SURNAME = "aSurname";
	private static final String JOSEUSER_NAME = "Jose";
	private static final String JOSEUSER_PASS = "password12345679";
	private static final String JOSEUSER_EMAIL = "jose@bla.com";
	private static final YearMonth JOSEUSER_CREDIT_CARD_EXPIRITY = YearMonth.of(
			LocalDateTime.now().getYear(),
			LocalDateTime.now().plusMonths(2).getMonth());
	private static final String JOSEUSER_CREDIT_CARD_SEC_CODE = "145";
	private static final String JOSEUSER_CREDIT_CARD_NUMBER = "123-456-789";
	private static final String JOSEUSER_USERNAME = "joseuser";
	private static final String ANTONIOUSER_USERNAME = "antonio";
	private final ForTests tests = new ForTests();

	private static EntityManagerFactory emf;

	@BeforeEach
	public void setUp() {
		emf = Persistence.createEntityManagerFactory("test-derby-cinema");
	}

	@Test
	public void aShowIsPlayingAt() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);

		long theaterId = createATheater(cinema);

		cinema.addNewShowFor(movieInfo.id(),
				LocalDateTime.of(2024, 10, 10, 13, 30), 10f, theaterId, 20);

		var movieShows = cinema
				.showsUntil(LocalDateTime.of(2024, 10, 10, 13, 31));

		assertEquals(1, movieShows.size());
		assertEquals("1hr 49mins", movieShows.get(0).duration());
		assertEquals(1, movieShows.get(0).shows().size());
		assertTrue(movieShows.get(0).shows().get(0).price() == 10f);
		assertTrue(movieShows.get(0).movieName()
				.equals(SUPER_MOVIE_NAME));
	}

	@Test
	public void reserveSeats() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);

		long theaterId = createATheater(cinema);

		var showInfo = cinema.addNewShowFor(movieInfo.id(),
				LocalDateTime.of(2024, 10, 10, 13, 30), 10f, theaterId, 20);

		var userId = registerAUser(cinema);

		var info = cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

		assertTrue(info.currentSeats().contains(new Seat(1, false)));
		assertTrue(info.currentSeats().contains(new Seat(2, true)));
		assertTrue(info.currentSeats().contains(new Seat(3, true)));
		assertTrue(info.currentSeats().contains(new Seat(4, true)));
		assertTrue(info.currentSeats().contains(new Seat(5, false)));
	}

	@Test
	public void retrieveShow() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);

		long theaterId = createATheater(cinema);

		var showInfo = cinema.addNewShowFor(movieInfo.id(),
				LocalDateTime.of(2024, 10, 10, 13, 30), 10f, theaterId, 20);

		var userId = registerAUser(cinema);

		cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

		var info = cinema.show(showInfo.showId());

		assertTrue(info.info().movieName().equals(SUPER_MOVIE_NAME));
		assertTrue(info.info().movieDuration().equals("1hr 49mins"));
		assertTrue(info.currentSeats().contains(new Seat(1, false)));
		assertTrue(info.currentSeats().contains(new Seat(2, true)));
		assertTrue(info.currentSeats().contains(new Seat(3, true)));
		assertTrue(info.currentSeats().contains(new Seat(4, true)));
		assertTrue(info.currentSeats().contains(new Seat(5, false)));
	}

	@Test
	public void reserveAlreadReservedSeats() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);
		long theaterId = createATheater(cinema);

		var showInfo = cinema.addNewShowFor(movieInfo.id(),
				LocalDateTime.of(2024, 10, 10, 13, 30), 10f, theaterId, 20);

		var userId = registerAUser(cinema);
		var joseId = registerUserJose(cinema);

		cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

		var e = assertThrows(BusinessException.class, () -> {
			cinema.reserve(joseId, showInfo.showId(), Set.of(1, 4, 3));
			fail("I have reserved an already reserved seat");
		});

		assertEquals(ShowTime.SELECTED_SEATS_ARE_BUSY, e.getMessage());
	}

	@Test
	public void loginOk() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());
		registerUserJose(cinema);

		var token = cinema.login(JOSEUSER_USERNAME, JOSEUSER_PASS);

		assertEquals("aToken", token);
	}

	@Test
	public void loginFail() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());
		registerUserJose(cinema);

		var e = assertThrows(AuthException.class, () -> {
			cinema.login(JOSEUSER_USERNAME, "wrongPassword");
			fail("A user has logged in with a wrong password");
		});

		assertEquals(Cinema.USER_OR_PASSWORD_ERROR, e.getMessage());
	}

	@Test
	public void registerAUserNameTwice() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());
		registerUserJose(cinema);

		var e = assertThrows(BusinessException.class, () -> {
			registerUserJose(cinema);
			fail("I have registered the same userName twice");
		});

		assertEquals(Cinema.USER_NAME_ALREADY_EXISTS, e.getMessage());
	}

	@Test
	public void confirmAndPaySeats() {
		var fakePaymenentProvider = tests.fakePaymenentProvider();
		var fakeEmailProvider = tests.fakeEmailProvider();

		var cinema = new Cinema(emf, fakePaymenentProvider, fakeEmailProvider,
				tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);
		long theaterId = createATheater(cinema);

		var showInfo = cinema.addNewShowFor(movieInfo.id(),
				LocalDateTime.of(2024, 10, 10, 13, 30), 10f, theaterId, 20);

		var joseId = registerUserJose(cinema);

		cinema.reserve(joseId, showInfo.showId(), Set.of(1, 5));

		var ticket = cinema.pay(joseId, showInfo.showId(), Set.of(1, 5),
				JOSEUSER_CREDIT_CARD_NUMBER,
				JOSEUSER_CREDIT_CARD_EXPIRITY,
				JOSEUSER_CREDIT_CARD_SEC_CODE);

		assertTrue(ticket.hasSeats(Set.of(1, 5)));
		assertTrue(ticket.isPurchaserUserName(JOSEUSER_USERNAME));
		assertTrue(fakePaymenentProvider.hasBeanCalledWith(
				JOSEUSER_CREDIT_CARD_NUMBER,
				JOSEUSER_CREDIT_CARD_EXPIRITY, JOSEUSER_CREDIT_CARD_SEC_CODE,
				ticket.total()));
		var emailTemplate = new NewSaleEmailTemplate(ticket.total(),
				JOSEUSER_USERNAME, Set.of(1, 5), SUPER_MOVIE_NAME,
				new DayTimeFormatted(LocalDateTime.of(2024, 10, 10, 13, 30))
						.toString());
		assertTrue(fakeEmailProvider.hasBeanCalledWith(JOSEUSER_EMAIL,
				emailTemplate.subject(), emailTemplate.body()));
	}

	@Test
	public void rateMovie() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);

		var joseId = registerUserJose(cinema);

		var userRate = cinema.rateMovieBy(joseId, movieInfo.id(), 4,
				"great movie");

		assertEquals(JOSEUSER_USERNAME, userRate.username());
		assertEquals(4, userRate.rateValue());
	}

	@Test
	public void retrieveRatesInvalidPageNumber() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(),
				10 /* page size */);
		var e = assertThrows(BusinessException.class, () -> {
			cinema.pagedRatesOfOrderedDate(1L, 0);
		});

		assertEquals(Cinema.PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO,
				e.getMessage());
	}

	@Test
	public void retrievePagedRatesFromMovie() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(),
				2 /* page size */);

		var movieInfo = tests.createSuperMovie(cinema);

		var joseId = registerUserJose(cinema);
		var userId = registerAUser(cinema);
		var antonioId = registerUserAntonio(cinema);

		cinema.rateMovieBy(userId, movieInfo.id(), 1, "very bad movie");
		cinema.rateMovieBy(joseId, movieInfo.id(), 2, "bad movie");
		cinema.rateMovieBy(antonioId, movieInfo.id(), 3, "regular movie");

		var userRates = cinema.pagedRatesOfOrderedDate(movieInfo.id(), 1);

		assertEquals(2, userRates.size());
		assertEquals(ANTONIOUSER_USERNAME, userRates.get(0).username());
		assertEquals(JOSEUSER_USERNAME, userRates.get(1).username());
	}

	@Test
	public void retrieveAllPagedRates() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(),
				2 /* page size */);

		var superMovieInfo = tests.createSuperMovie(cinema);
		var otherMovieInfo = tests.createOtherSuperMovie(cinema);

		var joseId = registerUserJose(cinema);

		cinema.rateMovieBy(joseId, superMovieInfo.id(), 1, "very bad movie");
		cinema.rateMovieBy(joseId, otherMovieInfo.id(), 3, "fine movie");

		var movies = cinema.pagedMoviesSortedByRate(1);

		assertEquals(2, movies.size());
		assertEquals(ForTests.OTHER_SUPER_MOVIE_NAME, movies.get(0).name());
		assertEquals(ForTests.SUPER_MOVIE_NAME, movies.get(1).name());
	}

	@Test
	public void rateTheSameMovieTwice() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var movieInfo = tests.createSuperMovie(cinema);
		var joseId = registerUserJose(cinema);

		cinema.rateMovieBy(joseId, movieInfo.id(), 4, "great movie");

		var e = assertThrows(BusinessException.class, () -> {
			cinema.rateMovieBy(joseId, movieInfo.id(), 4, "great movie");
			fail("I was able to rate the same movie twice");
		});

		assertEquals(Cinema.USER_HAS_ALREADY_RATE, e.getMessage());
	}

	@Test
	public void retrieveMovie() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken());

		var superMovie = tests.createSuperMovie(cinema);

		MovieInfo movie = cinema.movie(superMovie.id());

		assertTrue(movie.actors().size() == 2);
		assertTrue(movie.directorNames().size() == 1);
		assertTrue(movie.directorNames().get(0)
				.equals(SUPER_MOVIE_DIRECTOR_NAME));
		assertTrue(movie.actors()
				.contains(SUPER_MOVIE_ACTOR_CARLOS));
		assertTrue(movie.name().equals(SUPER_MOVIE_NAME));
		assertTrue(movie.plot().equals(SUPER_MOVIE_PLOT));
	}

	@Test
	public void moviesSortedByReleaseDate() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(), 1);

		tests.createSuperMovie(cinema);
		tests.createOtherSuperMovie(cinema);

		var movies = cinema.pagedMoviesSortedByReleaseDate(1);

		assertEquals(1, movies.size());
		assertTrue(
				movies.get(0).name().equals(ForTests.SUPER_MOVIE_NAME));
	}

	@Test
	public void retrieveAllMovies() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(), 1);

		tests.createSuperMovie(cinema);
		tests.createOtherSuperMovie(cinema);

		var movies1 = cinema.pagedMoviesSortedByName(1);

		assertEquals(1, movies1.size());
		assertTrue(movies1.get(0).name().equals(SUPER_MOVIE_NAME));
		assertEquals(2, movies1.get(0).genres().size());
		assertEquals(2, movies1.get(0).actors().size());

		var movies2 = cinema.pagedMoviesSortedByName(2);

		assertEquals(1, movies2.size());
		assertTrue(
				movies2.get(0).name().equals(ForTests.OTHER_SUPER_MOVIE_NAME));
		assertEquals(2, movies2.get(0).genres().size());
		assertEquals(1, movies2.get(0).actors().size());
	}

	@Test
	public void searchMovieByName() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(), 10);

		tests.createSuperMovie(cinema);
		tests.createOtherSuperMovie(cinema);

		var movies = cinema.pagedSearchMovieByName("another", 1);

		assertEquals(1, movies.size());
		assertTrue(
				movies.get(0).name().equals(ForTests.OTHER_SUPER_MOVIE_NAME));
	}

	@Test
	public void searchMovieByNameNotFound() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(), 10);

		tests.createSuperMovie(cinema);
		tests.createOtherSuperMovie(cinema);

		var movies = cinema.pagedSearchMovieByName("not_found_movie", 1);

		assertEquals(0, movies.size());
	}

	@Test
	public void userProfileFrom() {
		var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
				tests.doNothingEmailProvider(), tests.doNothingToken(), 10);
		var userId = registerUserJose(cinema);

		var profile = cinema.profileFrom(userId);
		assertEquals(JOSEUSER_USERNAME, profile.username());
		assertEquals(JOSEUSER_EMAIL, profile.email());
		assertEquals(JOSEUSER_NAME + " " + JOSEUSER_SURNAME,
				profile.fullname());

	}

	private Long registerUserJose(Cinema cinema) {
		var joseId = cinema.registerUser(JOSEUSER_NAME, JOSEUSER_SURNAME,
				JOSEUSER_EMAIL,
				JOSEUSER_USERNAME,
				JOSEUSER_PASS, JOSEUSER_PASS);
		return joseId;
	}

	private Long registerUserAntonio(Cinema cinema) {
		var userId = cinema.registerUser("Antonio", JOSEUSER_SURNAME,
				"antonio@bla.com",
				ANTONIOUSER_USERNAME,
				"password12345678", "password12345678");
		return userId;
	}

	private Long registerAUser(Cinema cinema) {
		var userId = cinema.registerUser("aUser", JOSEUSER_SURNAME,
				"enrique@bla.com",
				"username",
				"password12345678", "password12345678");
		return userId;
	}

	private Long createATheater(Cinema cinema) {
		return cinema.addNewTheater("a Theater",
				Set.of(1, 2, 3, 4, 5, 6));
	}

	@AfterEach
	public void tearDown() {
		emf.close();
	}

}
