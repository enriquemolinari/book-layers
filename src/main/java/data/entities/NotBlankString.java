package data.entities;

import data.services.DataException;

class NotBlankString {

	private String value;

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
