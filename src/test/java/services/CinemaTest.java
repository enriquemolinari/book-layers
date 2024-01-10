package services;

import data.entities.Password;
import data.services.CinemaRepository;
import data.services.DataException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.api.AuthException;
import services.api.BusinessException;
import services.api.MovieInfo;
import services.api.Seat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static services.ForTests.*;

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
    private static final Long NON_EXISTENT_ID = -2L;
    private final ForTests tests = new ForTests();
    private static EntityManagerFactory emf;

    @BeforeEach
    public void setUp() {
        emf = Persistence.createEntityManagerFactory("test-derby-cinema");
    }

    @Test
    public void showStartTimeMustBeInTheFuture() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());

        var movieInfo = tests.createSuperMovie(cinema);
        long theaterId = createATheater(cinema);

        var e = assertThrows(BusinessException.class, () -> {
            cinema.addNewShowFor(movieInfo.id(),
                    LocalDateTime.now().minusDays(1),
                    10f, theaterId, 20);
        });

        assertEquals(Cinema.START_TIME_MUST_BE_IN_THE_FUTURE, e.getMessage());
    }

    @Test
    public void aShowIsPlayingAt() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var movieInfo = tests.createSuperMovie(cinema);
        long theaterId = createATheater(cinema);
        cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(2).getYear(),
                        5, 10,
                        13, 30),
                10f, theaterId, 20);
        var movieShows = cinema
                .showsUntil(
                        LocalDateTime.of(LocalDate.now().plusYears(1).getYear(),
                                10, 10, 13, 31));
        assertEquals(1, movieShows.size());
        assertEquals("1hr 49mins", movieShows.get(0).duration());
        assertEquals(1, movieShows.get(0).shows().size());
        assertEquals(10f, movieShows.get(0).shows().get(0).price());
        assertEquals(SUPER_MOVIE_NAME, movieShows.get(0).movieName());
    }

    @Test
    public void reserveSeats() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());

        var movieInfo = tests.createSuperMovie(cinema);

        long theaterId = createATheater(cinema);

        var showInfo = cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);

        var userId = registerAUser(cinema);

        var info = cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

        assertTrue(cinema.allSeatsReservedBy(userId, showInfo.showId(),
                Set.of(1, 5)));

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
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerAUser(cinema);
        cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));
        var info = cinema.show(showInfo.showId());
        assertTrue(cinema.allSeatsReservedBy(userId, showInfo.showId(),
                Set.of(1, 5)));
        assertEquals(SUPER_MOVIE_NAME, info.info().movieName());
        assertEquals("1hr 49mins", info.info().movieDuration());
        assertTrue(info.currentSeats().contains(new Seat(1, false)));
        assertTrue(info.currentSeats().contains(new Seat(2, true)));
        assertTrue(info.currentSeats().contains(new Seat(3, true)));
        assertTrue(info.currentSeats().contains(new Seat(4, true)));
        assertTrue(info.currentSeats().contains(new Seat(5, false)));
    }

    @Test
    public void iCanReserveAnExpiredReservation() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(),
                // already in the past
                () -> LocalDateTime.now().minusMonths(1),
                tests.doNothingToken(),
                10);
        var movieInfo = tests.createSuperMovie(cinema);
        long theaterId = createATheater(cinema);
        var showInfo = cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(),
                        10, 10, 13, 30),
                10f, theaterId, 20);
        var joseUserId = registerUserJose(cinema);
        var userId = registerAUser(cinema);
        cinema.reserve(joseUserId, showInfo.showId(), Set.of(1, 5));
        // if exception is not thrown it means I was able to make the reservation
        cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));
        // in any case all is available because I have reserved with a date provider in the past
        assertFalse(cinema.allSeatsReservedBy(userId, showInfo.showId(),
                Set.of(1, 5)));
    }

    @Test
    public void movieIdNotExists() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var e = assertThrows(BusinessException.class, () -> {
            cinema.movie(NON_EXISTENT_ID);
            fail("MovieId should not exists in the database");
        });
        assertEquals(Cinema.MOVIE_ID_DOES_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void showTimeIdNotExists() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var e = assertThrows(BusinessException.class, () -> {
            cinema.show(NON_EXISTENT_ID);
            fail("ShowId should not exists in the database");
        });
        assertEquals(Cinema.SHOW_TIME_ID_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void theaterIdNotExists() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var movieInfo = tests.createSuperMovie(cinema);
        var e = assertThrows(BusinessException.class, () -> {
            cinema.addNewShowFor(movieInfo.id(), LocalDateTime.now().plusDays(1), 10f, NON_EXISTENT_ID, 10);
            fail("ShowId should not exists in the database");
        });
        assertEquals(Cinema.THEATER_ID_DOES_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void userIdNotExists() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var e = assertThrows(BusinessException.class, () -> {
            cinema.profileFrom(NON_EXISTENT_ID);
            fail("UserId should not exists in the database");
        });
        assertEquals(Cinema.USER_ID_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void reservationHasExpired() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(),
                // already in the past
                () -> LocalDateTime.now().minusMonths(1),
                tests.doNothingToken(),
                10);

        var movieInfo = tests.createSuperMovie(cinema);
        long theaterId = createATheater(cinema);

        var showInfo = cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);

        var userId = registerUserJose(cinema);

        cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

        var e = assertThrows(BusinessException.class, () -> {
            cinema.pay(userId, showInfo.showId(), Set.of(1, 5),
                    JOSEUSER_CREDIT_CARD_NUMBER,
                    JOSEUSER_CREDIT_CARD_EXPIRITY,
                    JOSEUSER_CREDIT_CARD_SEC_CODE);
        });

        assertEquals("Reservation is required before confirm", e.getMessage());
    }

    @Test
    public void reserveAlreadReservedSeats() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());

        var movieInfo = tests.createSuperMovie(cinema);
        long theaterId = createATheater(cinema);

        var showInfo = cinema.addNewShowFor(movieInfo.id(),
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);

        var userId = registerAUser(cinema);
        var joseId = registerUserJose(cinema);

        cinema.reserve(userId, showInfo.showId(), Set.of(1, 5));

        var e = assertThrows(BusinessException.class, () -> {
            cinema.reserve(joseId, showInfo.showId(), Set.of(1, 4, 3));
            fail("I have reserved an already reserved seat");
        });

        assertEquals(Cinema.SELECTED_SEATS_ARE_BUSY, e.getMessage());
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

        assertEquals(CinemaRepository.USER_OR_PASSWORD_ERROR, e.getMessage());
    }

    @Test
    public void changePasswordCurrentPasswordNotTheSame() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var userId = registerUserJose(cinema);

        var e = assertThrows(BusinessException.class, () -> {
            cinema.changePassword(userId, JOSEUSER_PASS + "toMakeItDifferent",
                    "password1234567",
                    "password1234567");
        });

        assertEquals(Cinema.CAN_NOT_CHANGE_PASSWORD, e.getMessage());
    }

    @Test
    public void changePasswordPasswordsNotTheSame() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var userId = registerUserJose(cinema);

        var e = assertThrows(BusinessException.class, () -> {
            cinema.changePassword(userId, JOSEUSER_PASS, "1234567password",
                    "password1234567");
        });

        assertEquals(Cinema.PASSWORDS_MUST_BE_EQUALS, e.getMessage());
    }

    @Test
    public void changePasswordNewPasswordNotValid() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var userId = registerUserJose(cinema);

        var e = assertThrows(DataException.class, () -> {
            cinema.changePassword(userId, JOSEUSER_PASS, "12345",
                    "12345");
        });

        assertEquals(Password.NOT_VALID_PASSWORD, e.getMessage());

    }

    @Test
    public void changePasswordOk() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var userId = registerUserJose(cinema);

        cinema.changePassword(userId, JOSEUSER_PASS, "1234567Passw",
                "1234567Passw");

        assertNotNull(cinema.login(JOSEUSER_USERNAME, "1234567Passw"));
    }

    @Test
    public void registerUserPasswordsDoesNotMatch() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());

        var e = assertThrows(BusinessException.class, () -> {
            cinema.registerUser("aname", "a surname", "anemail@ma.com",
                    "auniqueusername", "password1234567", "1234567password");
        });

        assertEquals(Cinema.PASSWORDS_MUST_BE_EQUALS, e.getMessage());
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
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);

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
                new FormattedDayTime(LocalDateTime.of(
                        LocalDate.now().plusYears(1).getYear(), 10, 10, 13, 30))
                        .toString());
        assertTrue(fakeEmailProvider.hasBeanCalledWith(JOSEUSER_EMAIL,
                emailTemplate.subject(), emailTemplate.body()));

        assertTrue(cinema.allSeatsConfirmedBy(joseId, showInfo.showId(),
                Set.of(1, 5)));
    }

    @Test
    public void rateSameMovieByThreeUsers() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken());
        var movieInfo = tests.createSuperMovie(cinema);
        var joseId = registerUserJose(cinema);
        var antonioId = registerUserAntonio(cinema);
        var aUserId = registerAUser(cinema);
        cinema.rateMovieBy(joseId, movieInfo.id(), 4,
                "great movie");
        cinema.rateMovieBy(antonioId, movieInfo.id(), 2,
                "bad movie");
        cinema.rateMovieBy(aUserId, movieInfo.id(), 5,
                "fantastic movie");
        var movie = cinema.movie(movieInfo.id());
        var listOfRates = cinema.pagedRatesOfOrderedDate(movieInfo.id(), 1);
        assertEquals(3, listOfRates.size());
        assertEquals(String.valueOf(3.67f), movie.ratingValue());
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
        assertEquals(2, movie.actors().size());
        assertEquals(1, movie.directorNames().size());
        assertEquals(SUPER_MOVIE_DIRECTOR_NAME, movie.directorNames().get(0));
        assertTrue(movie.actors()
                .contains(SUPER_MOVIE_ACTOR_CARLOS));
        assertEquals(SUPER_MOVIE_NAME, movie.name());
        assertEquals(SUPER_MOVIE_PLOT, movie.plot());
    }

    @Test
    public void moviesSortedByReleaseDate() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken(), 1);
        tests.createSuperMovie(cinema);
        tests.createOtherSuperMovie(cinema);
        var movies = cinema.pagedMoviesSortedByReleaseDate(1);
        assertEquals(1, movies.size());
        assertEquals(SUPER_MOVIE_NAME, movies.get(0).name());
    }

    @Test
    public void retrieveAllMovies() {
        var cinema = new Cinema(emf, tests.doNothingPaymentProvider(),
                tests.doNothingEmailProvider(), tests.doNothingToken(), 1);
        tests.createSuperMovie(cinema);
        tests.createOtherSuperMovie(cinema);
        var movies1 = cinema.pagedMoviesSortedByName(1);
        assertEquals(1, movies1.size());
        assertEquals(SUPER_MOVIE_NAME, movies1.get(0).name());
        assertEquals(2, movies1.get(0).genres().size());
        assertEquals(2, movies1.get(0).actors().size());
        var movies2 = cinema.pagedMoviesSortedByName(2);
        assertEquals(1, movies2.size());
        assertEquals(OTHER_SUPER_MOVIE_NAME, movies2.get(0).name());
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
        assertEquals(OTHER_SUPER_MOVIE_NAME, movies.get(0).name());
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
        return cinema.registerUser(JOSEUSER_NAME, JOSEUSER_SURNAME,
                JOSEUSER_EMAIL,
                JOSEUSER_USERNAME,
                JOSEUSER_PASS, JOSEUSER_PASS);
    }

    private Long registerUserAntonio(Cinema cinema) {
        return cinema.registerUser("Antonio", "Antonio Surname",
                "antonio@bla.com",
                ANTONIOUSER_USERNAME,
                "password12345678", "password12345678");
    }

    private Long registerAUser(Cinema cinema) {
        return cinema.registerUser("aUser", "user surname",
                "enrique@bla.com",
                "username",
                "password12345678", "password12345678");
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
