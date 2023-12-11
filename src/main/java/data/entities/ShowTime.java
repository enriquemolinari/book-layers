package data.entities;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import data.services.DataException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import services.FormattedDayTime;
import services.api.DateTimeProvider;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowTime {

	public static final String START_TIME_MUST_BE_IN_THE_FUTURE = "The show start time must be in the future";
	public static final String PRICE_MUST_BE_POSITIVE = "The price must be greater than zero";
	public static final int DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE = 10;
	public static final String SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE = "Show start time must be before movie release date";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private LocalDateTime startTime;

	@Transient
	// When hibernate creates an instance of this class, this will be
	// null if I don't initialize it here.
	private DateTimeProvider timeProvider = DateTimeProvider.create();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_movie")
	private Movie movieToBeScreened;
	private float price;
	@ManyToOne(fetch = FetchType.LAZY)
	private Theater screenedIn;
	@OneToMany(mappedBy = "show", cascade = CascadeType.PERSIST)
	private Set<ShowSeat> seatsForThisShow;
	@Column(name = "pointsToWin")
	private int pointsThatAUserWin;

	public ShowTime(DateTimeProvider provider, Movie movie,
			LocalDateTime startTime, float price, Theater screenedIn) {
		this(provider, movie, startTime, price, screenedIn,
				DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE);
	}

	public ShowTime(Movie movie, LocalDateTime startTime, float price,
			Theater screenedIn, int totalPointsToWin) {
		this(DateTimeProvider.create(), movie, startTime, price, screenedIn,
				totalPointsToWin);
	}

	public ShowTime(DateTimeProvider provider, Movie movie,
			LocalDateTime startTime, float price, Theater screenedIn,
			int totalPointsToWin) {
		this.timeProvider = provider;
		this.movieToBeScreened = movie;
		checkStartTimeIsInTheFuture(startTime);
		checkPriceIsPositiveAndNotFree(price);
		checkShowStartDateIsGreateThanReleaseDate(startTime, movie);
		this.price = price;
		this.startTime = startTime;
		this.screenedIn = screenedIn;
		this.seatsForThisShow = screenedIn.seatsForShow(this);
		this.pointsThatAUserWin = totalPointsToWin;
	}

	private void checkShowStartDateIsGreateThanReleaseDate(
			LocalDateTime startTime, Movie movie) {
		if (startTime.isBefore(movie.releaseDateAsDateTime())) {
			throw new DataException(
					SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
		}
	}

	public boolean isStartingAt(LocalDateTime of) {
		return this.startTime.equals(startTime);
	}

	public int pointsToEarn() {
		return this.pointsThatAUserWin;
	}

	private void checkPriceIsPositiveAndNotFree(float price) {
		if (price <= 0) {
			throw new DataException(PRICE_MUST_BE_POSITIVE);
		}
	}

	private void checkStartTimeIsInTheFuture(LocalDateTime startTime) {
		if (startTime.isBefore(this.timeProvider.now())) {
			throw new DataException(START_TIME_MUST_BE_IN_THE_FUTURE);
		}
	}

	public boolean hasSeatNumbered(int aSeatNumber) {
		return this.seatsForThisShow.stream()
				.anyMatch(seat -> seat.isSeatNumbered(aSeatNumber));
	}

	public String movieName() {
		return this.movieToBeScreened.name();
	}

	public String startDateTime() {
		return new FormattedDayTime(this.startTime).toString();
	}

	public List<Integer> confirmedSeatsFrom(User purchaser) {
		return this.seatsForThisShow.stream()
				.filter(seat -> seat.isConfirmedBy(purchaser))
				.map(seat -> seat.seatNumber()).toList();
	}

	public Long id() {
		return id;
	}

	public float price() {
		return price;
	}

	public int movieDuration() {
		return this.movieToBeScreened.duration();
	}

	public String screenedIn() {
		return this.screenedIn.name();
	}

	public Set<ShowSeat> seatsForThisShow() {
		return Collections.unmodifiableSet(this.seatsForThisShow);
	}
}
