package spring.web;

import data.services.CinemaRepository;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import spring.main.Main;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// define a test profile to create the in memory database and sample data
// see spring.main.AppConfiguration and spring.main.PersistenceConfiguration
// classes
@ActiveProfiles(value = "test")
public class CinemaSystemControllerTest {

    private static final String CHANGE_PASS_BODY_CURRENT_PASS = "currentPassword";
    private static final String CHANGE_PASS_BODY_PASSWORD1 = "newPassword1";
    private static final String CHANGE_PASS_BODY_PASSWORD2 = "newPassword2";
    private static final String INFO_KEY = "info";
    private static final String SEAT_AVAILABLE_KEY = "available";
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
    private static final String MOVIE_NAME_KEY = "name";
    private static final String MOVIE_ACTORS_KEY = "actors";
    private static final String MOVIE_RATING_TOTAL_VOTES_KEY = "ratingTotalVotes";
    private static final String MOVIE_RATING_VALUE_KEY = "ratingValue";
    private static final String MOVIE_RELEASE_DATE_KEY = "releaseDate";
    private static final String MOVIE_DIRECTORS_KEY = "directorNames";
    private static final String MOVIE_DURATION_KEY = "duration";
    private static final String MOVIE_PLOT_KEY = "plot";
    private static final String MOVIE_GENRES_KEY = "genres";
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
    private static final String URL = "http://localhost:8080";

    @Test
    public void loginOk() {
        var response = loginAsJosePost();
        response.then().body(FULLNAME_KEY, is(JOSE_FULLNAME))
                .body(USERNAME_KEY, is(USERNAME_JOSE))
                .body(EMAIL_KEY, is(JOSE_EMAIL))
                .body(POINTS_KEY, equalTo(0))
                .cookie(TOKEN_COOKIE_NAME, containsString("v2.local"));
    }

    private Response loginAsJosePost() {
        return loginAsPost(USERNAME_JOSE, PASSWORD_JOSE);
    }

    private Response loginAsNicoPost() {
        return loginAsPost("nico", "123456789012");
    }

    private Response loginAsLuciaPost() {
        return loginAsPost("lucia", "123456789012");
    }

