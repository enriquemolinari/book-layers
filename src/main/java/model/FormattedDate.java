package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class FormattedDate {

	private static String format = "MM-dd-yyyy";
	private LocalDate dateTime;

	public FormattedDate(LocalDate dateTime) {
		this.dateTime = dateTime;
	}

	public String toString() {
		return this.dateTime.format(DateTimeFormatter.ofPattern(format));
	}
}
