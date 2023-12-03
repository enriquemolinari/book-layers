package model.mail;

import model.api.EmailProvider;

public class TheBestEmailProvider implements EmailProvider {

	@Override
	public void send(String to, String subject, String body) {
		// mails sending always succeed
	}

}
