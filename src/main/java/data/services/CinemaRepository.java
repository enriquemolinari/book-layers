package data.services;

import java.time.LocalDateTime;
import java.util.List;

import data.entities.LoginAudit;
import data.entities.Movie;
import data.entities.ShowTime;
import data.entities.Theater;
import data.entities.User;
import data.entities.UserRate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

public class CinemaRepository {

	static final String SHOW_TIME_ID_NOT_EXISTS = "Show ID not found";
	static final String MOVIE_ID_DOES_NOT_EXISTS = "Movie ID not found";
	static final String USER_ID_NOT_EXISTS = "User not registered";
	public static final String USER_OR_PASSWORD_ERROR = "Invalid username or password";

	private EntityManager em;
	private int pageSize;

	public CinemaRepository(EntityManager em, int pageSize) {
		this.em = em;
		this.pageSize = pageSize;
	}

	public User login(String username, String password,
			LocalDateTime loginTime) {
		var q = this.em.createQuery(
				"select u from User u where u.userName = ?1 and u.password.password = ?2",
				User.class);
		q.setParameter(1, username);
		q.setParameter(2, password);
		var mightBeAUser = q.getResultList();
		if (mightBeAUser.size() == 0) {
			throw new DataException(USER_OR_PASSWORD_ERROR);
		}
		var user = mightBeAUser.get(0);
		em.persist(new LoginAudit(loginTime, user));
		return user;
	}

	public List<UserRate> pagedUserRates(Long movieId, int pageNumber) {
		var q = em.createQuery(
				"select ur from UserRate ur "
						+ "where ur.movie.id = ?1 "
						+ "order by ur.ratedAt desc",
				UserRate.class);
		q.setParameter(1, movieId);
		q.setFirstResult((pageNumber - 1) * this.pageSize);
		q.setMaxResults(this.pageSize);
		return q.getResultList();
	}

	public Theater theatreBy(Long theatreId) {
		try {
			return em.getReference(Theater.class, theatreId);
		} catch (IllegalArgumentException e) {
			throw new DataException(MOVIE_ID_DOES_NOT_EXISTS);
		}
	}

	public boolean doesUserExist(String userName) {
		var q = this.em.createQuery(
				"select u from User u where u.userName = ?1 ", User.class);
		q.setParameter(1, userName);
		var mightBeAUser = q.getResultList();
		return (mightBeAUser.size() > 0);
	}

	public boolean hasUserAlreadyRateMovie(Long userId, Long movieId) {
		var q = this.em.createQuery(
				"select ur from UserRate ur where ur.user.id = ?1 and movie.id = ?2",
				UserRate.class);
		q.setParameter(1, userId);
		q.setParameter(2, movieId);
		var mightHaveRated = q.getResultList();
		return mightHaveRated.size() > 0;
	}

	public List<Movie> pagedSearchMovieByName(String fullOrPartmovieName,
			int pageNumber) {
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
		return q.getResultList();
	}

	public List<Movie> pagedMoviesSortedBy(int pageNumber,
			String orderByClause) {
		var q = em.createQuery(
				"select m from Movie m "
						+ orderByClause,
				Movie.class);
		q.setFirstResult((pageNumber - 1) * this.pageSize);
		q.setMaxResults(this.pageSize);
		return q.getResultList();
	}

	public Movie movieWithActorsById(Long id) {
		try {
			return em
					.createQuery("from Movie m "
							+ "join fetch m.actors a "
							+ "join fetch m.actors.person "
							+ "where m.id = ?1 "
							+ "order by m.name asc", Movie.class)
					.setParameter(1, id).getSingleResult();
		} catch (NonUniqueResultException | NoResultException e) {
			throw new DataException(MOVIE_ID_DOES_NOT_EXISTS);
		}
	}

	public List<Movie> moviesWithShowsUntil(LocalDateTime untilTo) {
		var query = em.createQuery(
				"from Movie m "
						+ "join fetch m.showTimes s join fetch s.screenedIn "
						+ "where s.startTime >= ?1 and s.startTime <= ?2 "
						+ "order by m.name asc",
				Movie.class).setParameter(1, LocalDateTime.now())
				.setParameter(2, untilTo);
		return query.getResultList();
	}

	public ShowTime showTimeBy(Long id) {
		return findByIdOrThrows(ShowTime.class, id, SHOW_TIME_ID_NOT_EXISTS);
	}

	public Movie movieBy(Long movieId) {
		return findByIdOrThrows(Movie.class, movieId, MOVIE_ID_DOES_NOT_EXISTS);
	}

	public User userBy(Long userId) {
		return findByIdOrThrows(User.class, userId, USER_ID_NOT_EXISTS);
	}

	private <T> T findByIdOrThrows(Class<T> entity, Long id, String msg) {
		var e = em.find(entity, id);
		if (e == null) {
			throw new DataException(msg);
		}
		return e;
	}

	public <T> void save(T entity) {
		em.persist(entity);
	}
}
