package data.entities;

import java.time.LocalDateTime;

import data.services.DataException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@Table(uniqueConstraints = {
		@UniqueConstraint(name = "USER_CANT_RATE_A_MOVIE_MORE_THAN_ONCE", columnNames = {
				"movie_id", "user_id"})})
public class UserRate {

	static final String INVALID_RATING = "Rate value must be an integer value between 0 and 5";
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	private int value;
	private String comment;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id")
	private Movie movie;
	private LocalDateTime ratedAt;

	public UserRate(User user, int value, String comment, Movie movie,
			LocalDateTime ratedAt) {
		checkValidRateValue(value);

		this.user = user;
		this.value = value;
		this.comment = comment;
		this.movie = movie;
		this.ratedAt = ratedAt;
	}

	private void checkValidRateValue(int value) {
		if (value < 0 || value > 5) {
			throw new DataException(INVALID_RATING);
		}
	}

	public boolean isRatedBy(User aUser) {
		return this.user.equals(aUser);
	}

	public String userName() {
		return this.user.userName();
	}

	public int rateValue() {
		return this.value;
	}

	public LocalDateTime ratedAt() {
		return this.ratedAt;
	}

	public String comment() {
		return this.comment;
	}
}