    private Response loginAsPost(String userName, String password) {
        JSONObject loginRequestBody = new JSONObject();
        try {
            loginRequestBody.put(USERNAME_KEY, userName);
            loginRequestBody.put(PASSWORD_KEY, password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return given().contentType(JSON_CONTENT_TYPE)
                .body(loginRequestBody.toString())
                .post(URL + "/login");
    }

    @Test
    public void loginFail() throws JSONException {
        JSONObject loginRequestBody = new JSONObject();
        loginRequestBody.put(USERNAME_KEY, USERNAME_JOSE);
        loginRequestBody.put(PASSWORD_KEY, "44446789012");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(loginRequestBody.toString())
                .post(URL + "/login");

        response.then().body(ERROR_MESSAGE_KEY,
                is(CinemaRepository.USER_OR_PASSWORD_ERROR));
        assertFalse(response.cookies().containsKey(TOKEN_COOKIE_NAME));

    }

    @Test
    public void logoutOk() {
        var token = loginAsJoseAndGetCookie();
        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .post(URL + "/logout");
        var cookie = response.getDetailedCookie(TOKEN_COOKIE_NAME);
        assertEquals(0, cookie.getMaxAge());
        assertEquals("", cookie.getValue());
    }

    @Test
    public void registerUserOk() throws JSONException {
        JSONObject registerRequestBody = new JSONObject();
        registerRequestBody.put("name", "auser");
        registerRequestBody.put("surname", "ausersurname");
        registerRequestBody.put("email", "auser@ma.com");
        registerRequestBody.put(USERNAME_KEY, "auniqueusername");
        registerRequestBody.put(PASSWORD_KEY, "444467890124");
        registerRequestBody.put("repeatPassword", "444467890124");
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(registerRequestBody.toString())
                .post(URL + "/users/register");
        response.then().statusCode(200);
        loginAsPost("auniqueusername", "444467890124").then()
                .cookie(TOKEN_COOKIE_NAME, containsString("v2.local"));
    }

    @Test
    public void playingNowShowsOk() {
        var response = get(URL + "/shows");
        response.then().body(SHOW_MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME, SMALL_FISH_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME));
        response.then().body(MOVIE_DURATION_KEY,
                hasItems("1hr 49mins", "2hrs 05mins", "1hr 45mins",
                        "1hr 45mins"));
    }

    @Test
    public void moviesOk() {
        var response = get(URL + "/movies");
        response.then().body(MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSortedRateOk() {
        var response = get(URL + "/movies/sorted/rate");
        response.then().body("[0].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        response.then().body("[1].name", is(SMALL_FISH_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void retrieveUserProfileFailIfNotAuthenticated() {
        var response = get(URL + "/users/profile");
        response.then().body(ERROR_MESSAGE_KEY,
                is(CinemaSystemController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void changePasswordFailPasswordsDoesNotMatch() throws JSONException {
        var token = loginAsLuciaAndGetCookie();
        JSONObject changePassRequestBody = changePasswordBody();
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD2,
                "anotherpassword");
        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");
        assertEquals(500, response.statusCode());
        response.then().body(ERROR_MESSAGE_KEY,
                is("Passwords must be equals"));
    }

    @Test
    public void changePasswordFailWhenNotAuthenticated() throws JSONException {
        JSONObject changePassRequestBody = changePasswordBody();
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");
        response.then().body(ERROR_MESSAGE_KEY,
                is(CinemaSystemController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void changePasswordOk() throws JSONException {
        var token = loginAsLuciaAndGetCookie();
        JSONObject changePassRequestBody = changePasswordBody();
        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");
        assertEquals(200, response.statusCode());
    }

    private JSONObject changePasswordBody()
            throws JSONException {
        JSONObject changePassRequestBody = new JSONObject();
        changePassRequestBody.put(CHANGE_PASS_BODY_CURRENT_PASS,
                "123456789012");
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD1,
                "9898989898989898");
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD2,
                "9898989898989898");
        return changePassRequestBody;
    }

    @Test
    public void retrieveUserProfileOk() {
        var token = loginAsJoseAndGetCookie();
        var response = given()
                .cookie(TOKEN_COOKIE_NAME, token)
                .get(URL + "/users/profile");
        response.then().body(USERNAME_KEY, is(USERNAME_JOSE))
                .body(FULLNAME_KEY, is(JOSE_FULLNAME))
                .body(POINTS_KEY, is(0))
                .body(EMAIL_KEY, is(JOSE_EMAIL));
    }

    @Test
    public void moviesSortedReleaseDateOk() {
        var response = get(URL + "/movies/sorted/releasedate");
        response.then().body("[0].name", is(RUNNING_FAR_AWAY_MOVIE_NAME));
        response.then().body("[1].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSearchOk() {
        var response = get(URL + "/movies/search/rock");
        response.then().body("[0].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }

    private void assertOnMovies(Response response) {
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_GENRES_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_PLOT_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_DURATION_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_DIRECTORS_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RELEASE_DATE_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RATING_VALUE_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RATING_TOTAL_VOTES_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_ACTORS_KEY)));
    }

    @Test
    public void movieOneOk() {
        var response = get(URL + "/movies/1");
        response.then().body(MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body(JSON_ROOT, hasKey(MOVIE_GENRES_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_PLOT_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_DURATION_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_DIRECTORS_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RELEASE_DATE_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RATING_VALUE_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RATING_TOTAL_VOTES_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_ACTORS_KEY));
    }

    @Test
    public void ratesFromMovieOneOk() {
        var response = get(URL + "/movies/1/rate");
        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USERNAME_KEY, "lucia")).and(
                                (hasEntry(COMMENT_KEY,
                                        "I really enjoy the movie")))
                        .and(hasEntry(RATE_VALUE_KEY, 4)))));
        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USERNAME_KEY, "nico")).and(
                                (hasEntry(COMMENT_KEY,
                                        "Fantastic! The actors, the music, everything is fantastic!")))
                        .and(hasEntry(RATE_VALUE_KEY, 5)))));
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
    public void reserveAShowFailIfNotAuthenticated() {
        JSONArray seatsRequest = jsonBodyForReserveSeats(5, 7, 9);
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(seatsRequest.toString())
                .post(URL + "/shows/1/reserve");
        response.then().body(ERROR_MESSAGE_KEY,
                is(CinemaSystemController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void reserveAlreadyReservedShowFail() {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(7);
        reservePost(token, seatsRequest);
        var tokenJose = loginAsJoseAndGetCookie();
        JSONArray seatsRequest2 = jsonBodyForReserveSeats(5, 7, 9);
        var failedResponse = reservePost(tokenJose, seatsRequest2);
        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("All or some of the seats chosen are busy"));
    }

    @Test
    public void payAShowFailIfNotAuthenticated() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 3, 7);
        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(paymentRequest.toString())
                .post(URL + "/shows/1/pay");
        response.then().body(ERROR_MESSAGE_KEY,
                is(CinemaSystemController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void payNotReservedSeatsFail() throws JSONException {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 3, 7);
        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);
        var failedResponse = payPost(token, paymentRequest);
        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("Reservation is required before confirm"));
    }

    @Test
    public void payReservedShowOk() throws JSONException {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(12, 13, 17);
        reservePost(token, seatsRequest);
        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);
        var response = payPost(token, paymentRequest);
        response.then().body(SHOW_MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body("total", is(30.0F));
        response.then().body("pointsWon", is(10));
        response.then().body(USERNAME_KEY, is("nico"));
        response.then().body("payedSeats", hasItems(12, 13, 17));
    }

    private JSONObject paymentRequestForSeats(JSONArray seatsRequest)
            throws JSONException {
        JSONObject paymentRequest = new JSONObject();
        paymentRequest.put("selectedSeats", seatsRequest);
        paymentRequest.put("creditCardNumber", "56565-98758-2323");
        paymentRequest.put("secturityCode", "326");
        paymentRequest.put("expirationYear",
                LocalDate.now().plusYears(1).getYear());
        paymentRequest.put("expirationMonth", 11);
        return paymentRequest;
    }

    @Test
    public void reserveAShowOk() {
        var response = reserveSeatsTwoFourFromShowOnePost();
        List<Map<String, Object>> list = response.then().extract().jsonPath()
                .getList(CURRENT_SEATS_KEY);
        var notAvailableSeats = list.stream()
                .filter(l -> l.get(SEAT_AVAILABLE_KEY).equals(false))
                .toList();
        var availableSeats = list.stream()
                .filter(l -> l.get(SEAT_AVAILABLE_KEY).equals(true))
                .toList();
        assertEquals(3, notAvailableSeats.size());
        assertEquals(27, availableSeats.size());
        response.then().body(INFO_KEY + "." + SHOW_MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body("info.showId", is(1));
        response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
        response.then().body(INFO_KEY, hasKey("movieDuration"));
    }

    private Response reserveSeatsTwoFourFromShowOnePost() {
        var token = loginAsJoseAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 4);
        return reservePost(token, seatsRequest);
    }

    private Response payPost(String token, JSONObject paymentRequest) {
        return given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(paymentRequest.toString())
                .post(URL + "/shows/1/pay");
    }

    private Response reservePost(String token, JSONArray seatsRequest) {
        return given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(seatsRequest.toString())
                .post(URL + "/shows/1/reserve");
    }

    private JSONArray jsonBodyForReserveSeats(Integer... seats) {
        JSONArray seatsRequest = new JSONArray();
        for (Integer seat : seats) {
            seatsRequest.put(seat);
        }
        return seatsRequest;
    }

    private String loginAsLuciaAndGetCookie() {
        var loginResponse = loginAsLuciaPost();
        return getCookie(loginResponse);
    }

    private String loginAsNicoAndGetCookie() {
        var loginResponse = loginAsNicoPost();
        return getCookie(loginResponse);
    }

    private String getCookie(Response loginResponse) {
        return loginResponse.getCookie(TOKEN_COOKIE_NAME);
    }

    private String loginAsJoseAndGetCookie() {
        var loginResponse = loginAsJosePost();
        return getCookie(loginResponse);
    }
}
