package model.api;

import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
@Getter(value = AccessLevel.PUBLIC)
public class Ticket {
	private float total;
	private int pointsWon;
	private String salesDate;
	private String username;
	private List<Integer> payedSeats;
	private String movieName;
	private String showStartTime;

	public Ticket(float total, int pointsWon,
			String formattedSalesDate, String userName,
			List<Integer> payedSeats, String movieName, String showStartTime) {
		this.total = total;
		this.pointsWon = pointsWon;
		this.salesDate = formattedSalesDate;
		this.username = userName;
		this.payedSeats = payedSeats;
		this.movieName = movieName;
		this.showStartTime = showStartTime;
	}

	public boolean hasSeats(Set<Integer> seats) {
		return seats.containsAll(seats);
	}

	public boolean isPurchaserUserName(String aUserName) {
		return this.username.equals(aUserName);
	}

	public float total() {
		return this.total;
	}
}
