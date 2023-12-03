package model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.api.BusinessException;
import model.api.DateTimeProvider;
import model.api.Seat;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
class ShowSeat {

	static final int MINUTES_TO_KEEP_RESERVATION = 5;
	static final String SEAT_BUSY = "Seat is currently busy";
	static final String SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED = "The seat cannot be confirmed";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	private boolean reserved;
	private boolean confirmed;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_show")
	private ShowTime show;
	private LocalDateTime reservedUntil;
	private Integer seatNumber;
	@Version
	private int version;

	@Transient
	private DateTimeProvider provider = DateTimeProvider.create();

	public ShowSeat(ShowTime s, Integer seatNumber,
			DateTimeProvider dateProvider) {
		this.show = s;
		this.seatNumber = seatNumber;

		this.reserved = false;
		this.confirmed = false;
		this.provider = dateProvider;
	}

	public void doReserveForUser(User user) {
		if (!isAvailable()) {
			throw new BusinessException(SEAT_BUSY);
		}

		this.reserved = true;
		this.user = user;
		this.reservedUntil = provider.now()
				.plusMinutes(MINUTES_TO_KEEP_RESERVATION);
	}

	public boolean isBusy() {
		return !isAvailable();
	}

	private boolean isAvailable() {
		return !reserved && !confirmed;
	}

	public void doConfirmForUser(User user) {
		if (!isReservedBy(user) || confirmed) {
			throw new BusinessException(SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED);
		}

		this.confirmed = true;
		this.user = user;
	}

	boolean isConfirmedBy(User user) {
		if (this.user == null) {
			return false;
		}
		return confirmed && this.user.equals(user);
	}

	boolean isReservedBy(User user) {
		if (this.user == null) {
			return false;
		}
		return reserved && this.user.equals(user)
				&& LocalDateTime.now().isBefore(this.reservedUntil);
	}

	public boolean isSeatNumbered(int aSeatNumber) {
		return this.seatNumber.equals(aSeatNumber);
	}

	public boolean isIncludedIn(Set<Integer> selectedSeats) {
		return selectedSeats.stream()
				.anyMatch(ss -> ss.equals(this.seatNumber));
	}

	int seatNumber() {
		return seatNumber;
	}

	public Seat toSeat() {
		return new Seat(seatNumber, isAvailable());
	}
}
