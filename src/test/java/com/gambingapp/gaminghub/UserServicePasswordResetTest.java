package com.gambingapp.gaminghub;

import com.gambingapp.gaminghub.model.User;
import com.gambingapp.gaminghub.repository.CoinTransactionRepository;
import com.gambingapp.gaminghub.repository.UserRepository;
import com.gambingapp.gaminghub.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicePasswordResetTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signupShouldPersistEmailForPasswordResetLookup() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("strongPassword123")).thenReturn("encoded-password");

        boolean success = userService.signup("alice", "strongPassword123", "alice@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(true, success);
        assertEquals("alice@example.com", userCaptor.getValue().getEmail());
        verify(coinTransactionRepository).save(any());
    }
}
