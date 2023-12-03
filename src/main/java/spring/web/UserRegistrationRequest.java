package spring.web;

public record UserRegistrationRequest(String name, String surname, String email,
		String userName,
		String password, String repeatPassword) {
}
