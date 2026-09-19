
package com.shopsphere.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.paymentservice.dto.PaymentRequest;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(100L);
        request.setUserId(200L);
        request.setAmount(new BigDecimal("499.99"));
        request.setPaymentMethod("CARD");
        request.setTransactionId("TXN-1001");

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentService.createPayment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        )).thenReturn(payment);

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value(100))
        .andExpect(jsonPath("$.userId").value(200))
        .andExpect(jsonPath("$.amount").value(499.99))
        .andExpect(jsonPath("$.paymentMethod").value("CARD"))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.transactionId").value("TXN-1001"));

        verify(paymentService, times(1)).createPayment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );
    }

    @Test
    void shouldGetAllPayments() throws Exception {

        Payment payment1 = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        Payment payment2 = new Payment(
                101L,
                201L,
                new BigDecimal("999.99"),
                "UPI",
                "TXN-1002"
        );

        when(paymentService.getAllPayments())
                .thenReturn(List.of(payment1, payment2));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[1].orderId").value(101));

        verify(paymentService, times(1))
                .getAllPayments();
    }

    @Test
    void shouldGetPaymentById() throws Exception {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentService.getPaymentById(1L))
                .thenReturn(payment);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.userId").value(200))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentService, times(1))
                .getPaymentById(1L);
    }

    @Test
    void shouldGetPaymentsByOrderId() throws Exception {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        when(paymentService.getPaymentsByOrderId(100L))
                .thenReturn(List.of(payment));

        mockMvc.perform(get("/api/payments/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].transactionId").value("TXN-1001"));

        verify(paymentService, times(1))
                .getPaymentsByOrderId(100L);
    }

    @Test
    void shouldUpdatePaymentStatus() throws Exception {

        Payment payment = new Payment(
                100L,
                200L,
                new BigDecimal("499.99"),
                "CARD",
                "TXN-1001"
        );

        payment.setStatus("SUCCESS");

        when(paymentService.updatePaymentStatus(1L, "SUCCESS"))
                .thenReturn(payment);

        mockMvc.perform(
                put("/api/payments/1/status")
                        .param("status", "SUCCESS")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(paymentService, times(1))
                .updatePaymentStatus(1L, "SUCCESS");
    }

    @Test
    void shouldDeletePayment() throws Exception {

        doNothing()
                .when(paymentService)
                .deletePayment(1L);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1))
                .deletePayment(1L);
    }

    @Test
    void shouldRejectInvalidPaymentRequest() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(null);
        request.setUserId(200L);
        request.setAmount(new BigDecimal("499.99"));
        request.setPaymentMethod("CARD");
        request.setTransactionId("TXN-1001");

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

        verify(paymentService, never()).createPayment(
                anyLong(),
                anyLong(),
                any(BigDecimal.class),
                anyString(),
                anyString()
        );
    }
}

