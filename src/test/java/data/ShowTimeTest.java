package data;

import data.entities.ShowTime;
import data.entities.Theater;
import data.repository.DataException;
import org.junit.jupiter.api.Test;
import services.ForTests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ShowTimeTest {

    private final ForTests tests = new ForTests();

    @Test
    public void showTimeStartTimeMustBeAfterMovieReleaseDate() {
        Exception e = assertThrows(DataException.class, () -> {
            new ShowTime(
                    tests.createSmallFishMovie(LocalDate.now().plusDays(20)),
                    LocalDateTime.now().plusMinutes(10), 10f,
                    new Theater("A Theater", Set.of(1)));
        });

        assertEquals(e.getMessage(),
                ShowTime.SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
    }

    @Test
    public void showTimePriceMustNotBeFree() {
        Exception e = assertThrows(DataException.class, () -> {
            new ShowTime(tests.createSmallFishMovie(),
                    LocalDateTime.now().plusDays(1), 0f, new Theater(
                    "A Theater", Set.of(1)));
        });
        assertEquals(e.getMessage(), ShowTime.PRICE_MUST_BE_POSITIVE);
    }

    @Test
    public void createShowTime() {
        var aShow = tests.createShowForSmallFish();

        assertTrue(aShow.hasSeatNumbered(1));
        assertTrue(aShow.hasSeatNumbered(2));
        assertFalse(aShow.hasSeatNumbered(8));
    }
}
