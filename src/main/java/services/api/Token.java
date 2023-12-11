package services.api;

import java.util.Map;

public interface Token {
	String tokenFrom(Map<String, Object> payload);

	Long verifyAndGetUserIdFrom(String token);
}
