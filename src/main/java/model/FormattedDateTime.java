package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class FormattedDateTime {

	private static String format = "MM-dd-yyyy HH:mm";
	private LocalDateTime dateTime;

	public FormattedDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public String toString() {
		return this.dateTime.format(DateTimeFormatter.ofPattern(format));
	}
}
