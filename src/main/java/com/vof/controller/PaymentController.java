package com.vof.controller;
import com.vof.dto.request.PaymentProofRequest;
import com.vof.dto.response.CommonApiResponse;
import com.vof.dto.response.PaymentResponse;
import com.vof.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
@RestController @RequestMapping("/api/payment-proof") @RequiredArgsConstructor @SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<CommonApiResponse> uploadPaymentProof(@Valid @ModelAttribute PaymentProofRequest request) throws IOException {
        PaymentResponse paymentResponse = paymentService.uploadPaymentProof(request);
        return new ResponseEntity<>(CommonApiResponse.builder().success(true).message("Payment proof uploaded successfully. Status: " + paymentResponse.getStatus()).data(paymentResponse).build(), HttpStatus.OK);
    }
}
