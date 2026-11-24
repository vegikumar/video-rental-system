package com.crio.rentvideo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.rentvideo.dto.LoginRequest;
import com.crio.rentvideo.dto.RegisterRequest;
import com.crio.rentvideo.dto.UserResponse;
import com.crio.rentvideo.entity.Role;
import com.crio.rentvideo.entity.User;
import com.crio.rentvideo.exception.BadRequestException;
import com.crio.rentvideo.exception.UnauthorizedException;
import com.crio.rentvideo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    void register_defaultsRoleToCustomer() {
        RegisterRequest request = RegisterRequest.builder()
                .email("customer@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(response.getEmail()).isEqualTo("customer@example.com");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("taken@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_returnsUserOnValidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(2L)
                .email("admin@example.com")
                .password("hashed")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.login(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void login_throwsOnInvalidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password("wrong")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
