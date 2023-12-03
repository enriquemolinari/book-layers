package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.api.BusinessException;
import model.api.UserProfile;

@Entity
@Table(name = "ClientUser")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"userName"})
public class User {

	static final String INVALID_USERNAME = "A valid username must be provided";
	static final String CAN_NOT_CHANGE_PASSWORD = "Some of the provided information is not valid to change the password";
	static final String POINTS_MUST_BE_GREATER_THAN_ZERO = "Points must be greater than zero";
	static final String PASSWORDS_MUST_BE_EQUALS = "Passwords must be equals";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(unique = true)
	private String userName;
	@OneToOne(cascade = CascadeType.PERSIST)
	private Person person;
	// password must not escape by any means out of this object
	@Embedded
	private Password password;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "purchaser")
	private List<Sale> purchases;

	private int points;

	public User(Person person, String userName, String password,
			String repeatPassword) {
		checkPasswordsMatch(password, repeatPassword);
		this.person = person;
		this.userName = new NotBlankString(userName,
				INVALID_USERNAME).value();
		this.password = new Password(password);
		this.points = 0;
		this.purchases = new ArrayList<>();
	}

	private void checkPasswordsMatch(String password, String repeatPassword) {
		if (!password.equals(repeatPassword)) {
			throw new BusinessException(PASSWORDS_MUST_BE_EQUALS);
		}
	}

	boolean hasPassword(String password) {
		return this.password.equals(new Password(password));
	}

	public void changePassword(String currentPassword, String newPassword1,
			String newPassword2) {
		if (!hasPassword(currentPassword)) {
			throw new BusinessException(CAN_NOT_CHANGE_PASSWORD);
		}
		checkPasswordsMatch(newPassword2, newPassword1);

		this.password = new Password(newPassword1);
	}

	void newEarnedPoints(int points) {
		if (points <= 0) {
			throw new BusinessException(POINTS_MUST_BE_GREATER_THAN_ZERO);
		}
		this.points += points;
	}

	public boolean hasPoints(int points) {
		return this.points == points;
	}

	public String userName() {
		return userName;
	}

	public boolean hasName(String aName) {
		return this.person.hasName(aName);
	}

	public boolean hasSurname(String aSurname) {
		return this.person.aSurname(aSurname);
	}

	public boolean hasUsername(String aUserName) {
		return this.userName.equals(aUserName);
	}

	void newPurchase(Sale sale, int pointsWon) {
		this.newEarnedPoints(pointsWon);
		this.purchases.add(sale);
	}

	String email() {
		return this.person.email();
	}

	public Map<String, Object> toMap() {
		return Map.of("id", this.id);
	}

	Long id() {
		return id;
	}

	public UserProfile toProfile() {
		return new UserProfile(this.person.fullName(), this.userName,
				this.person.email(), this.points);
	}
}
