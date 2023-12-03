package model.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface CinemaSystem {

	List<MovieShows> showsUntil(LocalDateTime untilTo);

	List<MovieInfo> pagedMoviesSortedByName(int pageNumber);

	List<MovieInfo> pagedMoviesSortedByRate(int pageNumber);

	List<MovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber);

	MovieInfo movie(Long id);

	DetailedShowInfo show(Long id);

	MovieInfo addNewMovie(String name, int duration,
			LocalDate releaseDate, String plot, Set<Genre> genres);

	MovieInfo addActorTo(Long movieId, String name, String surname,
			String email, String characterName);

	MovieInfo addDirectorToMovie(Long movieId, String name,
			String surname, String email);

	Long addNewTheater(String name, Set<Integer> seatsNumbers);

	ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
			float price, Long theaterId, int pointsToWin);

	DetailedShowInfo reserve(Long userId, Long showTimeId,
			Set<Integer> selectedSeats);

	Ticket pay(Long userId, Long showTimeId, Set<Integer> selectedSeats,
			String creditCardNumber, YearMonth expirationDate,
			String secturityCode);

	UserMovieRate rateMovieBy(Long userId, Long idMovie, int rateValue,
			String comment);

	List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId, int pageNumber);

	List<MovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
			int pageNumber);

	String login(String username, String password);

	Long userIdFrom(String token);

	UserProfile profileFrom(Long userId);

	Long registerUser(String name, String surname, String email,
			String userName,
			String password, String repeatPassword);
}
