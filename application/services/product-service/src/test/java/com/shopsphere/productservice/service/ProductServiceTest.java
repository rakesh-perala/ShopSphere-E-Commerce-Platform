
package com.shopsphere.productservice.service;

import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("75000.00"), result.getPrice());

        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldGetAllProducts() {

        Product product1 = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        Product product2 = new Product(
                "Mouse",
                "Wireless mouse",
                new BigDecimal("1500.00"),
                "Electronics",
                25
        );

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldGetProductById() {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals(
                "Product not found with id: 999",
                exception.getMessage()
        );

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void shouldGetProductsByCategory() {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productRepository.findByCategoryIgnoreCase("electronics"))
                .thenReturn(List.of(product));

        List<Product> result =
                productService.getProductsByCategory("electronics");

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());

        verify(productRepository, times(1))
                .findByCategoryIgnoreCase("electronics");
    }

    @Test
    void shouldUpdateProduct() {

        Product existingProduct = new Product(
                "Old Laptop",
                "Old description",
                new BigDecimal("60000.00"),
                "Electronics",
                5
        );

        Product updatedProduct = new Product(
                "New Laptop",
                "Updated description",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        Product result =
                productService.updateProduct(1L, updatedProduct);

        assertEquals("New Laptop", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(
                new BigDecimal("75000.00"),
                result.getPrice()
        );
        assertEquals(10, result.getStockQuantity());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void shouldDeleteProduct() {

        Product product = new Product(
                "Laptop",
                "Enterprise laptop",
                new BigDecimal("75000.00"),
                "Electronics",
                10
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }
}
