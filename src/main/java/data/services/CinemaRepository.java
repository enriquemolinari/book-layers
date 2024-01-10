package data.services;

import data.entities.*;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CinemaRepository {

    public static final String USER_OR_PASSWORD_ERROR = "Invalid username or password";
    private final EntityManager em;
    private final int pageSize;

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
        if (mightBeAUser.isEmpty()) {
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

    public Optional<Theater> theatreBy(Long theatreId) {
        return findById(Theater.class, theatreId);
    }

    public boolean doesUserExist(String userName) {
        var q = this.em.createQuery(
                "select u from User u where u.userName = ?1 ", User.class);
        q.setParameter(1, userName);
        var mightBeAUser = q.getResultList();
        return (!mightBeAUser.isEmpty());
    }

    public boolean hasUserAlreadyRateMovie(Long userId, Long movieId) {
        var q = this.em.createQuery(
                "select ur from UserRate ur where ur.user.id = ?1 and movie.id = ?2",
                UserRate.class);
        q.setParameter(1, userId);
        q.setParameter(2, movieId);
        var mightHaveRated = q.getResultList();
        return !mightHaveRated.isEmpty();
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

    public Optional<ShowTime> showTimeBy(Long id) {
        return findById(ShowTime.class, id);
    }

    public Optional<Movie> movieBy(Long movieId) {
        return findById(Movie.class, movieId);
    }

    public Optional<User> userBy(Long userId) {
        return findById(User.class, userId);
    }

    private <T> Optional<T> findById(Class<T> entity, Long id) {
        return Optional.ofNullable(em.find(entity, id));
    }

    public <T> void save(T entity) {
        em.persist(entity);
    }
}
