package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import model.api.BusinessException;
import model.api.DateTimeProvider;

public class ShowTimeTest {

	private final ForTests tests = new ForTests();

	@Test
	public void showTimeStartTimeMustBeInTheFuture() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new ShowTime(DateTimeProvider.create(),
					tests.createSmallFishMovie(),
					LocalDateTime.of(2023, 03, 10, 15, 0, 0, 0), 10f,
					new Theater("A Theater", Set.of(1),
							DateTimeProvider.create()));
		});

		assertEquals(e.getMessage(), ShowTime.START_TIME_MUST_BE_IN_THE_FUTURE);
	}

	@Test
	public void showTimeStartTimeMustBeAfterMovieReleaseDate() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new ShowTime(DateTimeProvider.create(),
					tests.createSmallFishMovie(LocalDate.now().plusDays(20)),
					LocalDateTime.now().plusMinutes(10), 10f,
					new Theater("A Theater", Set.of(1),
							DateTimeProvider.create()));
		});

		assertEquals(e.getMessage(),
				ShowTime.SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
	}

	@Test
	public void showTimePriceMustNotBeFree() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new ShowTime(DateTimeProvider.create(),
					tests.createSmallFishMovie(),
					LocalDateTime.now().plusDays(1), 0f, new Theater(
							"A Theater", Set.of(1), DateTimeProvider.create()));
		});

		assertEquals(e.getMessage(), ShowTime.PRICE_MUST_BE_POSITIVE);
	}

	@Test
	public void createShowTime() {
		var aShow = tests.createShowForSmallFish();

		assertTrue(aShow.hasSeatNumbered(1));
		assertTrue(aShow.hasSeatNumbered(2));
		assertFalse(aShow.hasSeatNumbered(8));
		assertTrue(aShow
				.isStartingAt(LocalDateTime.of(2023, 10, 10, 15, 0, 0, 0)));
	}

	@Test
	public void reservationHasExpired() {
		var aShow = tests.createShowForSmallFish(
				// already expired reservation
				() -> LocalDateTime.now().minusMinutes(6));
		var carlos = createCarlosUser();

		var seatsToReserve = Set.of(1, 2);
		aShow.reserveSeatsFor(carlos, seatsToReserve);

		assertFalse(aShow.areAllSeatsReservedBy(carlos, seatsToReserve));
	}

	@Test
	public void reserveAnAvailableSeat() {
		var aShow = tests.createShowForSmallFish();
		var carlos = createCarlosUser();

		var seatsToReserve = Set.of(1, 2);
		aShow.reserveSeatsFor(carlos, seatsToReserve);

		assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserve));
	}

	@Test
	public void reserveAlreadyReservedSeats() {
		var aShow = tests.createShowForSmallFish();
		var carlos = createCarlosUser();
		var jose = createJoseUser();

		var seatsToReserveByCarlos = Set.of(1, 2);
		aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos);

		var seatsToTryReserveByJose = Set.of(2, 3);
		Exception e = assertThrows(BusinessException.class, () -> {
			aShow.reserveSeatsFor(carlos, seatsToTryReserveByJose);
		});

		assertEquals(e.getMessage(), ShowTime.SELECTED_SEATS_ARE_BUSY);
		assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserveByCarlos));
		assertTrue(aShow.noneOfTheSeatsAreReservedBy(jose,
				seatsToTryReserveByJose));
	}

	@Test
	public void confirmReservedSeats() {
		var aShow = tests.createShowForSmallFish();
		var carlos = createCarlosUser();

		var seatsToReserveByCarlos = Set.of(1, 2);
		aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos);

		var seatsToConfirmByCarlos = Set.of(1, 2);
		aShow.confirmSeatsForUser(carlos, seatsToConfirmByCarlos);

		assertTrue(
				aShow.areAllSeatsConfirmedBy(carlos, seatsToConfirmByCarlos));

		assertEquals(seatsToConfirmByCarlos,
				Set.copyOf(aShow.confirmedSeatsFrom(carlos)));

		assertTrue(aShow.noneOfTheSeatsAreConfirmedBy(carlos,
				Set.of(3, 4, 5, 6)));
	}

	@Test
	public void notAllSeatsAreReserved() {
		var aShow = tests.createShowForSmallFish();
		var carlos = createCarlosUser();

		aShow.reserveSeatsFor(carlos, Set.of(1, 2));

		assertFalse(aShow.areAllSeatsReservedBy(carlos, Set.of(1, 2, 5)));
	}

	@Test
	public void confirmNonReservedSeats() {
		var aShow = tests.createShowForSmallFish();
		var carlos = createCarlosUser();

		var seatsToReserveByCarlos = Set.of(1, 2, 4, 5);
		aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos);

		var seatsToConfirmByCarlos = Set.of(5, 6, 7);

		Exception e = assertThrows(BusinessException.class, () -> {
			aShow.confirmSeatsForUser(carlos, seatsToConfirmByCarlos);
		});

		assertEquals(e.getMessage(),
				ShowTime.RESERVATION_IS_REQUIRED_TO_CONFIRM);
		assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserveByCarlos));
		assertTrue(aShow.noneOfTheSeatsAreConfirmedBy(carlos,
				seatsToConfirmByCarlos));
	}

	private User createCarlosUser() {
		return new User(new Person("Carlos", "Garzia", "cgarzia@my.com"),
				"cgarzia", "123456789101112", "123456789101112");
	}

	private User createJoseUser() {
		return new User(new Person("Jose", "Lopiz", "jlopiz@my.com"), "jlopiz",
				"123456789101112",
				"123456789101112");
	}

}
