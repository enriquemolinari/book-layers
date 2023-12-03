package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.RollbackException;
import model.api.AuthException;
import model.api.BusinessException;
import model.api.CinemaSystem;
import model.api.CreditCardPaymentProvider;
import model.api.DateTimeProvider;
import model.api.DetailedShowInfo;
import model.api.EmailProvider;
import model.api.Genre;
import model.api.MovieInfo;
import model.api.MovieShows;
import model.api.ShowInfo;
import model.api.Ticket;
import model.api.Token;
import model.api.UserMovieRate;
import model.api.UserProfile;

public class Cinema implements CinemaSystem {
	static final String USER_NAME_ALREADY_EXISTS = "userName already exists";
	private static final int NUMBER_OF_RETRIES = 2;
	static final String MOVIE_ID_DOES_NOT_EXISTS = "Movie ID not found";
	static final String SHOW_TIME_ID_NOT_EXISTS = "Show ID not found";
	static final String USER_ID_NOT_EXISTS = "User not registered";
	static final String CREDIT_CARD_DEBIT_HAS_FAILED = "Credit card debit have failed";
	static final String USER_HAS_ALREADY_RATE = "The user has already rate the movie";
	static final String PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO = "page number must be greater than zero";
	private static final int DEFAULT_PAGE_SIZE = 20;
	public static final String USER_OR_PASSWORD_ERROR = "Invalid username or password";

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

	public List<MovieShows> showsUntil(LocalDateTime untilTo) {
		return inTx(em -> {
			return movieShowsUntil(untilTo);
		});
	}

	private List<MovieShows> movieShowsUntil(LocalDateTime untilTo) {
		var query = em.createQuery(
				"from Movie m "
						+ "join fetch m.showTimes s join fetch s.screenedIn "
						+ "where s.startTime >= ?1 and s.startTime <= ?2 "
						+ "order by m.name asc",
				Movie.class).setParameter(1, LocalDateTime.now())
				.setParameter(2, untilTo);

		return query.getResultList().stream()
				.map(movieShow -> movieShow.toMovieShow())
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public MovieInfo movie(Long id) {
		return inTx(em -> {
			try {
				return movieWithActorsById(id);
			} catch (NonUniqueResultException | NoResultException e) {
				throw new BusinessException(MOVIE_ID_DOES_NOT_EXISTS);
			}
		});
	}

	private MovieInfo movieWithActorsById(Long id) {
		return em
				.createQuery("from Movie m "
						+ "join fetch m.actors a "
						+ "join fetch m.actors.person "
						+ "where m.id = ?1 "
						+ "order by m.name asc", Movie.class)
				.setParameter(1, id).getSingleResult().toInfo();
	}

	@Override
	public MovieInfo addNewMovie(String name, int duration,
			LocalDate releaseDate, String plot, Set<Genre> genres) {
		return inTx(em -> {
			var movie = new Movie(name, plot, duration, releaseDate, genres);
			em.persist(movie);
			return movie.toInfo();
		});
	}

	@Override
	public MovieInfo addActorTo(Long movieId, String name, String surname,
			String email, String characterName) {
		return inTx(em -> {
			var movie = em.getReference(Movie.class, movieId);
			movie.addAnActor(name, surname, email, characterName);
			return movie.toInfo();
		});
	}

	@Override
	public MovieInfo addDirectorToMovie(Long movieId, String name,
			String surname, String email) {
		return inTx(em -> {
			var movie = em.getReference(Movie.class, movieId);
			movie.addADirector(name, surname, email);
			return movie.toInfo();
		});
	}

	public Long addNewTheater(String name, Set<Integer> seatsNumbers) {
		return inTx(em -> {
			var theater = new Theater(name, seatsNumbers);
			em.persist(theater);
			return theater.id();
		});
	}

	public ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
			float price, Long theaterId, int pointsToWin) {
		return inTx(em -> {
			var movie = movieBy(movieId);
			var theatre = theatreBy(theaterId);

			var showTime = new ShowTime(movie, startTime, price, theatre,
					pointsToWin);

			em.persist(showTime);

			return showTime.toShowInfo();
		});
	}

	@Override
	public DetailedShowInfo reserve(Long userId, Long showTimeId,
			Set<Integer> selectedSeats) {
		return inTx(em -> {

			ShowTime showTime = showTimeBy(showTimeId);
			var user = userBy(userId);

			showTime.reserveSeatsFor(user, selectedSeats);

			return showTime.toDetailedInfo();
		});
	}

	@Override
	public Ticket pay(Long userId, Long showTimeId, Set<Integer> selectedSeats,
			String creditCardNumber, YearMonth expirationDate,
			String secturityCode) {

		return inTx(em -> {
			ShowTime showTime = showTimeBy(showTimeId);
			var user = userBy(userId);

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
			var q = this.em.createQuery(
					"select u from User u where u.userName = ?1 and u.password.password = ?2",
					User.class);
			q.setParameter(1, username);
			q.setParameter(2, password);
			var mightBeAUser = q.getResultList();
			if (mightBeAUser.size() == 0) {
				throw new AuthException(USER_OR_PASSWORD_ERROR);
			}
			var user = mightBeAUser.get(0);
			em.persist(new LoginAudit(this.dateTimeProvider.now(), user));
			return token.tokenFrom(user.toMap());
		});
	}

