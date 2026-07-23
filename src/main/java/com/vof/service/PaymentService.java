package com.vof.service;
import com.vof.dto.request.PaymentProofRequest;
import com.vof.dto.response.PaymentResponse;
import java.io.IOException;
public interface PaymentService { PaymentResponse uploadPaymentProof(PaymentProofRequest request) throws IOException; }
