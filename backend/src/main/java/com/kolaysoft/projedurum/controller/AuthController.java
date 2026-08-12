package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.LoginRequest;
import com.kolaysoft.projedurum.dto.LoginResponse;
import com.kolaysoft.projedurum.security.JwtService;
import com.kolaysoft.projedurum.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.sifre())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        return new LoginResponse(
                token,
                principal.getId(),
                principal.getUser().getAdSoyad(),
                principal.getUsername(),
                principal.getUser().getRol().name()
        );
    }
}
