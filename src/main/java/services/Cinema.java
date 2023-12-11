package services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import data.entities.Movie;
import data.entities.Person;
import data.entities.Sale;
import data.entities.ShowSeat;
import data.entities.ShowTime;
import data.entities.Theater;
import data.entities.User;
import data.services.CinemaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;
import services.api.BusinessException;
import services.api.CinemaSystem;
import services.api.CreditCardPaymentProvider;
import services.api.DateTimeProvider;
import services.api.DetailedShowInfo;
import services.api.EmailProvider;
import services.api.Genre;
import services.api.MovieInfo;
import services.api.MovieShows;
import services.api.Seat;
import services.api.ShowInfo;
import services.api.Ticket;
import services.api.Token;
import services.api.UserMovieRate;
import services.api.UserProfile;

public class Cinema implements CinemaSystem {
	static final String USER_NAME_ALREADY_EXISTS = "userName already exists";
	private static final int NUMBER_OF_RETRIES = 2;
	static final String CREDIT_CARD_DEBIT_HAS_FAILED = "Credit card debit have failed";
	static final String USER_HAS_ALREADY_RATE = "The user has already rate the movie";
	static final String PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO = "page number must be greater than zero";
	public static final String SELECTED_SEATS_ARE_BUSY = "All or some of the seats chosen are busy";
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MINUTES_TO_KEEP_RESERVATION = 5;
	private static final String SEAT_BUSY = "Seat is currently busy";
	public static final String RESERVATION_IS_REQUIRED_TO_CONFIRM = "Reservation is required before confirm";
	static final String SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED = "The seat cannot be confirmed";
	public static final String CAN_NOT_CHANGE_PASSWORD = "Some of the provided information is not valid to change the password";
	public static final String PASSWORDS_MUST_BE_EQUALS = "Passwords must be equals";

	private EntityManagerFactory emf;
	private CreditCardPaymentProvider paymentGateway;
	private EmailProvider emailProvider;
	private EntityManager em;
	private int pageSize;
	private DateTimeProvider dateTimeProvider;
	private Token token;

	public Cinema(EntityManagerFactory emf,
			CreditCardPaymentProvider paymentGateway,
			EmailProvider emailProvider, DateTimeProvider provider,
			Token token,
			int pageSize) {
		this.emf = emf;
		this.paymentGateway = paymentGateway;
		this.emailProvider = emailProvider;
		this.token = token;
		this.pageSize = pageSize;
		this.dateTimeProvider = provider;
	}

	public Cinema(EntityManagerFactory emf,
			CreditCardPaymentProvider paymentGateway,
			EmailProvider emailProvider, Token token, int pageSize) {
		this(emf, paymentGateway, emailProvider, DateTimeProvider.create(),
				token, pageSize);
	}

	public Cinema(EntityManagerFactory emf,
			CreditCardPaymentProvider paymentGateway,
			EmailProvider emailProvider, Token token) {
		this(emf, paymentGateway, emailProvider, DateTimeProvider.create(),
				token, DEFAULT_PAGE_SIZE);
	}

