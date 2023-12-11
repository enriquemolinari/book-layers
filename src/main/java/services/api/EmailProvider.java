package services.api;

public interface EmailProvider {

	void send(String to, String subject, String body);

}
