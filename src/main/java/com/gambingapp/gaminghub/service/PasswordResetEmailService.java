package com.gambingapp.gaminghub.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailService.class);

    private final boolean enabled;
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public PasswordResetEmailService(
            @org.springframework.beans.factory.annotation.Value("${app.password-reset.email.enabled:false}") boolean enabled,
            JavaMailSender mailSender,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.username:gaminghub271@gmail.com}") String fromEmail) {
        this.enabled = enabled;
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public boolean sendPasswordResetEmail(String toEmail, String resetLink) {
        if (!enabled) {
            log.warn("Password reset email delivery is disabled. Reset link for {}: {}", toEmail, resetLink);
            return false;
        }
        if (mailSender == null) {
            log.error("Password reset email delivery is unavailable because JavaMailSender is not configured.");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Gaming Hub - Password Reset Request");
            helper.setText(
                    "You requested a password reset for your Gaming Hub account.\n\n" +
                    "Use this link to reset your password: " + resetLink + "\n\n" +
                    "If you did not request this reset, you can ignore this email.",
                    true);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
            return false;
        }
    }
}