	@Override
	public Long registerUser(String name, String surname, String email,
			String userName,
			String password, String repeatPassword) {
		return inTxWithRetriesOnConflict((em) -> {
			checkUserNameAlreadyExists(userName);
			var user = new User(new Person(name, surname, email), userName,
					password,
					repeatPassword);
			em.persist(user);
			return user.id();
		}, NUMBER_OF_RETRIES);
	}

	@Override
	public UserMovieRate rateMovieBy(Long userId, Long movieId, int rateValue,
			String comment) {
		return inTxWithRetriesOnConflict(em -> {
			checkUserIsRatingSameMovieTwice(userId, movieId);
			var user = userBy(userId);
			var movie = movieBy(movieId);

			var userRate = movie.rateBy(user, rateValue, comment);
			return userRate.toUserMovieRate();
		}, NUMBER_OF_RETRIES);
	}

	private void checkUserIsRatingSameMovieTwice(Long userId, Long movieId) {
		var q = this.em.createQuery(
				"select ur from UserRate ur where ur.user.id = ?1 and movie.id = ?2",
				UserRate.class);
		q.setParameter(1, userId);
		q.setParameter(2, movieId);
		var mightHaveRated = q.getResultList();
		if (mightHaveRated.size() > 0) {
			throw new BusinessException(USER_HAS_ALREADY_RATE);
		}
	}

	private void checkUserNameAlreadyExists(String userName) {
		var q = this.em.createQuery(
				"select u from User u where u.userName = ?1 ", User.class);
		q.setParameter(1, userName);
		var mightBeAUser = q.getResultList();
		if (mightBeAUser.size() > 0) {
			throw new BusinessException(USER_NAME_ALREADY_EXISTS);
		}
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
		showTime.confirmSeatsForUser(user, selectedSeats);
		var totalAmount = showTime.totalAmountForTheseSeats(selectedSeats);
		return totalAmount;
	}

	private Theater theatreBy(Long theatreId) {
		try {
			return em.getReference(Theater.class, theatreId);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(MOVIE_ID_DOES_NOT_EXISTS);
		}
	}

	private Movie movieBy(Long movieId) {
		return findByIdOrThrows(Movie.class, movieId, MOVIE_ID_DOES_NOT_EXISTS);
	}

	private User userBy(Long userId) {
		return findByIdOrThrows(User.class, userId, USER_ID_NOT_EXISTS);
	}

	private ShowTime showTimeBy(Long id) {
		return findByIdOrThrows(ShowTime.class, id, SHOW_TIME_ID_NOT_EXISTS);
	}

	<T> T findByIdOrThrows(Class<T> entity, Long id, String msg) {
		var e = em.find(entity, id);
		if (e == null) {
			throw new BusinessException(msg);
		}

		return e;
	}

	@Override
	public List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId,
			int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var q = em.createQuery(
					"select ur from UserRate ur "
							+ "where ur.movie.id = ?1 "
							+ "order by ur.ratedAt desc",
					UserRate.class);
			q.setParameter(1, movieId);
			q.setFirstResult((pageNumber - 1) * this.pageSize);
			q.setMaxResults(this.pageSize);
			return q.getResultList().stream()
					.map(rate -> rate.toUserMovieRate()).toList();
		});
	}

	@Override
	public DetailedShowInfo show(Long id) {
		return inTx(em -> {
			var show = showTimeBy(id);
			return show.toDetailedInfo();
		});
	}

	@Override
	public List<MovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
			int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var q = em.createQuery(
					"select m from Movie m "
							// a trigram index is required
							// on m.name to make this perform fine
							+ "where lower(m.name) like lower(?1) "
							+ "order by m.name desc",
					Movie.class);
			q.setParameter(1, "%" + fullOrPartmovieName + "%");
			q.setFirstResult((pageNumber - 1) * this.pageSize);
			q.setMaxResults(this.pageSize);
			return q.getResultList().stream().map(m -> m.toInfo()).toList();
		});
	}

	private void checkPageNumberIsGreaterThanZero(int pageNumber) {
		if (pageNumber <= 0) {
			throw new BusinessException(PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO);
		}
	}

	@Override
	public List<MovieInfo> pagedMoviesSortedByName(int pageNumber) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return pagedMoviesSortedBy(pageNumber, "order by m.name");
	}

	@Override
	public List<MovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber) {
		return pagedMoviesSortedBy(pageNumber, "order by m.releaseDate desc");
	}

	private List<MovieInfo> pagedMoviesSortedBy(int pageNumber,
			String orderByClause) {
		checkPageNumberIsGreaterThanZero(pageNumber);
		return inTx(em -> {
			var q = em.createQuery(
					"select m from Movie m "
							+ orderByClause,
					Movie.class);
			q.setFirstResult((pageNumber - 1) * this.pageSize);
			q.setMaxResults(this.pageSize);

			return q.getResultList().stream().map(m -> m.toInfo()).toList();
		});
	}

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
	public UserProfile profileFrom(Long userId) {
		return inTx(em -> {
			return userBy(userId).toProfile();
		});
	}
}
