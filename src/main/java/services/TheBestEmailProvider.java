package services;

import services.api.EmailProvider;

public class TheBestEmailProvider implements EmailProvider {
    @Override
    public void send(String to, String subject, String body) {
        // mails sending always succeed
    }
}
