package data;

import data.entities.Actor;
import data.entities.Genre;
import data.entities.Movie;
import data.entities.Person;
import data.repository.DataException;
import org.junit.jupiter.api.Test;
import services.ForTests;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTest {

    private final ForTests tests = new ForTests();

    @Test
    public void smallFishMovie() {
        var smallFish = tests.createSmallFishMovie();

        assertTrue(smallFish.hasDurationOf(102));
        assertFalse(smallFish.hasDurationOf(10));
        assertTrue(smallFish.isNamed("Small Fish"));
        assertTrue(smallFish.isCharacterNamed("George Bix"));
        assertTrue(smallFish.hasReleaseDateOf(LocalDate.of(2023, 10, 10)));
        assertTrue(smallFish.hasGenresOf(List.of(Genre.COMEDY, Genre.ACTION)));
        assertTrue(smallFish.hasARole("aName aSurname"));
        assertTrue(smallFish.isDirectedBy("aDirectorName aDirectorSurname"));
    }

    @Test
    public void movieNameIsInvalid() {
        Exception e = assertThrows(DataException.class, () -> {
            new Movie("  ", "plot ...", 102,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.COMEDY, Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });

        assertEquals(Movie.MOVIE_NAME_INVALID, e.getMessage());
    }

    @Test
    public void durationIsInvalid() {
        Exception e = assertThrows(DataException.class, () -> {
            new Movie("Small Fish", "plot...", 0,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.COMEDY, Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });
        assertEquals(Movie.DURATION_INVALID, e.getMessage());
    }

    @Test
    public void genreIsInvalid() {
        Exception e = assertThrows(DataException.class, () -> {
            new Movie("Small Fish", "plot...", 100,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of()/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });
        assertEquals(Movie.GENRES_INVALID, e.getMessage());
    }

    @Test
    public void directorsWithBlankNames() {
        Exception e = assertThrows(DataException.class, () -> {
            new Movie("Small Fish", "plot...", 100,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person(" ", "aSurname",
                                    "anotherEmail@mail.com"),
                            new Person("aName", "aSurname",
                                    "anotherOtherEmail@mail.com")));
        });
        assertEquals(Person.NAME_MUST_NOT_BE_BLANK, e.getMessage());
    }

    @Test
    public void newCreatedMovieHasCeroRate() {
        var smallFish = tests.createSmallFishMovie();
        assertTrue(smallFish.hasRateValue(0));
    }
}
