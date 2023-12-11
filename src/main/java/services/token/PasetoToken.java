package services.token;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;
import services.api.AuthException;
import services.api.DateTimeProvider;
import services.api.Token;

public class PasetoToken implements Token {

	static final String INVALID_TOKEN = "Invalid token. You have to login.";
	private byte[] base64Secret;
	private static final long defaultMilliSecondsSinceNow = 60 * 60 * 1000; // 1
																			// hs
	private DateTimeProvider dateProvider;
	private Long milliSecondsSinceNow;

	public PasetoToken(String base64Secret) {
		this(DateTimeProvider.create(), base64Secret,
				defaultMilliSecondsSinceNow);
	}

	public PasetoToken(DateTimeProvider dateProvider, String base64Secret,
			long milliSecondsSinceNow) {
		this.dateProvider = dateProvider;
		this.base64Secret = Base64.getDecoder().decode(base64Secret);
		this.milliSecondsSinceNow = milliSecondsSinceNow;
	}

	private Long expiration() {
		return (dateProvider.now().atZone(ZoneId.systemDefault()).toInstant()
				.toEpochMilli() + this.milliSecondsSinceNow) / 1000;
	}

	@Override
	public String tokenFrom(Map<String, Object> payload) {
		var pb = Pasetos.V2.LOCAL.builder();

		payload.forEach((key, value) -> {
			pb.claim(key, value);
		});

		pb.setExpiration(Instant.ofEpochSecond(this.expiration()));

		return pb.setSharedSecret(Keys.secretKey(this.base64Secret)).compact();
	}

	@Override
	public Long verifyAndGetUserIdFrom(String token) {
		Paseto tk;
		try {
			tk = Pasetos.parserBuilder()
					.setSharedSecret(Keys.secretKey(this.base64Secret)).build()
					.parse(token);
			return tk.getClaims().get("id", Long.class);
		} catch (Exception ex) {
			throw new AuthException(INVALID_TOKEN);
		}
	}
}
