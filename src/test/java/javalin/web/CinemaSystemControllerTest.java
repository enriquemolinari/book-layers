package javalin.web;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.time.YearMonth;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;
import jakarta.persistence.Persistence;
import model.Cinema;
import model.token.PasetoToken;
import spring.main.SetUpDb;

public class CinemaSystemControllerTest {
	private static final String INFO_KEY = "info";
	private static final String CURRENT_SEATS_KEY = "currentSeats";
	private static final String PASSWORD_KEY = "password";
	private static final String JOSE_FULLNAME = "Josefina Simini";
	private static final String JOSE_EMAIL = "jsimini@mymovies.com";
	private static final String POINTS_KEY = "points";
	private static final String EMAIL_KEY = "email";
	private static final String FULLNAME_KEY = "fullname";
	private static final String COMMENT_KEY = "comment";
	private static final String RATE_VALUE_KEY = "rateValue";
	private static final String USERNAME_KEY = "username";
	private static final String JSON_ROOT = "$";
	private static final String SHOW_MOVIE_NAME_KEY = "movieName";
	private static final String ROCK_IN_THE_SCHOOL_MOVIE_NAME = "Rock in the School";
	private static final String RUNNING_FAR_AWAY_MOVIE_NAME = "Running far Away";
	private static final String SMALL_FISH_MOVIE_NAME = "Small Fish";
	private static final String CRASH_TEA_MOVIE_NAME = "Crash Tea";
	private static final String PASSWORD_JOSE = "123456789012";
	private static final String USERNAME_JOSE = "jsimini";
	private static final String ERROR_MESSAGE_KEY = "message";
	private static final String TOKEN_COOKIE_NAME = "token";
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static String URL = "http://localhost:8080";

	@BeforeAll
	public static void before() {
		String SECRET = "Kdj5zuBIBBgcWpv9zjKOINl2yUKUXVKO+SkOVE3VuZ4=";

		var emf = Persistence
				.createEntityManagerFactory("test-derby-cinema");

		new SetUpDb(emf).createSchemaAndPopulateSampleData();

		var cinema = new Cinema(emf,
				(String creditCardNumber, YearMonth expire, String securityCode,
						float totalAmount) -> {
				},
				(String to, String subject, String body) -> {
				},
				new PasetoToken(SECRET), 2 /* page size */);

		new CinemaSystemController(8080, cinema).start();
	}

	@Test
	public void loginOk() throws JSONException {
		var response = loginAsJosePost();

		response.then().body(FULLNAME_KEY, is(JOSE_FULLNAME))
				.body(USERNAME_KEY, is(USERNAME_JOSE))
				.body(EMAIL_KEY, is(JOSE_EMAIL))
				.body(POINTS_KEY, equalTo(0))
				.cookie(TOKEN_COOKIE_NAME, containsString("v2.local"));
	}

	@Test
	public void rateMovieFailIfNotAuthenticated() throws JSONException {
		JSONObject rateRequestBody = new JSONObject();
		rateRequestBody.put(RATE_VALUE_KEY, 4);
		rateRequestBody.put(COMMENT_KEY, "a comment...");

		var response = given().contentType(JSON_CONTENT_TYPE)
				.body(rateRequestBody.toString())
				.post(URL + "/movies/1/rate");

		response.then().body(ERROR_MESSAGE_KEY,
				is(CinemaSystemController.AUTHENTICATION_REQUIRED));
	}

	@Test
	public void showOneOk() {
		var response = get(URL + "/shows/1");
		// To avoid fragile tests, I use oneOf, as the movie assigned to show 1
		// might change
		response.then().body("info." + SHOW_MOVIE_NAME_KEY,
				is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
						RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
		response.then().body("info.showId", is(1));
		response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
		response.then().body(INFO_KEY, hasKey("movieDuration"));
	}

	@Test
	public void rateMovieOk() throws JSONException {
		var token = loginAsJoseAndGetCookie();

		JSONObject rateRequestBody = new JSONObject();
		rateRequestBody.put(RATE_VALUE_KEY, 4);
		rateRequestBody.put(COMMENT_KEY, "a comment...");

		var response = given().contentType(JSON_CONTENT_TYPE)
				.cookie(TOKEN_COOKIE_NAME, token)
				.body(rateRequestBody.toString())
				.post(URL + "/movies/2/rate");

		response.then().body(USERNAME_KEY, is(USERNAME_JOSE))
				.body(RATE_VALUE_KEY, is(4))
				.body(COMMENT_KEY, is("a comment..."));
	}

	private String loginAsJoseAndGetCookie() {
		var loginResponse = loginAsJosePost();
		var token = getCookie(loginResponse);
		return token;
	}

	private String getCookie(Response loginResponse) {
		var token = loginResponse.getCookie(TOKEN_COOKIE_NAME);
		return token;
	}

	private Response loginAsJosePost() {
		return loginAsPost(USERNAME_JOSE, PASSWORD_JOSE);
	}

	private Response loginAsPost(String userName, String password) {
		JSONObject loginRequestBody = new JSONObject();
		try {
			loginRequestBody.put(USERNAME_KEY, userName);
			loginRequestBody.put(PASSWORD_KEY, password);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		var response = given().contentType(JSON_CONTENT_TYPE)
				.body(loginRequestBody.toString())
				.post(URL + "/login");
		return response;
	}
}
