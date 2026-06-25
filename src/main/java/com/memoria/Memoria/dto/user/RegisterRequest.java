package com.memoria.Memoria.dto.user;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String confirmedPassword
) {
}
