package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class NewSaleEmailTemplate {

	private float totalAmount;
	private String userName;
	private List<Integer> seatsBought;
	private String movieName;
	private String showStartTime;
	static final String EMAIL_SUBJECT_SALE = "You have new tickets!";

	public NewSaleEmailTemplate(float totalAmount, String userName,
			Set<Integer> seatsBought, String movieName, String showStartTime) {
		this.totalAmount = totalAmount;
		this.userName = userName;
		this.seatsBought = new ArrayList<>(seatsBought);
		Collections.sort(this.seatsBought);
		this.movieName = movieName;
		this.showStartTime = showStartTime;
	}

	public String subject() {
		return EMAIL_SUBJECT_SALE;
	}

	public String body() {
		var body = new StringBuilder();
		body.append("Hello ").append(userName).append("!");
		body.append(System.lineSeparator());
		body.append("You have new tickets!");
		body.append(System.lineSeparator());
		body.append("Here are the details of your booking: ");
		body.append(System.lineSeparator());
		body.append("Movie: ").append(movieName);
		body.append(System.lineSeparator());
		body.append("Seats: ").append(seatsBought.stream()
				.map(s -> s.toString()).collect(Collectors.joining(",")));
		body.append(System.lineSeparator());
		body.append("Show time: ").append(showStartTime);
		body.append(System.lineSeparator());
		body.append("Total paid: ").append(totalAmount);
		return body.toString();
	}
}
