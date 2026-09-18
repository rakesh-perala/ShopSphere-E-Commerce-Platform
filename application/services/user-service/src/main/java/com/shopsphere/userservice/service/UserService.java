package com.shopsphere.userservice.service;

import com.shopsphere.userservice.dto.UserCreateRequest;
import com.shopsphere.userservice.dto.UserResponse;
import com.shopsphere.userservice.entity.User;
import com.shopsphere.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserCreateRequest request) {

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword()
        );

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public Optional<UserResponse> getUserById(Long id) {

        return userRepository.findById(id)
                .map(this::toUserResponse);
    }

    public Optional<UserResponse> getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .map(this::toUserResponse);
    }

    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}
