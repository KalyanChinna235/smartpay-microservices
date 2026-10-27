package com.smartpay.user.controller;


import com.smartpay.user.config.PasswordConfig;
import com.smartpay.user.entity.User;
import com.smartpay.user.repository.UserRepository;
import com.smartpay.user.security.JwtAuthenticationFilter;
import com.smartpay.user.security.JwtService;
import com.smartpay.user.security.SecurityConfig;
import com.smartpay.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, PasswordConfig.class,  JwtAuthenticationFilter.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldReturnUserServiceStatus() throws Exception {
        mockMvc.perform(get("/api/users/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service")
                        .value("SmartPay User Service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        User user = new User();
        user.setFullName("Kalyan Test");
        user.setEmail("kalyan.test@example.com");
        user.setPassword("bcrypt-hash");

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("kalyan.test@example.com");

        when(userRepository.findByEmail("kalyan.test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(
                org.mockito.ArgumentMatchers.eq("valid-token"),
                any(org.springframework.security.core.userdetails.UserDetails.class)
        )).thenReturn(true);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("kalyan.test@example.com"));
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

}
