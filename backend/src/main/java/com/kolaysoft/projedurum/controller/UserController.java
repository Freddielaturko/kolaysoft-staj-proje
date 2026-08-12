package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.CreateUserRequest;
import com.kolaysoft.projedurum.dto.UserResponse;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Bu controller'a erisim SecurityConfig'te /api/admin/** -> hasRole("ADMIN") ile sinirlandi
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu email ile kayitli bir kullanici zaten var");
        }

        User user = new User();
        user.setAdSoyad(request.adSoyad());
        user.setEmail(request.email());
        user.setSifreHash(passwordEncoder.encode(request.sifre()));
        user.setRol(request.rol());

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }
}
