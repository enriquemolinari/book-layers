package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.api.ActorInMovieName;
import model.api.BusinessException;
import model.api.Genre;
import model.api.MovieInfo;
import model.api.MovieShows;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Movie {

	static final String MOVIE_PLOT_INVALID = "Movie plot must not be null or blank";
	static final String MOVIE_NAME_INVALID = "Movie name must not be null or blank";
	static final String DURATION_INVALID = "Movie's duration must be greater than 0";
	static final String GENRES_INVALID = "You must add at least one genre to the movie";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private int duration;
	private LocalDate releaseDate;
	private String plot;

	@ElementCollection(targetClass = Genre.class)
	@CollectionTable
	@Enumerated(EnumType.STRING)
	private Set<Genre> genres;
	@OneToMany(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_movie")
	private List<Actor> actors;
	@ManyToMany(cascade = CascadeType.PERSIST)
	private List<Person> directors;
	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "movie")
	// List does not load the entire collection for adding new elements
	// if there is a bidirectional mapping
	private List<UserRate> userRates;

	// this is pre-calculated rating for this movie
	@Embedded
	private Rating rating;

	@OneToMany(mappedBy = "movieToBeScreened")
	private List<ShowTime> showTimes;

	public Movie(String name, String plot, int duration, LocalDate releaseDate,
			Set<Genre> genres, List<Actor> actors, List<Person> directors) {
		checkDurationGreaterThanZero(duration);
		checkGenresAtLeastHasOne(genres);
		this.name = new NotBlankString(name, MOVIE_NAME_INVALID).value();
		this.plot = new NotBlankString(plot, MOVIE_PLOT_INVALID).value();
		this.duration = duration;
		this.releaseDate = releaseDate;
		this.genres = genres;
		this.actors = actors;
		this.directors = directors;
		this.userRates = new ArrayList<>();
		this.rating = Rating.notRatedYet();
	}

	public Movie(String name, String plot, int duration, LocalDate releaseDate,
			Set<Genre> genres) {
		this(name, plot, duration, releaseDate, genres, new ArrayList<Actor>(),
				new ArrayList<Person>());
	}

	private <T> void checkCollectionSize(Set<T> collection, String errorMsg) {
		if (collection.size() == 0) {
			throw new BusinessException(errorMsg);
		}
	}

	private void checkGenresAtLeastHasOne(Set<Genre> genres) {
		checkCollectionSize(genres, GENRES_INVALID);
	}

	private void checkDurationGreaterThanZero(int duration) {
		if (duration <= 0) {
			throw new BusinessException(DURATION_INVALID);
		}
	}

	public boolean hasDurationOf(int aDuration) {
		return this.duration == aDuration;
	}

	public boolean isNamed(String aName) {
		return this.name.equals(aName);
	}

	public boolean isNamedAs(Movie aMovie) {
		return this.name.equals(aMovie.name);
	}

	public boolean hasReleaseDateOf(LocalDate aDate) {
		return releaseDate.equals(aDate);
	}

	public boolean hasGenresOf(List<Genre> genddres) {
		return this.genres.stream().allMatch(g -> genddres.contains(g));
	}

	public boolean hasARole(String anActorName) {
		return this.actors.stream().anyMatch(a -> a.isNamed(anActorName));
	}

	public boolean isCharacterNamed(String aCharacterName) {
		return this.actors.stream()
				.anyMatch(a -> a.hasCharacterName(aCharacterName));
	}

	public boolean isDirectedBy(String aDirectorName) {
		return this.directors.stream().anyMatch(d -> d.isNamed(aDirectorName));
	}

	public UserRate rateBy(User user, int value, String comment) {
		// Ideally validating logic that a user does not rate the same
		// movie twice should be here. However, to do that Hibernate will
		// load the entire collection in memory. That
		// would hurt performance as the collection gets bigger.
		// This validation gets performed in Cimema.
		var userRate = new UserRate(user, value, comment, this);
		this.rating.calculaNewRate(value);
		this.userRates.add(userRate);
		return userRate;
	}

	boolean hasRateValue(float aValue) {
		return this.rating.hasValue(aValue);
	}

	public boolean hasTotalVotes(int votes) {
		return this.rating.hastTotalVotesOf(votes);
	}

	String name() {
		return this.name;
	}

	public MovieShows toMovieShow() {
		return new MovieShows(this.id, this.name,
				new MovieDurationFormat(duration).toString(),
				genreAsListOfString(), this.showTimes.stream()
						.map(show -> show.toShowInfo()).toList());
	}

	public void addAnActor(String name, String surname, String email,
			String characterName) {
		this.actors.add(
				new Actor(new Person(name, surname, email), characterName));
	}

	public void addADirector(String name, String surname, String email) {
		this.directors.add(new Person(name, surname, email));
	}

	public MovieInfo toInfo() {
		return new MovieInfo(id, name,
				new MovieDurationFormat(duration).toString(), plot,
				genreAsListOfString(), directorsNamesAsString(),
				new FormattedDate(releaseDate).toString(),
				rating.actualRateAsString(), rating.totalVotes(),
				toActorsInMovieNames());
	}

	private List<String> directorsNamesAsString() {
		return directors.stream().map(d -> d.fullName()).toList();
	}

	private List<ActorInMovieName> toActorsInMovieNames() {
		return this.actors.stream()
				.map(actor -> new ActorInMovieName(actor.fullName(),
						actor.characterName()))
				.toList();
	}

	private Set<String> genreAsListOfString() {
		return this.genres.stream().map(g -> capitalizeFirstLetter(g.name()))
				.collect(Collectors.toSet());
	}

	private String capitalizeFirstLetter(String aString) {
		return aString.substring(0, 1).toUpperCase()
				+ aString.substring(1).toLowerCase();
	}

	int duration() {
		return this.duration;
	}

	LocalDateTime releaseDateAsDateTime() {
		return this.releaseDate.atTime(0, 0);
	}
}
