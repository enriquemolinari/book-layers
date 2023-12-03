package javalin.web;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import model.api.AuthException;
import model.api.CinemaSystem;
import spring.web.LoginRequest;

public class CinemaSystemController {

	private static final String TOKEN_COOKIE_NAME = "token";
	public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
	private int webPort;
	private Javalin app;
	private CinemaSystem cinema;

	public CinemaSystemController(int webPort, CinemaSystem cinema) {
		this.webPort = webPort;
		this.cinema = cinema;
	}

	public void start() {
		this.app = Javalin.create();
		app.post("/login", login());
		app.post("/logout", logout());
		app.post("/movies/{id}/rate", rateMovie());
		app.get("/shows/{id}", showDetail());
		// TODO: finish with the endpoints

		app.exception(AuthException.class, (e, ctx) -> {
			ctx.status(401);
			ctx.json(Map.of("message", e.getMessage()));
			// log error in a stream...
		});

		app.exception(Exception.class, (e, ctx) -> {
			ctx.json(
					Map.of("message",
							e.getMessage()));
			// log error in a stream...
		}).start(this.webPort);
	}

	void close() {
		this.app.close();
	}

	private Handler rateMovie() {
		return ctx -> {
			var token = ctx.cookie(TOKEN_COOKIE_NAME);
			ifAuthenticatedDo(token, userId -> {
				var r = ctx.bodyAsClass(RateRequest.class);
				var rated = this.cinema.rateMovieBy(userId,
						ctx.pathParamAsClass("id", Long.class).get(),
						r.rateValue(), r.comment());

				return ctx.json(rated);
			});
		};
	}

	private Handler login() {
		return ctx -> {
			var r = ctx.bodyAsClass(LoginRequest.class);

			var token = this.cinema.login(r.username(), r.password());
			var profile = cinema.profileFrom(cinema.userIdFrom(token));

			ctx.res().setHeader("Set-Cookie",
					TOKEN_COOKIE_NAME + "=" + token + ";path=/; HttpOnly; ");

			ctx.json(profile);
		};
	}

	private Handler logout() {
		return ctx -> {
			// want register login/logout time?
			// just remove the token cookie
			ctx.removeCookie(TOKEN_COOKIE_NAME);
			ctx.json(Map.of("result", "success"));
		};
	}

	private <S> S ifAuthenticatedDo(String token, Function<Long, S> method) {
		var userId = Optional.ofNullable(token).map(tk -> {
			var uid = this.cinema.userIdFrom(tk);
			return uid;
		}).orElseThrow(() -> new AuthException(
				AUTHENTICATION_REQUIRED));

		return method.apply(userId);
	}

	private Handler showDetail() {
		return ctx -> {
			ctx.json(
					this.cinema.show(
							ctx.pathParamAsClass("id", Long.class).get()));
		};
	}
}
