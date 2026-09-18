package com.shopsphere.userservice.service;

import com.shopsphere.userservice.dto.UserCreateRequest;
import com.shopsphere.userservice.dto.UserResponse;
import com.shopsphere.userservice.entity.User;
import com.shopsphere.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldCreateAndReturnUser() {

        UserCreateRequest request = new UserCreateRequest();

        request.setFirstName("Rakesh");
        request.setLastName("Perala");
        request.setEmail("rakesh@example.com");
        request.setPassword("Password123");

        User savedUser = new User(
                "Rakesh",
                "Perala",
                "rakesh@example.com",
                "Password123"
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Rakesh", response.getFirstName());
        assertEquals("Perala", response.getLastName());
        assertEquals("rakesh@example.com", response.getEmail());

        verify(userRepository, times(1))
                .save(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnUsers() {

        User user1 = new User(
                "Rakesh",
                "Perala",
                "rakesh@example.com",
                "Password123"
        );

        User user2 = new User(
                "John",
                "Doe",
                "john@example.com",
                "Password123"
        );

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(2, responses.size());
        assertEquals("Rakesh", responses.get(0).getFirstName());
        assertEquals("John", responses.get(1).getFirstName());

        verify(userRepository, times(1))
                .findAll();
    }

    @Test
    void getUserById_shouldReturnUserWhenUserExists() {

        User user = new User(
                "Rakesh",
                "Perala",
                "rakesh@example.com",
                "Password123"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<UserResponse> response =
                userService.getUserById(1L);

        assertTrue(response.isPresent());
        assertEquals("Rakesh", response.get().getFirstName());
        assertEquals(
                "rakesh@example.com",
                response.get().getEmail()
        );

        verify(userRepository, times(1))
                .findById(1L);
    }

    @Test
    void getUserById_shouldReturnEmptyWhenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<UserResponse> response =
                userService.getUserById(99L);

        assertTrue(response.isEmpty());

        verify(userRepository, times(1))
                .findById(99L);
    }

    @Test
    void getUserByEmail_shouldReturnUserWhenUserExists() {

        User user = new User(
                "Rakesh",
                "Perala",
                "rakesh@example.com",
                "Password123"
        );

        when(userRepository.findByEmail("rakesh@example.com"))
                .thenReturn(Optional.of(user));

        Optional<UserResponse> response =
                userService.getUserByEmail("rakesh@example.com");

        assertTrue(response.isPresent());
        assertEquals(
                "Rakesh",
                response.get().getFirstName()
        );
        assertEquals(
                "rakesh@example.com",
                response.get().getEmail()
        );

        verify(userRepository, times(1))
                .findByEmail("rakesh@example.com");
    }

    @Test
    void getUserByEmail_shouldReturnEmptyWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        Optional<UserResponse> response =
                userService.getUserByEmail("unknown@example.com");

        assertTrue(response.isEmpty());

        verify(userRepository, times(1))
                .findByEmail("unknown@example.com");
    }

    @Test
    void deleteUser_shouldDeleteUser() {

        userService.deleteUser(1L);

        verify(userRepository, times(1))
                .deleteById(1L);
    }
}
