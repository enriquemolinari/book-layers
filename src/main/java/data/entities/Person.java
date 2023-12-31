package data.entities;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Person {

	public static final String NAME_MUST_NOT_BE_BLANK = "Name must not be blank";
	static final String SURNAME_MUST_NOT_BE_BLANK = "Surname must not be blank";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private String surname;
	@Embedded
	private Email email;

	public Person(String name, String surname, String email) {
		this.name = new NotBlankString(name, NAME_MUST_NOT_BE_BLANK).value();
		this.surname = new NotBlankString(surname, SURNAME_MUST_NOT_BE_BLANK)
				.value();
		this.email = new Email(email);
	}

	public boolean isNamed(String aName) {
		return this.fullName().equals(aName);
	}

	String fullName() {
		return this.name + " " + this.surname;
	}

	public boolean hasName(String aName) {
		return this.name.equals(aName);
	}

	public boolean aSurname(String aSurname) {
		return this.surname.equals(aSurname);
	}

	String email() {
		return this.email.asString();
	}
}
