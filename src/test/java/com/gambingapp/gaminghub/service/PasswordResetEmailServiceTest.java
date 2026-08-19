package com.gambingapp.gaminghub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PasswordResetEmailServiceTest {

    @Test
    void shouldReturnFalseWhenMailDeliveryIsDisabled() {
        PasswordResetEmailService service = new PasswordResetEmailService(false, null, "noreply@example.com");

        boolean sent = service.sendPasswordResetEmail("user@example.com", "http://localhost/reset?token=abc");

        assertFalse(sent);
    }
}
