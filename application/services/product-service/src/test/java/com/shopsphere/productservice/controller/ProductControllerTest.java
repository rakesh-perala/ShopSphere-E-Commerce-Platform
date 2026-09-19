
package com.shopsphere.productservice.controller;

import com.shopsphere.productservice.dto.ProductRequest;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productService.createProduct(any(Product.class)))
                .thenReturn(product);

        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Enterprise laptop");
        request.setPrice(new BigDecimal("75000.00"));
        request.setCategory("Electronics");
        request.setStockQuantity(10);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void shouldGetAllProducts() throws Exception {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void shouldGetProductById() throws Exception {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productService.getProductById(1L))
                .thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(75000.00));
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].category")
                        .value("Electronics"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        Product product = new Product(
                "Updated Laptop",
                "Updated description",
                new BigDecimal("80000.00"),
                "Electronics",
                20
        );

        when(productService.updateProduct(
                any(Long.class),
                any(Product.class)
        )).thenReturn(product);

        ProductRequest request = new ProductRequest();
        request.setName("Updated Laptop");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("80000.00"));
        request.setCategory("Electronics");
        request.setStockQuantity(20);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Updated Laptop"))
                .andExpect(jsonPath("$.stockQuantity").value(20));
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidProductRequest() throws Exception {

        ProductRequest request = new ProductRequest();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
