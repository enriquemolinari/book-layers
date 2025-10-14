package data.entities;

import data.repository.DataException;

//TODO: move to cinema
public class NotBlankString {

    private final String value;

    public NotBlankString(String value, String errorMsg) {
        if (value == null || value.isBlank()) {
            throw new DataException(errorMsg);
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
