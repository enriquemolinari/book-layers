package data.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Sale {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private float total;
	private LocalDateTime salesDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_user")
	private User purchaser;

	private int pointsWon;

	@ManyToOne
	@JoinColumn(name = "id_showtime")
	private ShowTime soldShow;

	private Set<Integer> selectedSeats;

	public Sale(float totalAmount, User userThatPurchased, ShowTime soldShow,
			int pointsWon, Set<Integer> selectedSeats) {
		this.total = totalAmount;
		this.purchaser = userThatPurchased;
		this.soldShow = soldShow;
		this.selectedSeats = selectedSeats;
		this.salesDate = LocalDateTime.now();
		this.pointsWon = pointsWon;
		userThatPurchased.newPurchase(this, pointsWon);
	}

	public boolean hasTotalOf(float aTotal) {
		return this.total == aTotal;
	}

	boolean purchaseBy(User aUser) {
		return this.purchaser.equals(aUser);
	}

	public List<Integer> confirmedSeatNumbers() {
		return this.selectedSeats.stream().toList();
	}

	public float total() {
		return this.total;
	}

	public int pointsWon() {
		return this.pointsWon;
	}

	public LocalDateTime salesDate() {
		return this.salesDate;
	}

	public String purchaserUserName() {
		return this.purchaser.userName();
	}

	public String movieName() {
		return this.soldShow.movieName();
	}

	public LocalDateTime showStartTime() {
		return this.soldShow.startDateTime();
	}
}
