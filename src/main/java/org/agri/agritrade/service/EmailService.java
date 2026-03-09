package org.agri.agritrade.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send email: recipient is empty");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@krishisetu.com");
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        String subject = "KrishiSetu - Password Reset OTP";
        String body = String.format(
                "Dear User,\n\nYour OTP for password reset is: %s\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nKrishiSetu Team", otp);
        sendEmail(to, subject, body);
    }

    @Async
    public void sendOrderConfirmation(String to, String orderDetails) {
        String subject = "KrishiSetu - Order Confirmation";
        sendEmail(to, subject, orderDetails);
    }

    @Async
    public void sendBidNotification(String to, String bidDetails) {
        String subject = "KrishiSetu - Bid Update";
        sendEmail(to, subject, bidDetails);
    }
}
