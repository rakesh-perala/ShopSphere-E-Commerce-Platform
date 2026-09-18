package com.shopsphere.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.userservice.dto.UserCreateRequest;
import com.shopsphere.userservice.dto.UserResponse;
import com.shopsphere.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setFirstName("Rakesh");
        request.setLastName("Perala");
        request.setEmail("rakesh@example.com");
        request.setPassword("Password123");

        UserResponse response = new UserResponse(
                1L,
                "Rakesh",
                "Perala",
                "rakesh@example.com"
        );

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Rakesh"))
                .andExpect(jsonPath("$.lastName").value("Perala"))
                .andExpect(jsonPath("$.email")
                        .value("rakesh@example.com"));
    }

    @Test
    void createUser_shouldReturnBadRequestForInvalidRequest()
            throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setFirstName("");
        request.setLastName("");
        request.setEmail("invalid-email");
        request.setPassword("123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void getAllUsers_shouldReturnUsers() throws Exception {

        List<UserResponse> responses = List.of(
                new UserResponse(
                        1L,
                        "Rakesh",
                        "Perala",
                        "rakesh@example.com"
                ),
                new UserResponse(
                        2L,
                        "John",
                        "Doe",
                        "john@example.com"
                )
        );

        when(userService.getAllUsers())
                .thenReturn(responses);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName")
                        .value("Rakesh"))
                .andExpect(jsonPath("$[1].firstName")
                        .value("John"));
    }

    @Test
    void getUserById_shouldReturnUserWhenExists()
            throws Exception {

        UserResponse response = new UserResponse(
                1L,
                "Rakesh",
                "Perala",
                "rakesh@example.com"
        );

        when(userService.getUserById(1L))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName")
                        .value("Rakesh"))
                .andExpect(jsonPath("$.email")
                        .value("rakesh@example.com"));
    }

    @Test
    void getUserById_shouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        when(userService.getUserById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmail_shouldReturnUserWhenExists()
            throws Exception {

        UserResponse response = new UserResponse(
                1L,
                "Rakesh",
                "Perala",
                "rakesh@example.com"
        );

        when(userService.getUserByEmail("rakesh@example.com"))
                .thenReturn(Optional.of(response));

        mockMvc.perform(
                        get("/api/users/email/rakesh@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("rakesh@example.com"));
    }

    @Test
    void getUserByEmail_shouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        when(userService.getUserByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/users/email/unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnNoContent()
            throws Exception {

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
