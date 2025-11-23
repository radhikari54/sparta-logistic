package com.sparta.logistic.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

// Simple SMTP sender using Jakarta Mail. Load credentials from application properties or env.
public class SmtpMailSender {
    private final String username;
    private final String password;
    private final Properties props;

    public SmtpMailSender(String username, String password, Properties props) {
        this.username = username;
        this.password = password;
        this.props = props;
    }

    public void send(String to, String subject, String body) throws MessagingException {
        // basic validation to fail fast with clear hint
        if (username == null || username.isEmpty()) {
            throw new MessagingException("SMTP username is empty. Ensure configuration or environment variables provide the username.");
        }
        if (password == null || password.isEmpty()) {
            throw new MessagingException("SMTP password is empty. Ensure the provider API key / app password is set in environment (do NOT hardcode secrets).");
        }

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getInstance(props, auth);
        boolean debug = Boolean.parseBoolean(props.getProperty("mail.debug", "false"));
        session.setDebug(debug);

        Message msg = new MimeMessage(session);

        // Use configured "mail.from" if present, otherwise fall back to username
        String fromAddr = props.getProperty("mail.from", username);
        msg.setFrom(new InternetAddress(fromAddr));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject);
        msg.setText(body);

        // Explicit connect to see authentication step and server response in debug
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            String host = props.getProperty("mail.smtp.host", props.getProperty("mail.host"));
            int port = Integer.parseInt(props.getProperty("mail.smtp.port", props.getProperty("mail.port", "587")));
            // Respect starttls.required if set
            if ("true".equalsIgnoreCase(props.getProperty("mail.smtp.starttls.required"))) {
                props.put("mail.smtp.starttls.required", "true");
            }
            transport.connect(host, port, username, password);
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (AuthenticationFailedException afe) {
            // rethrow with clearer guidance
            throw new AuthenticationFailedException(
                "AuthenticationFailedException: 535 Authentication failed. Check username (for SendGrid use 'apikey') and ensure the API key / app password is valid and provided via environment. Original: " + afe.getMessage()
            );
        } finally {
            if (transport != null) {
                try { transport.close(); } catch (MessagingException ignored) {}
            }
        }
    }

    // New: attempt to connect to SMTP server and close connection immediately.
    // Call this from controller to check availability without relying on Spring's startup test-connection.
    public void testConnection() throws MessagingException {
        if (username == null || username.isEmpty()) {
            throw new MessagingException("SMTP username is empty. Ensure configuration or environment variables provide the username.");
        }
        if (password == null || password.isEmpty()) {
            throw new MessagingException("SMTP password is empty. Ensure the provider API key / app password is set in environment (do NOT hardcode secrets).");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        boolean debug = Boolean.parseBoolean(props.getProperty("mail.debug", "false"));
        session.setDebug(debug);

        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            String host = props.getProperty("mail.smtp.host", props.getProperty("mail.host"));
            int port = Integer.parseInt(props.getProperty("mail.smtp.port", props.getProperty("mail.port", "587")));
            transport.connect(host, port, username, password);
        } catch (AuthenticationFailedException afe) {
            throw new AuthenticationFailedException("Authentication failed when testing SMTP connection: " + afe.getMessage());
        } catch (MessagingException me) {
            throw new MessagingException("Failed to connect to SMTP server: " + me.getMessage(), me);
        } finally {
            if (transport != null) {
                try { transport.close(); } catch (MessagingException ignored) {}
            }
        }
    }

    // Example helper to build typical properties for STARTTLS
    public static Properties defaultProperties(String host, String port, boolean starttls, boolean auth, String sslTrust) {
        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", port);
        p.put("mail.smtp.auth", String.valueOf(auth));
        p.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        if (sslTrust != null && !sslTrust.isEmpty()) p.put("mail.smtp.ssl.trust", sslTrust);
        p.put("mail.debug", "true"); // toggle during troubleshooting
        return p;
    }
}
