package com.vof.service;

/** Delivers sensitive, short-lived OTPs to a member's verified mobile number. */
public interface SmsSender {
    void sendPasswordOtp(String mobileNumber, String otp);
}
