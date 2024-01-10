package data.entities;

import data.services.DataException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Email {

    private String email;
    public static final String NOT_VALID_EMAIL = "Email address is not valid";
    private static final String REGEX = "^[\\w-_.+]*[\\w-_.]@(\\w+\\.)+\\w+\\w$";

    public Email(String email) {
        if (!email.matches(REGEX)) {
            throw new DataException(NOT_VALID_EMAIL);
        }
        this.email = email;
    }

    public String asString() {
        return email;
    }
}
