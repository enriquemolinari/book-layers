package data.entities;

import data.services.DataException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowTime {

    public static final String PRICE_MUST_BE_POSITIVE = "The price must be greater than zero";
    public static final int DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE = 10;
    public static final String SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE = "Show start time must be before movie release date";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movie")
    private Movie movieToBeScreened;
    private float price;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theater screenedIn;
    @OneToMany(mappedBy = "show", cascade = CascadeType.PERSIST)
    private Set<ShowSeat> seatsForThisShow;
    @Column(name = "pointsToWin")
    private int pointsThatAUserWin;

    public ShowTime(Movie movie,
                    LocalDateTime startTime, float price, Theater screenedIn) {
        this(movie, startTime, price, screenedIn,
                DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE);
    }

    public ShowTime(Movie movie,
                    LocalDateTime startTime, float price, Theater screenedIn,
                    int totalPointsToWin) {
        this.movieToBeScreened = movie;
        checkPriceIsPositiveAndNotFree(price);
        checkShowStartDateIsGreateThanReleaseDate(startTime, movie);
        this.price = price;
        this.startTime = startTime;
        this.screenedIn = screenedIn;
        this.seatsForThisShow = screenedIn.seatsForShow(this);
        this.pointsThatAUserWin = totalPointsToWin;
    }

    private void checkShowStartDateIsGreateThanReleaseDate(
            LocalDateTime startTime, Movie movie) {
        if (startTime.isBefore(movie.releaseDateAsDateTime())) {
            throw new DataException(
                    SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
        }
    }
	
    public int pointsToEarn() {
        return this.pointsThatAUserWin;
    }

    private void checkPriceIsPositiveAndNotFree(float price) {
        if (price <= 0) {
            throw new DataException(PRICE_MUST_BE_POSITIVE);
        }
    }

    public boolean hasSeatNumbered(int aSeatNumber) {
        return this.seatsForThisShow.stream()
                .anyMatch(seat -> seat.isSeatNumbered(aSeatNumber));
    }

    public String movieName() {
        return this.movieToBeScreened.name();
    }

    public LocalDateTime startDateTime() {
        return this.startTime;
    }

    public Long id() {
        return id;
    }

    public float price() {
        return price;
    }

    public int movieDuration() {
        return this.movieToBeScreened.duration();
    }

    public String screenedIn() {
        return this.screenedIn.name();
    }

    public Set<ShowSeat> seatsForThisShow() {
        return Collections.unmodifiableSet(this.seatsForThisShow);
    }
}
