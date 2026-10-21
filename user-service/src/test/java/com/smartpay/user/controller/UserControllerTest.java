package com.smartpay.user.controller;

import com.smartpay.user.UserController;
import com.smartpay.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnUserServiceStatus() throws Exception {
        mockMvc.perform(get("/api/users/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service")
                        .value("SmartPay User Service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
