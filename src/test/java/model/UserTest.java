package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import model.api.BusinessException;
import model.api.UserProfile;

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
		Exception e = assertThrows(BusinessException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					null, "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(User.INVALID_USERNAME));
	}

	@Test
	public void userNameIsInvalid() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					"", "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(User.INVALID_USERNAME));
	}

	@Test
	public void userEmailIsInvalid() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinarigmail.com"),
					"emolinari", "Ab138RtoUjkL", "Ab138RtoUjkL");
		});

		assertTrue(e.getMessage().equals(Email.NOT_VALID_EMAIL));
	}

	@Test
	public void userPasswordsDoesNotMatch() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					"emolinari", "Ab138RtoUjkL", "Ab13RtoUjkL");
		});

		assertTrue(e.getMessage().equals(User.PASSWORDS_MUST_BE_EQUALS));
	}

	@Test
	public void userPasswordIsInvalid() {
		Exception e = assertThrows(BusinessException.class, () -> {
			new User(
					new Person("Enrique", "Molinari",
							"enrique.molinari@gmail.com"),
					"emolinari", "abcAdif", "abcAdif");
		});

		assertTrue(e.getMessage().equals(Password.NOT_VALID_PASSWORD));
	}

	@Test
	public void changePasswordCurrentPasswordNotTheSame() {
		var u = createUserEnrique();

		Exception e = assertThrows(BusinessException.class, () -> {
			u.changePassword("abchd1239876", "Abcdefghijkl", "Abcdefghijkl");
		});

		assertTrue(e.getMessage().equals(User.CAN_NOT_CHANGE_PASSWORD));
	}

	@Test
	public void changePasswordNewPassword1And2DoesNotMatch() {
		var u = createUserEnrique();

		Exception e = assertThrows(BusinessException.class, () -> {
			u.changePassword("Ab138RtoUjkL", "Abcdefghrjkl", "Abcdefghijkl");
		});

		assertTrue(e.getMessage().equals(User.PASSWORDS_MUST_BE_EQUALS));
	}

	@Test
	public void changePasswordNewPasswordNotValid() {
		var u = createUserEnrique();

		Exception e = assertThrows(BusinessException.class, () -> {
			u.changePassword("Ab138RtoUjkL", "Abcdefgh", "Abcdefgh");
		});

		assertTrue(e.getMessage().equals(Password.NOT_VALID_PASSWORD));
	}

	@Test
	public void changePasswordOk() {
		var u = createUserEnrique();

		u.changePassword("Ab138RtoUjkL", "Abcdefghijkl", "Abcdefghijkl");

		assertTrue(u.hasPassword("Abcdefghijkl"));
	}

	@Test
	public void newCreatedUserHasZeroPoints() {
		var u = createUserEnrique();
		assertTrue(u.hasPoints(0));
	}

	@Test
	public void userProfile() {
		var u = createUserEnrique();

		assertEquals(new UserProfile("Enrique Molinari", "enriquemolinari",
				"enrique.molinari@gmail.com", 0), u.toProfile());
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

		Exception e = assertThrows(BusinessException.class, () -> {
			u.newEarnedPoints(0);
		});

		assertTrue(
				e.getMessage().equals(User.POINTS_MUST_BE_GREATER_THAN_ZERO));
	}

}
