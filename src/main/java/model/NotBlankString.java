package model;

import model.api.BusinessException;

class NotBlankString {

	private String value;

	public NotBlankString(String value, String errorMsg) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(errorMsg);
		}
		this.value = value;
	}

	public String value() {
		return value;
	}
}
