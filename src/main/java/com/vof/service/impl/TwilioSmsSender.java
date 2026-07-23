package com.vof.service.impl;

import com.vof.service.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * SMS delivery is enabled only when app.sms.provider=twilio and all Twilio values
 * are configured. This avoids silently sending a password OTP through an unknown
 * provider in production.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {
    private final RestClient.Builder restClientBuilder;

    @org.springframework.beans.factory.annotation.Value("${app.sms.twilio.account-sid:}")
    private String accountSid;
    @org.springframework.beans.factory.annotation.Value("${app.sms.twilio.auth-token:}")
    private String authToken;
    @org.springframework.beans.factory.annotation.Value("${app.sms.twilio.from-number:}")
    private String fromNumber;

    @Override
    public void sendPasswordOtp(String mobileNumber, String otp) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            throw new IllegalStateException("Twilio SMS is selected but its credentials or sender number are not configured.");
        }
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", mobileNumber);
        body.add("From", fromNumber);
        body.add("Body", "Your Valley of Flowers password OTP is " + otp + ". It expires in 10 minutes.");

        restClientBuilder.baseUrl("https://api.twilio.com")
                .build()
                .post()
                .uri("/2010-04-01/Accounts/{accountSid}/Messages.json", accountSid)
                .headers(headers -> headers.setBasicAuth(accountSid, authToken, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
