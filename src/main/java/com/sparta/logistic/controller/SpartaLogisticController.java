package com.sparta.logistic.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// added imports for email
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.sparta.logistic.dto.EnquiryRequest;

@RestController
@RequestMapping("/api/logistic")
public class SpartaLogisticController {

    private static final Logger logger = LoggerFactory.getLogger(SpartaLogisticController.class);

    // Inject JavaMailSender (requires spring-boot-starter-mail on classpath and mail config in application.properties)
    @Autowired
    private JavaMailSender mailSender;

    // recipient can be overridden in application.properties: enquiry.notification.recipient
    @Value("${enquiry.notification.recipient:contact@sparta.com}")
    private String notificationRecipient;

    // added: control the Mail "From" header (falls back to spring.mail.username, then no-reply)
    @Value("${spring.mail.from:${spring.mail.username:no-reply@sparta.com}}")
    private String mailFrom;

    @GetMapping("/info")
    public ResponseEntity<String> getLogisticInfo() {
        return ResponseEntity.ok("Sparta Logistic Information");
    }

    @PostMapping("/enquiry")
    public ResponseEntity<Object> submitEnquiry(@RequestBody EnquiryRequest enquiry) {
        // Basic validation
        if (enquiry == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is required");
        }
        if (isEmpty(enquiry.getFirstName()) || isEmpty(enquiry.getEmail())
                || isEmpty(enquiry.getPhoneNumber()) || isEmpty(enquiry.getCity())
                || isEmpty(enquiry.getInquiry()) || isEmpty(enquiry.getMessage())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields are required");
        }
        if (!isValidEmail(enquiry.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email");
        }
        if (!isValidPhone(enquiry.getPhoneNumber())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone number");
        }

        // Log the enquiry (could be persisted or emailed in future)
        logger.info("Received enquiry: {}", enquiry);

        // Send notification email
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(notificationRecipient);
            msg.setFrom(mailFrom); // set the from address so SMTP providers accept the message
            msg.setSubject("New Enquiry from " + enquiry.getFirstName() + " [" + enquiry.getInquiry() + "]");
            StringBuilder body = new StringBuilder();
            body.append("You have received a new enquiry:\n\n");
            body.append("Name: ").append(enquiry.getFirstName()).append("\n");
            body.append("Email: ").append(enquiry.getEmail()).append("\n");
            body.append("Phone: ").append(enquiry.getPhoneNumber()).append("\n");
            body.append("City: ").append(enquiry.getCity()).append("\n");
            body.append("Type: ").append(enquiry.getInquiry()).append("\n\n");
            body.append("Message:\n").append(enquiry.getMessage()).append("\n");
            msg.setText(body.toString());
            mailSender.send(msg);
            logger.info("Enquiry notification email sent to {}", notificationRecipient);
        } catch (MailException ex) {
            logger.error("Failed to send enquiry notification email", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("status", "error", "message", "Failed to send notification"));
        }

        // Return created response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of("status", "received", "firstName", enquiry.getFirstName()));
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        // simple check; replace with robust validation if needed
        return email != null && email.contains("@") && email.indexOf('@') != 0 && email.indexOf('@') != email.length() - 1;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }
}
