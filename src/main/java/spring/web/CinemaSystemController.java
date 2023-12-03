package spring.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.api.AuthException;
import model.api.CinemaSystem;
import model.api.DetailedShowInfo;
import model.api.MovieInfo;
import model.api.MovieShows;
import model.api.Ticket;
import model.api.UserMovieRate;
import model.api.UserProfile;

@RestController
public class CinemaSystemController {

	public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
	private static final String TOKEN_COOKIE_NAME = "token";
	private CinemaSystem cinema;

	public CinemaSystemController(CinemaSystem cinema) {
		this.cinema = cinema;
	}

	@GetMapping("/movies/{id}")
	public ResponseEntity<MovieInfo> movieDetail(@PathVariable Long id) {
		return ResponseEntity.ok(cinema.movie(id));
	}

	@GetMapping("/movies/sorted/rate")
	public ResponseEntity<List<MovieInfo>> moviesSortedByRate(
			@RequestParam(defaultValue = "1") int page) {
		return ResponseEntity.ok(cinema.pagedMoviesSortedByRate(page));
	}

	@GetMapping("/movies/search/{fullOrPartialName}")
	public ResponseEntity<List<MovieInfo>> moviesSearchBy(
			@PathVariable String fullOrPartialName,
			@RequestParam(defaultValue = "1") int page) {
		return ResponseEntity
				.ok(cinema.pagedSearchMovieByName(fullOrPartialName, page));
	}

	@GetMapping("/movies/sorted/releasedate")
	public ResponseEntity<List<MovieInfo>> moviesSortedByReleaseDate(
			@RequestParam(defaultValue = "1") int page) {
		return ResponseEntity.ok(cinema.pagedMoviesSortedByReleaseDate(page));
	}

	@GetMapping("/movies")
	public ResponseEntity<List<MovieInfo>> allMovies(
			@RequestParam(defaultValue = "1") int page) {
		return ResponseEntity.ok(cinema.pagedMoviesSortedByName(page));
	}

	@GetMapping("/shows")
	public List<MovieShows> playingTheseDays() {
		return cinema.showsUntil(LocalDateTime.now().plusDays(10));
	}

	@GetMapping("/shows/{id}")
	public ResponseEntity<DetailedShowInfo> showDetail(
			@PathVariable Long id) {
		return ResponseEntity.ok(cinema.show(id));
	}

	@PostMapping("/users/register")
	public ResponseEntity<Long> userRegistration(
			@RequestBody UserRegistrationRequest request) {

		return ResponseEntity
				.ok(cinema.registerUser(request.name(), request.surname(),
						request.email(),
						request.userName(), request.password(),
						request.repeatPassword()));
	}

	@GetMapping("/users/profile")
	public ResponseEntity<UserProfile> userProfile(
			@CookieValue(required = false) String token) {

		var profile = ifAuthenticatedDo(token, userId -> {
			return cinema.profileFrom(userId);
		});

		return ResponseEntity.ok(profile);
	}

	@GetMapping("/movies/{id}/rate")
	public ResponseEntity<List<UserMovieRate>> pagedRatesOfOrderedDate(
			@PathVariable Long id,
			@RequestParam(defaultValue = "1") int page) {
		return ResponseEntity.ok(cinema.pagedRatesOfOrderedDate(id, page));
	}

	@PostMapping("/login")
	public ResponseEntity<UserProfile> login(@RequestBody LoginRequest form) {
		String token = cinema.login(form.username(), form.password());
		var profile = cinema.profileFrom(cinema.userIdFrom(token));

		var cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, token)
				.httpOnly(true).path("/").build();
		var headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
		return ResponseEntity.ok().headers(headers).body(profile);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@CookieValue(required = false) String token) {
		return ifAuthenticatedDo(token, (userId) -> {
			var cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, null)
					.httpOnly(true).maxAge(0).build();
			var headers = new HttpHeaders();
			headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
			return ResponseEntity.ok().headers(headers).build();
		});
	}

	@PostMapping("/shows/{showId}/reserve")
	public ResponseEntity<DetailedShowInfo> makeReservation(
			@CookieValue(required = false) String token,
			@PathVariable Long showId, @RequestBody Set<Integer> seats) {

		var showInfo = ifAuthenticatedDo(token, userId -> {
			return this.cinema.reserve(userId, showId,
					seats);
		});

		return ResponseEntity.ok(showInfo);
	}

	@PostMapping("/shows/{showId}/pay")
	public ResponseEntity<Ticket> payment(
			@CookieValue(required = false) String token,
			@PathVariable Long showId, @RequestBody PaymentRequest payment) {

		var ticket = ifAuthenticatedDo(token, userId -> {
			return this.cinema.pay(userId, showId,
					payment.selectedSeats(), payment.creditCardNumber(),
					payment.toYearMonth(),
					payment.secturityCode());
		});

		return ResponseEntity.ok(ticket);
	}

	@PostMapping("/movies/{movieId}/rate")
	public ResponseEntity<UserMovieRate> rateMovie(
			@CookieValue(required = false) String token,
			@PathVariable Long movieId, @RequestBody RateRequest rateRequest) {

		var userMovieRated = ifAuthenticatedDo(token, userId -> {
			return this.cinema.rateMovieBy(userId, movieId,
					rateRequest.rateValue(), rateRequest.comment());
		});

		return ResponseEntity.ok(userMovieRated);
	}

	private <S> S ifAuthenticatedDo(String token, Function<Long, S> method) {
		var userId = Optional.ofNullable(token).map(tk -> {
			var uid = this.cinema.userIdFrom(tk);
			return uid;
		}).orElseThrow(() -> new AuthException(
				AUTHENTICATION_REQUIRED));

		return method.apply(userId);
	}
}
