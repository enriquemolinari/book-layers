package model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import model.api.BusinessException;
import model.api.DateTimeProvider;
import model.api.DetailedShowInfo;
import model.api.ShowInfo;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowTime {

	static final String START_TIME_MUST_BE_IN_THE_FUTURE = "The show start time must be in the future";
	static final String PRICE_MUST_BE_POSITIVE = "The price must be greater than zero";
	static final String SELECTED_SEATS_ARE_BUSY = "All or some of the seats chosen are busy";
	static final String RESERVATION_IS_REQUIRED_TO_CONFIRM = "Reservation is required before confirm";
	private static final int DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE = 10;
	static final String SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE = "Show start time must be before movie release date";

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
			throw new BusinessException(
					SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
		}
	}

	public boolean isStartingAt(LocalDateTime of) {
		return this.startTime.equals(startTime);
	}

	private Set<ShowSeat> filterSelectedSeats(Set<Integer> selectedSeats) {
		return this.seatsForThisShow.stream()
				.filter(seat -> seat.isIncludedIn(selectedSeats))
				.collect(Collectors.toUnmodifiableSet());
	}

	void reserveSeatsFor(User user, Set<Integer> selectedSeats) {
		var selection = filterSelectedSeats(selectedSeats);
		checkAllSelectedSeatsAreAvailable(selection);
		reserveAllSeatsFor(user, selection);
	}

	int pointsToEarn() {
		return this.pointsThatAUserWin;
	}

	float totalAmountForTheseSeats(Set<Integer> selectedSeats) {
		return Math.round(selectedSeats.size() * this.price * 100.0f) / 100.0f;
	}

	void confirmSeatsForUser(User user, Set<Integer> selectedSeats) {
		var selection = filterSelectedSeats(selectedSeats);
		checkAllSelectedSeatsAreReservedBy(user, selection);
		confirmAllSeatsFor(user, selection);
	}

	private void checkPriceIsPositiveAndNotFree(float price) {
		if (price <= 0) {
			throw new BusinessException(PRICE_MUST_BE_POSITIVE);
		}
	}

	private void checkStartTimeIsInTheFuture(LocalDateTime startTime) {
		if (startTime.isBefore(this.timeProvider.now())) {
			throw new BusinessException(START_TIME_MUST_BE_IN_THE_FUTURE);
		}
	}

	public boolean hasSeatNumbered(int aSeatNumber) {
		return this.seatsForThisShow.stream()
				.anyMatch(seat -> seat.isSeatNumbered(aSeatNumber));
	}

	boolean noneOfTheSeatsAreReservedBy(User aUser,
			Set<Integer> seatsToReserve) {
		return !areAllSeatsReservedBy(aUser, seatsToReserve);
	}

	public boolean noneOfTheSeatsAreConfirmedBy(User carlos,
			Set<Integer> seatsToConfirmByCarlos) {
		return !areAllSeatsConfirmedBy(carlos, seatsToConfirmByCarlos);
	}

	boolean areAllSeatsConfirmedBy(User aUser, Set<Integer> seatsToReserve) {
		var selectedSeats = filterSelectedSeats(seatsToReserve);
		return allMatchConditionFor(selectedSeats,
				seat -> seat.isConfirmedBy(aUser));
	}

	boolean areAllSeatsReservedBy(User aUser, Set<Integer> seatsToReserve) {
		var selectedSeats = filterSelectedSeats(seatsToReserve);
		return allMatchConditionFor(selectedSeats,
				seat -> seat.isReservedBy(aUser));
	}

	private void checkAtLeastOneMatchConditionFor(Set<ShowSeat> seatsToReserve,
			Predicate<ShowSeat> condition, String errorMsg) {
		if (seatsToReserve.stream().anyMatch(condition)) {
			throw new BusinessException(errorMsg);
		}
	}

	private boolean allMatchConditionFor(Set<ShowSeat> seatsToReserve,
			Predicate<ShowSeat> condition) {
		return seatsToReserve.stream().allMatch(condition);
	}

	private void reserveAllSeatsFor(User user, Set<ShowSeat> selection) {
		selection.stream().forEach(seat -> seat.doReserveForUser(user));
	}

	private void confirmAllSeatsFor(User user, Set<ShowSeat> selection) {
		selection.stream().forEach(seat -> seat.doConfirmForUser(user));
	}

	private void checkAllSelectedSeatsAreAvailable(Set<ShowSeat> selection) {
		checkAtLeastOneMatchConditionFor(selection, seat -> seat.isBusy(),
				SELECTED_SEATS_ARE_BUSY);
	}

	private void checkAllSelectedSeatsAreReservedBy(User user,
			Set<ShowSeat> selection) {
		checkAtLeastOneMatchConditionFor(selection,
				seat -> !seat.isReservedBy(user),
				RESERVATION_IS_REQUIRED_TO_CONFIRM);
	}

	String movieName() {
		return this.movieToBeScreened.name();
	}

	String startDateTime() {
		return new DayTimeFormatted(this.startTime).toString();
	}

	public ShowInfo toShowInfo() {
		return new ShowInfo(this.id, movieName(),
				new MovieDurationFormat(movieToBeScreened.duration())
						.toString(),
				startDateTime(),
				this.price);
	}

	public DetailedShowInfo toDetailedInfo() {
		return new DetailedShowInfo(this.toShowInfo(),
				this.screenedIn.name(),
				this.seatsForThisShow.stream().map(s -> s.toSeat()).toList());
	}

	public List<Integer> confirmedSeatsFrom(User purchaser) {
		return this.seatsForThisShow.stream()
				.filter(seat -> seat.isConfirmedBy(purchaser))
				.map(seat -> seat.seatNumber()).toList();
	}

}
