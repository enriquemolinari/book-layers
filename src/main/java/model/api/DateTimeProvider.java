package model.api;

import java.time.LocalDateTime;

@FunctionalInterface
public interface DateTimeProvider {

	LocalDateTime now();

	static DateTimeProvider create() {
		return () -> LocalDateTime.now();
	}
}
