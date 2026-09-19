package com.shopsphere.paymentservice.service;

import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        Payment result = paymentService.createPayment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        assertNotNull(result);
        assertEquals(100L, result.getOrderId());
        assertEquals(200L, result.getUserId());
        assertEquals(new BigDecimal("499.99"), result.getAmount());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals("PENDING", result.getStatus());

        verify(paymentRepository, times(1))
                .save(any(Payment.class));
    }

    @Test
    void shouldGetAllPayments() {

        Payment payment1 = new Payment(
                100L,
                200L,
                new BigDecimal("100.00"),
                "CARD",
                "TXN-1001"
        );

        Payment payment2 = new Payment(
                101L,
                201L,
                new BigDecimal("200.00"),
                "UPI",
                "TXN-1002"
        );

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment1, payment2));

        List<Payment> result = paymentService.getAllPayments();

        assertEquals(2, result.size());

        verify(paymentRepository, times(1))
                .findAll();
    }

    @Test
    void shouldGetPaymentById() {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getOrderId());

        verify(paymentRepository, times(1))
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFound() {

        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.getPaymentById(999L)
        );

        assertEquals(
                "Payment not found with id: 999",
                exception.getMessage()
        );

        verify(paymentRepository, times(1))
                .findById(999L);
    }

    @Test
    void shouldGetPaymentsByOrderId() {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentRepository.findByOrderId(100L))
                .thenReturn(List.of(payment));

        List<Payment> result =
                paymentService.getPaymentsByOrderId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());

        verify(paymentRepository, times(1))
                .findByOrderId(100L);
    }

    @Test
    void shouldUpdatePaymentStatus() {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        Payment result =
                paymentService.updatePaymentStatus(1L, "SUCCESS");

        assertEquals("SUCCESS", result.getStatus());

        verify(paymentRepository, times(1))
                .findById(1L);

        verify(paymentRepository, times(1))
                .save(payment);
    }

    @Test
    void shouldDeletePayment() {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        paymentService.deletePayment(1L);

        verify(paymentRepository, times(1))
                .findById(1L);

        verify(paymentRepository, times(1))
                .delete(payment);
    }
}
