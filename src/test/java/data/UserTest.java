package data;

import data.entities.Email;
import data.entities.Person;
import data.entities.User;
import data.repository.DataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void userCanBeCreated() {
        var u = createUserEnrique();

        assertTrue(u.hasPassword("Ab138RtoUjkL"));
        assertTrue(u.hasName("Enrique"));
        assertTrue(u.hasSurname("Molinari"));
        assertTrue(u.hasUsername("enriquemolinari"));
    }

    private User createUserEnrique() {
        return new User(
                new Person("Enrique", "Molinari", "enrique.molinari@gmail.com"),
                "enriquemolinari", "Ab138RtoUjkL");
    }

    @Test
    public void userNameIsInvalidWithNull() {
        Exception e = assertThrows(DataException.class, () -> {
            new User(
                    new Person("Enrique", "Molinari",
                            "enrique.molinari@gmail.com"),
                    null, "Ab138RtoUjkL");
        });
        assertEquals(User.INVALID_USERNAME, e.getMessage());
    }

    @Test
    public void userNameIsInvalid() {
        Exception e = assertThrows(DataException.class, () -> {
            new User(
                    new Person("Enrique", "Molinari",
                            "enrique.molinari@gmail.com"),
                    "", "Ab138RtoUjkL");
        });
        assertEquals(User.INVALID_USERNAME, e.getMessage());
    }

    @Test
    public void userEmailIsInvalid() {
        Exception e = assertThrows(DataException.class, () -> {
            new User(
                    new Person("Enrique", "Molinari",
                            "enrique.molinarigmail.com"),
                    "emolinari", "Ab138RtoUjkL");
        });
        assertEquals(Email.NOT_VALID_EMAIL, e.getMessage());
    }

    @Test
    public void newCreatedUserHasZeroPoints() {
        var u = createUserEnrique();
        assertTrue(u.hasPoints(0));
    }

    @Test
    public void userEarnsSomePoints() {
        var u = createUserEnrique();
        u.newEarnedPoints(10);
        assertTrue(u.hasPoints(10));
    }

    @Test
    public void userEarnsAnInvalidNumberOfPoints() {
        var u = createUserEnrique();
        Exception e = assertThrows(DataException.class, () -> {
            u.newEarnedPoints(0);
        });
        assertEquals(User.POINTS_MUST_BE_GREATER_THAN_ZERO, e.getMessage());
    }
}
