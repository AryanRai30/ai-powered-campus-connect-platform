package com.campusconnect.service;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.exception.UserAlreadyExistsException;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.security.CustomUserDetailsService;
import com.campusconnect.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service handling authentication operations: user registration and user login.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role with default access")));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .isActive(true)
                .build();

        user.addRole(studentRole);
        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        
        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", savedUser.getId());
        extraClaims.put("roles", roleNames);

        String token = jwtService.generateToken(extraClaims, userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .roles(roleNames)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("roles", roleNames);

        String token = jwtService.generateToken(extraClaims, userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }
}
