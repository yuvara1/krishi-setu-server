package org.agri.agritrade.service;

public interface EmailServicePort {
    void sendEmail(String to, String subject, String body);
    void sendOrderConfirmation(String to, String message);
    void sendBidNotification(String to, String message);
    void sendOtpEmail(String to, String otp);
}
