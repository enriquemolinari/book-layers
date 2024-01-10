package data.entities;

import data.services.DataException;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"password"})
public class Password {
    private String password;
    public static final String NOT_VALID_PASSWORD = "Password is not valid";

    //TODO: move to cinema...
    public Password(String password) {
        String pwd = new NotBlankString(password, NOT_VALID_PASSWORD).value();
        if (pwd.length() < 12) {
            throw new DataException(NOT_VALID_PASSWORD);
        }
        // hash password before assign !
        this.password = password;
    }

}
