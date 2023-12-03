package model.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.paseto.jpaseto.lang.Keys;
import model.api.AuthException;

public class TokenTest {

	private SecretKey key;
	private String encodedKey;

	@BeforeEach
	public void before() {
		key = Keys.secretKey();
		encodedKey = Base64.getEncoder()
				.encodeToString(key.getEncoded());
	}

	@Test
	public void validToken() {
		var token = new PasetoToken(() -> LocalDateTime.now(), encodedKey,
				60 * 1000);
		var stringToken = token.tokenFrom(Map.of("id", 1L));
		assertEquals(1L, token.verifyAndGetUserIdFrom(stringToken));
	}

	@Test
	public void expiredToken() {
		var token = new PasetoToken(() -> LocalDateTime.now().minusMinutes(5),
				encodedKey,
				60 * 1000);
		var stringToken = token.tokenFrom(Map.of("id", 1L));

		var e = assertThrows(AuthException.class, () -> {
			token.verifyAndGetUserIdFrom(stringToken);
			fail("I have obtained the user id from an expired token");
		});

		assertEquals(PasetoToken.INVALID_TOKEN, e.getMessage());
	}
}
