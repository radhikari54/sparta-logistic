package com.sparta.logistic.controller;

import com.sparta.logistic.dto.EnquiryRequest;
import com.sparta.logistic.mail.SmtpMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/mail")
public class MailController {

    @Value("${mail.username:}")
    private String mailUsername;

    @Value("${mail.password:}")
    private String mailPassword;

    @Value("${mail.host:smtp.sendgrid.net}")
    private String mailHost;

    @Value("${mail.port:587}")
    private String mailPort;

    @Value("${mail.starttls:true}")
    private String mailStarttls;

    @Value("${mail.auth:true}")
    private String mailAuth;

    @Value("${mail.ssl.trust:}")
    private String mailSslTrust;

    @Value("${mail.debug:false}")
    private String mailDebug;

    // new: read configured from address; prefer mail.from, fall back to spring.mail.from via property in application.properties
    @Value("${mail.from:${spring.mail.from:}}")
    private String mailFrom;

    // Simple test endpoint: GET /api/logistic/mail/send?to=someone@example.com&subject=hi&body=hello
    @PostMapping("/send")
    public ResponseEntity<String> sendEnquiryMail(@RequestBody EnquiryRequest enquiry) {

        String to = "ranjana.adhikari@spartalogisticsindia.in";
        String subject = "Enquiry for quotes";
        // Build properties for SmtpMailSender / Jakarta Mail
        Properties props = new Properties();
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);
        props.put("mail.smtp.auth", mailAuth);
        props.put("mail.smtp.starttls.enable", mailStarttls);
        if (mailSslTrust != null && !mailSslTrust.isEmpty()) {
            props.put("mail.smtp.ssl.trust", mailSslTrust);
        }
        props.put("mail.debug", mailDebug);

        // Add configured from address so SmtpMailSender will use it
        if (mailFrom != null && !mailFrom.isEmpty()) {
            props.put("mail.from", mailFrom);
        }

        SmtpMailSender sender = new SmtpMailSender(mailUsername, mailPassword, props);

        try {
            // First check server availability explicitly
//            sender.testConnection();

            // If testConnection() succeeds, proceed to send
            sender.send(to, subject, prepareMessageBody(enquiry));
            return ResponseEntity.ok("Mail sent (check logs for SMTP debug).");
        } catch (AuthenticationFailedException afe) {
            // Authentication problems: return 401-like status
            String safeMsg = "SMTP authentication failed. Verify username/API key and that the credential is active.";
            return ResponseEntity.status(401).body(safeMsg + " " + afe.getMessage());
        } catch (MessagingException me) {
            // If the server is not reachable or other messaging errors occurred, return 503
            String msg = me.getMessage() != null ? me.getMessage() : "MessagingException";
            return ResponseEntity.status(503).body("Mail server unavailable or failed: " + msg);
        } catch (Exception ex) {
            // catch-all to avoid leaking internals
            return ResponseEntity.status(500).body("Unexpected error while sending mail.");
        }
    }

    private String prepareMessageBody(EnquiryRequest enquiry) {
        StringBuilder body = new StringBuilder();
        try {
            body.append("You have received a new enquiry:\n\n");
            body.append("Name: ").append(enquiry.getFirstName()).append("\n");
            body.append("Email: ").append(enquiry.getEmail()).append("\n");
            body.append("Phone: ").append(enquiry.getPhoneNumber()).append("\n");
            body.append("City: ").append(enquiry.getCity()).append("\n");
            body.append("Type: ").append(enquiry.getInquiry()).append("\n\n");
            body.append("Message:\n").append(enquiry.getMessage()).append("\n");
        } catch (MailException ex) {

        }
        return body.toString();
    }
}
