package com.smartpay.user;

import com.smartpay.user.dto.RegisterRequest;
import com.smartpay.user.dto.UserResponse;
import com.smartpay.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of(
                "service", "SmartPay User Service",
                "status", "UP",
                "message", "User service is running successfully"
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }
}
