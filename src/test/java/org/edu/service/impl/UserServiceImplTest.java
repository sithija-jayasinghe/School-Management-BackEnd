package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.edu.dto.request.UserUpdateRequest;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.response.UserResponse;
import org.edu.util.Role;
import org.edu.entity.User;
import org.edu.exception.DuplicateEmailException;
import org.edu.repository.UserRepository;
import org.edu.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password123");
        request.setRole(Role.TEACHER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRole(Role.TEACHER);
        savedUser.setActive(true);
        savedUser.setCreatedAt(Instant.now());
        savedUser.setUpdatedAt(Instant.now());

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.TEACHER, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("John Doe");
        request.setEmail(" John@Example.com ");
        request.setPassword("Password123");
        request.setRole(Role.TEACHER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.register(request));
    }

    @Test
    void shouldReturnCurrentAuthenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setRole(Role.ADMIN);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        UserResponse response = userService.getCurrentUser();

        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void shouldListUsers() {
        User activeUser = new User();
        activeUser.setId(1L);
        activeUser.setName("Kasun Senanayake");
        activeUser.setEmail("kasun@sms.lk");
        activeUser.setRole(Role.ADMIN);
        activeUser.setActive(true);

        when(userRepository.findAll(PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(activeUser), PageRequest.of(0, 10), 1));

        var response = userService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(1, response.getTotalElements());
        assertTrue(response.getContent().getFirst().isActive());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setEmail("old@example.com");
        user.setRole(Role.TEACHER);
        user.setActive(true);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("New Name");
        request.setEmail("new@example.com");
        request.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("New Name", response.getName());
        assertEquals("new@example.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void shouldDeactivateUser() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        userService.deactivateUser(1L);

        assertFalse(user.isActive());
    }
}
