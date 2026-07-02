package com.esprit.connect.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String email, String firstName, String uid, String token) {
        String resetLink = frontendUrl + "/reset-password?uid=" + uid + "&token=" + token;
        String subject = "ESPRIT Connect - Password Reset";

        String htmlContent = "<!DOCTYPE html>"
                + "<html><body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;\">"
                + "<h1 style=\"color: white; margin: 0;\">ESPRIT Connect</h1>"
                + "</div>"
                + "<div style=\"background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 10px 10px;\">"
                + "<h2 style=\"color: #333;\">Password Reset Request</h2>"
                + "<p style=\"color: #555;\">Hello " + (firstName != null ? firstName : "User") + ",</p>"
                + "<p style=\"color: #555;\">We received a request to reset your password. Click the button below to set a new password:</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<a href=\"" + resetLink + "\" style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); "
                + "color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;\">"
                + "Reset Password</a>"
                + "</div>"
                + "<p style=\"color: #555;\">If you didn't request this, you can safely ignore this email. Your password will remain unchanged.</p>"
                + "<p style=\"color: #555;\">This link will expire in 24 hours.</p>"
                + "<hr style=\"border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;\">"
                + "<p style=\"color: #999; font-size: 12px; text-align: center;\">"
                + "ESPRIT Connect - Connecting Students, Alumni & Professionals</p>"
                + "</div>"
                + "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
