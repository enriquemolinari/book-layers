package model.api;

public interface EmailProvider {

	void send(String to, String subject, String body);

}
