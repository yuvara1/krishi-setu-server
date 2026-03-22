package org.agri.agritrade.service;

public interface SmsServicePort {
    void sendSms(String phoneNumber, String message);
}