	// DONE
	@Override
	public List<MovieShows> showsUntil(LocalDateTime untilTo) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var movies = dataRepository.moviesWithShowsUntil(untilTo);
			return movies.stream()
					.map(movie -> {
						return convertMovieToMovieShow(movie);
					})
					.collect(Collectors.toUnmodifiableList());
		});
	}

	private MovieShows convertMovieToMovieShow(Movie movie) {
		return new MovieShows(movie.id(), movie.name(),
				new MovieDurationFormat(movie.duration())
						.toString(),
				movie.genreAsListOfString(), movie.showTimes()
						.map(show -> convertShowTimeToShowInfo(show))
						.toList());
	}

	// DONE
	@Override
	public MovieInfo movie(Long id) {
		return inTx(em -> {
			var movie = new CinemaRepository(em, this.pageSize)
					.movieWithActorsById(id);
			return convertMovieToMovieInfo(movie);
		});
	}

	// DONE
	@Override
	public MovieInfo addNewMovie(String name, int duration,
			LocalDate releaseDate, String plot, Set<Genre> genres) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var movie = new Movie(name, plot, duration, releaseDate, genres);
			dataRepository.save(movie);
			return convertMovieToMovieInfo(movie);
		});
	}

	// DONE
	@Override
	public MovieInfo addActorTo(Long movieId, String name, String surname,
			String email, String characterName) {
		return inTx(em -> {
			var movie = movieBy(movieId, em);
			movie.addAnActor(name, surname, email, characterName);
			return convertMovieToMovieInfo(movie);
		});
	}

	// DONE
	@Override
	public MovieInfo addDirectorToMovie(Long movieId, String name,
			String surname, String email) {
		return inTx(em -> {
			var movie = movieBy(movieId, em);
			movie.addADirector(name, surname, email);
			return convertMovieToMovieInfo(movie);
		});
	}

	private Movie movieBy(Long movieId, EntityManager em) {
		var dataRepository = new CinemaRepository(em, this.pageSize);
		var movie = dataRepository.movieBy(movieId);
		return movie;
	}

	// DONE
	@Override
	public Long addNewTheater(String name, Set<Integer> seatsNumbers) {
		return inTx(em -> {
			var theater = new Theater(name, seatsNumbers);
			var dataRepository = new CinemaRepository(em, this.pageSize);
			dataRepository.save(theater);
			return theater.id();
		});
	}

	// DONE
	@Override
	public ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
			float price, Long theaterId, int pointsToWin) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var movie = dataRepository.movieBy(movieId);
			var theatre = dataRepository.theatreBy(theaterId);

			var showTime = new ShowTime(movie, startTime, price, theatre,
					pointsToWin);

			dataRepository.save(showTime);

			return convertShowTimeToShowInfo(showTime);
		});
	}

	boolean allSeatsWithCondition(Long userId, Long showTimeId,
			Set<Integer> selectedSeats,
			BiFunction<ShowSeat, User, Boolean> condition) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);

			ShowTime showTime = dataRepository.showTimeBy(showTimeId);
			var user = dataRepository.userBy(userId);
			var selectedSeats1 = filterSelectedSeats(showTime, selectedSeats);

			return allMatchConditionFor(selectedSeats1,
					seat -> condition.apply(seat, user));
		});
	}

	boolean allSeatsReservedBy(Long userId, Long showTimeId,
			Set<Integer> selectedSeats) {
		return inTx(em -> {
			return allSeatsWithCondition(userId, showTimeId, selectedSeats,
					(seat, user) -> seat.isReservedBy(user));
		});
	}

	boolean allSeatsConfirmedBy(Long userId, Long showTimeId,
			Set<Integer> selectedSeats) {
		return inTx(em -> {
			return allSeatsWithCondition(userId, showTimeId, selectedSeats,
					(seat, user) -> seat.isConfirmedBy(user));
		});
	}

	private boolean allMatchConditionFor(Set<ShowSeat> seatsToReserve,
			Predicate<ShowSeat> condition) {
		return seatsToReserve.stream().allMatch(condition);
	}

	@Override
	public DetailedShowInfo reserve(Long userId, Long showTimeId,
			Set<Integer> selectedSeats) {
		return inTx(em -> {

			var dataRepository = new CinemaRepository(em, this.pageSize);

			ShowTime showTime = dataRepository.showTimeBy(showTimeId);
			var user = dataRepository.userBy(userId);

			Set<ShowSeat> selectedShowSeats = filterSelectedSeats(showTime,
					selectedSeats);

			checkAllSelectedSeatsAreAvailable(selectedShowSeats);

			selectedShowSeats.stream().forEach(seat -> {
				if (!seat.isAvailable()) {
					throw new BusinessException(SEAT_BUSY);
				}

				seat.reserve();
				seat.reservedBy(user);
				seat.reservedUntil(this.dateTimeProvider.now()
						.plusMinutes(MINUTES_TO_KEEP_RESERVATION));
			});

			return convertShowTimeToDetailedShowInfo(showTime);
		});
	}

	private Set<ShowSeat> filterSelectedSeats(ShowTime showTime,
			Set<Integer> selectedSeats) {
		return showTime.seatsForThisShow().stream()
				.filter(seat -> seat.isIncludedIn(selectedSeats))
				.collect(Collectors.toUnmodifiableSet());
	}

	private void checkAllSelectedSeatsAreAvailable(Set<ShowSeat> selection) {
		checkAtLeastOneMatchConditionFor(selection, seat -> seat.isBusy(),
				SELECTED_SEATS_ARE_BUSY);
	}

	private void checkAtLeastOneMatchConditionFor(Set<ShowSeat> seatsToReserve,
			Predicate<ShowSeat> condition, String errorMsg) {
		if (seatsToReserve.stream().anyMatch(condition)) {
			throw new BusinessException(errorMsg);
		}
	}

	@Override
	public Ticket pay(Long userId, Long showTimeId, Set<Integer> selectedSeats,
			String creditCardNumber, YearMonth expirationDate,
			String secturityCode) {

		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);

			var showTime = dataRepository.showTimeBy(showTimeId);
			var user = dataRepository.userBy(userId);

			var totalAmount = confirmSeatsAndGetTotalAmount(selectedSeats,
					showTime, user);

			tryCreditCardDebit(creditCardNumber, expirationDate, secturityCode,
					totalAmount);

			sendNewSaleEmailToTheUser(selectedSeats, showTime, user,
					totalAmount);

			var sale = new Sale(totalAmount, user, showTime,
					showTime.pointsToEarn(), selectedSeats);

			return sale.ticket();
		});
	}

	@Override
	public String login(String username, String password) {
		return inTx(em -> {
			var user = new CinemaRepository(em, this.pageSize).login(
					username, password,
					this.dateTimeProvider.now());
			return token.tokenFrom(user.toMap());
		});
	}

	// DONE
	@Override
	public Long registerUser(String name, String surname, String email,
			String userName,
			String password, String repeatPassword) {
		return inTxWithRetriesOnConflict((em) -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);

			if (dataRepository.doesUserExist(userName)) {
				throw new BusinessException(USER_NAME_ALREADY_EXISTS);
			}

			checkPasswordsMatch(password, repeatPassword);

			var user = new User(new Person(name, surname, email), userName,
					password,
					repeatPassword);
			dataRepository.save(user);
			return user.id();
		}, NUMBER_OF_RETRIES);
	}

	// DONE
	@Override
	public UserMovieRate rateMovieBy(Long userId, Long movieId,
			int userRateValue,
			String userComment) {
		return inTxWithRetriesOnConflict(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);

			if (dataRepository.hasUserAlreadyRateMovie(userId, movieId)) {
				throw new BusinessException(USER_HAS_ALREADY_RATE);
			}
			var movie = dataRepository.movieBy(movieId);
			var user = dataRepository.userBy(userId);

			var newRateValue = calcNewRateValueForTheMovie(userRateValue,
					movie);

			var ratedAt = dateTimeProvider.now();
			movie.setRateValue(newRateValue);
			movie.addUserRate(user, userRateValue,
					userComment, ratedAt);
			return new UserMovieRate(user.userName(), userRateValue,
					new FormattedDateTime(ratedAt).toString(),
					userComment);
		}, NUMBER_OF_RETRIES);
	}

	private float calcNewRateValueForTheMovie(int userRateValue,
			Movie movie) {
		var newRateValue = Math
				.round(((movie.totalValue() + userRateValue)
						/ (movie.totalVotes() + 1))
						* 100.0f)
				/ 100.0f;
		return newRateValue;
	}

	private void tryCreditCardDebit(String creditCardNumber,
			YearMonth expirationDate, String secturityCode, float totalAmount) {
		try {
			this.paymentGateway.pay(creditCardNumber, expirationDate,
					secturityCode, totalAmount);
		} catch (Exception e) {
			throw new BusinessException(CREDIT_CARD_DEBIT_HAS_FAILED, e);
		}
	}

	private void sendNewSaleEmailToTheUser(Set<Integer> selectedSeats,
			ShowTime showTime, User user, float totalAmount) {
		var emailTemplate = new NewSaleEmailTemplate(totalAmount,
				user.userName(), selectedSeats, showTime.movieName(),
				showTime.startDateTime());

		this.emailProvider.send(user.email(), emailTemplate.subject(),
				emailTemplate.body());
	}

	private float confirmSeatsAndGetTotalAmount(Set<Integer> selectedSeats,
			ShowTime showTime, User user) {
		var selectedShowSeats = filterSelectedSeats(showTime, selectedSeats);
		checkAllSelectedSeatsAreReservedBy(user, selectedShowSeats);
		selectedShowSeats.stream().forEach(seat -> {
			if (!seat.isReservedBy(user) || seat.isConfirmed()) {
				throw new BusinessException(
						SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED);
			}
			seat.confirm();
			seat.confirmedBy(user);
		});

		return Math.round(selectedSeats.size() * showTime.price() * 100.0f)
				/ 100.0f;
	}

	private void checkAllSelectedSeatsAreReservedBy(User user,
			Set<ShowSeat> selection) {
		checkAtLeastOneMatchConditionFor(selection,
				seat -> !seat.isReservedBy(user),
				RESERVATION_IS_REQUIRED_TO_CONFIRM);
	}

	// DONE
	@Override
	public List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId,
			int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var usersRate = dataRepository.pagedUserRates(movieId, pageNumber);

			return usersRate.stream()
					.map(rate -> new UserMovieRate(rate.userName(),
							rate.rateValue(),
							new FormattedDateTime(rate.ratedAt()).toString(),
							rate.comment()))
					.toList();
		});
	}

	// DONE
	@Override
	public DetailedShowInfo show(Long id) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var show = dataRepository.showTimeBy(id);

			return convertShowTimeToDetailedShowInfo(show);
		});
	}

	private DetailedShowInfo convertShowTimeToDetailedShowInfo(ShowTime show) {
		var seats = show.seatsForThisShow().stream()
				.map(s -> new Seat(s.seatNumber(), s.isAvailable())).toList();

		return new DetailedShowInfo(convertShowTimeToShowInfo(show),
				show.screenedIn(),
				seats);
	}

	private ShowInfo convertShowTimeToShowInfo(ShowTime show) {
		return new ShowInfo(show.id(), show.movieName(),
				new MovieDurationFormat(show.movieDuration())
						.toString(),
				show.startDateTime(),
				show.price());
	}

	// DONE
	@Override
	public List<MovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
			int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var movies = dataRepository.pagedSearchMovieByName(
					fullOrPartmovieName,
					pageNumber);
			return movies.stream().map(m -> {
				return convertMovieToMovieInfo(m);
			}).toList();
		});
	}

	private MovieInfo convertMovieToMovieInfo(Movie m) {
		return new MovieInfo(m.id(), m.name(),
				new MovieDurationFormat(m.duration()).toString(),
				m.plot(),
				m.genreAsListOfString(), m.directorsNamesAsString(),
				new FormattedDate(m.releaseDate()).toString(),
				m.ratingValueAsString(), m.totalVotes(),
				m.toActorsInMovieNames());
	}

	private void checkPageNumberIsGreaterThanZero(int pageNumber) {
		if (pageNumber <= 0) {
			throw new BusinessException(PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO);
		}
	}

	// DONE
	@Override
	public List<MovieInfo> pagedMoviesSortedByName(int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return pagedMoviesSortedBy(pageNumber, "order by m.name");
	}

	// DONE
	@Override
	public List<MovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber) {
		return pagedMoviesSortedBy(pageNumber, "order by m.releaseDate desc");
	}

	private List<MovieInfo> pagedMoviesSortedBy(int pageNumber,
			String orderByClause) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var movies = dataRepository.pagedMoviesSortedBy(pageNumber,
					orderByClause);

			return movies.stream()
					.map(m -> convertMovieToMovieInfo(m)).toList();
		});
	}

	// DONE
	@Override
	public List<MovieInfo> pagedMoviesSortedByRate(int pageNumber) {
		return pagedMoviesSortedBy(pageNumber,
				"order by m.rating.totalUserVotes desc, m.rating.rateValue desc");
	}

	private <T> T inTx(Function<EntityManager, T> toExecute) {
		em = emf.createEntityManager();
		var tx = em.getTransaction();

		try {
			tx.begin();

			T t = toExecute.apply(em);
			tx.commit();

			return t;
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	private <T> T inTxWithRetriesOnConflict(
			Function<EntityManager, T> toExecute,
			int retriesNumber) {
		int retries = 0;

		while (retries < retriesNumber) {
			try {
				return inTx(toExecute);
				// There is no a great way in JPA to detect a constraint
				// violation. I use RollbackException and retries one more
				// time for specific use cases
			} catch (RollbackException e) {
				// jakarta.persistence.RollbackException
				retries++;
			}
		}
		throw new BusinessException(
				"Trasaction could not be completed due to concurrency conflic");
	}

	@Override
	public Long userIdFrom(String token) {
		return this.token.verifyAndGetUserIdFrom(token);
	}

	@Override
	public void changePassword(Long userId, String currentPassword,
			String newPassword1, String newPassword2) {
		inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var user = dataRepository.userBy(userId);

			if (!user.hasPassword(currentPassword)) {
				throw new BusinessException(CAN_NOT_CHANGE_PASSWORD);
			}
			checkPasswordsMatch(newPassword2, newPassword1);

			user.setNewPassword(newPassword1);

			// just to conform the compiler
			return null;
		});
	}

	private void checkPasswordsMatch(String password, String repeatPassword) {
		if (!password.equals(repeatPassword)) {
			throw new BusinessException(PASSWORDS_MUST_BE_EQUALS);
		}
	}

	@Override
	public UserProfile profileFrom(Long userId) {
		return inTx(em -> {
			var dataRepository = new CinemaRepository(em, this.pageSize);
			var user = dataRepository.userBy(userId);
			return new UserProfile(user.fullName(), user.userName(),
					user.email(), user.points());
		});
	}
}
