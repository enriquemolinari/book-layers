package model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Actor {

	static final String NAME_INVALID = "name must not be blank";
	static final String CHARACTER_NAME_INVALID = "character name must not be blank";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_person")
	private Person person;
	private String characterName;

	public Actor(Person person, String characterName) {
		this.person = person;
		this.characterName = new NotBlankString(characterName,
				CHARACTER_NAME_INVALID).value();
	}

	public boolean isNamed(String aName) {
		return this.person.isNamed(aName);
	}

	public boolean hasCharacterName(String aCharacterName) {
		return this.characterName.equals(aCharacterName);
	}

	String fullName() {
		return this.person.fullName();
	}

	String characterName() {
		return this.characterName;
	}
}
