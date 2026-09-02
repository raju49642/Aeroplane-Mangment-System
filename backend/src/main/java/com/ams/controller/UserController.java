package com.ams.controller;

import com.ams.dto.RegisterUserRequest;
import com.ams.entity.User;
import com.ams.service.UserService;
import com.ams.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /** The authenticated customer's own profile, used to prefill a passenger form. */
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new com.ams.exception.ResourceNotFoundException("Authenticated user not found")));
    }
}
