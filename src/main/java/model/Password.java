package model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.api.BusinessException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"password"})
class Password {
	private String password;
	static final String NOT_VALID_PASSWORD = "Password is not valid";

	public Password(String password) {
		String pwd = new NotBlankString(password, NOT_VALID_PASSWORD).value();
		if (pwd.length() < 12) {
			throw new BusinessException(NOT_VALID_PASSWORD);
		}
		// hash password before assign !
		this.password = password;
	}

}
