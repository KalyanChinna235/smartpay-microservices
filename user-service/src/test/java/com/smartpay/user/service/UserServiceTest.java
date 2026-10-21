
package com.smartpay.user.service;

import com.smartpay.user.dto.RegisterRequest;
import com.smartpay.user.dto.UserResponse;
import com.smartpay.user.entity.User;
import com.smartpay.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "Kalyan Test",
                "Kalyan.Test@example.com",
                "SmartPay@123"
        );

        when(userRepository.existsByEmail("kalyan.test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("SmartPay@123"))
                .thenReturn("bcrypt-hash");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Simulate JPA's @PrePersist callback for this unit test.
        // The real persistence callback runs when Hibernate persists the entity.
        UserResponse response = userService.register(request);

        assertEquals("Kalyan Test", response.fullName());
        assertEquals("kalyan.test@example.com", response.email());
        assertNull(response.id());
        assertNull(response.createdAt());

        verify(passwordEncoder).encode("SmartPay@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Kalyan Test",
                "kalyan.test@example.com",
                "SmartPay@123"
        );

        when(userRepository.existsByEmail("kalyan.test@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }
}
