package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.dto.PaymentRequest;
import com.shopsphere.paymentservice.dto.PaymentResponse;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.createPayment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getTransactionId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PaymentResponse(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        List<PaymentResponse> payments = paymentService.getAllPayments()
                .stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {

        Payment payment = paymentService.getPaymentById(id);

        return ResponseEntity.ok(new PaymentResponse(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(
            @PathVariable Long orderId) {

        List<PaymentResponse> payments = paymentService
                .getPaymentsByOrderId(orderId)
                .stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Payment payment = paymentService.updatePaymentStatus(id, status);

        return ResponseEntity.ok(new PaymentResponse(payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id) {

        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }
}
