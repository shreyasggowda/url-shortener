package com.example.urlshortener.api;

import com.example.urlshortener.domain.AppUser;
import com.example.urlshortener.domain.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) { this.users = users; this.passwordEncoder = passwordEncoder; this.authenticationManager = authenticationManager; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody Credentials credentials, HttpServletRequest request) {
        String email = credentials.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "An account with that email already exists"));
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(credentials.password())));
        authenticate(user, credentials.password(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("email", user.getEmail()));
    }
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Credentials credentials, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(credentials.email().trim().toLowerCase(), credentials.password()));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
        return Map.of("email", ((AppUser) auth.getPrincipal()).getEmail());
    }
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) { org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler handler = new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler(); handler.logout(request, response, null); }
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal AppUser user) { return user == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build() : ResponseEntity.ok(Map.of("email", user.getEmail())); }

    private void authenticate(AppUser user, String password, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(user.getEmail(), password));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
    }
    public record Credentials(@NotBlank @Email String email, @NotBlank @Size(min = 8, max = 72) String password) { }
}
