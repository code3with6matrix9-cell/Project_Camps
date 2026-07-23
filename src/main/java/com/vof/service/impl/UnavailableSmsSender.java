package com.vof.service.impl;

import com.vof.service.SmsSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Prevents a password reset from pretending that an SMS was delivered. */
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "none", matchIfMissing = true)
public class UnavailableSmsSender implements SmsSender {
    @Override
    public void sendPasswordOtp(String mobileNumber, String otp) {
        throw new IllegalStateException("Password OTP delivery is unavailable. Configure app.sms.provider=twilio before enabling password reset.");
    }
}
