package data.entities;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
class Rating {

	private int totalUserVotes = 0;
	private float rateValue = 0;
	private float totalValue = 0;

	public static Rating notRatedYet() {
		return new Rating();
	}

	String actualRateAsString() {
		return String.valueOf(this.rateValue);
	}

	float actualRate() {
		return this.rateValue;
	}

	boolean hasValue(float aValue) {
		return this.rateValue == aValue;
	}

	public boolean hastTotalVotesOf(int votes) {
		return this.totalUserVotes == votes;
	}

	int totalVotes() {
		return this.totalUserVotes;
	}

	float totalValue() {
		return this.totalValue;
	}

	public void setValue(float newMovieValue) {
		this.rateValue = newMovieValue;
	}

	public void addTotalValue(int userValue) {
		this.totalValue += userValue;
		this.totalUserVotes += 1;
	}

}
