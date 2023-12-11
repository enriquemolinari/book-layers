package services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import data.entities.Email;
import data.entities.Password;
import data.entities.Person;
import data.entities.User;
import data.services.DataException;

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
		var u = new User(
				new Person("Enrique", "Molinari", "enrique.molinari@gmail.com"),
				"enriquemolinari", "Ab138RtoUjkL", "Ab138RtoUjkL");
		return u;
	}

	@Test
	public void userNameIsInvalidWithNull() {
		Exception e = assertThrows(DataException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					null, "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(User.INVALID_USERNAME));
	}

	@Test
	public void userNameIsInvalid() {
		Exception e = assertThrows(DataException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					"", "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(User.INVALID_USERNAME));
	}

	@Test
	public void userEmailIsInvalid() {
		Exception e = assertThrows(DataException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinarigmail.com"),
					"emolinari", "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(Email.NOT_VALID_EMAIL));
	}

	@Test
	public void userPasswordIsInvalid() {
		Exception e = assertThrows(DataException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					"emolinari", "abcAdif", "abcAdif");
		});

		assertTrue(e.getMessage().equals(Password.NOT_VALID_PASSWORD));
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

		assertTrue(
				e.getMessage().equals(User.POINTS_MUST_BE_GREATER_THAN_ZERO));
	}

}
