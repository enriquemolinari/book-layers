package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import data.entities.ShowTime;
import data.entities.Theater;
import data.services.DataException;
import services.api.DateTimeProvider;

public class ShowTimeTest {

	private final ForTests tests = new ForTests();

	@Test
	public void showTimeStartTimeMustBeInTheFuture() {
		Exception e = assertThrows(DataException.class, () -> {
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
		Exception e = assertThrows(DataException.class, () -> {
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
		Exception e = assertThrows(DataException.class, () -> {
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
}
