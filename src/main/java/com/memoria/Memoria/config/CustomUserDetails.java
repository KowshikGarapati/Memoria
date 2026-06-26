package com.memoria.Memoria.config;

import com.memoria.Memoria.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Return the roles/authorities of the user.
     * For now, Memoria has no roles, so return an empty list.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    /**
     * Return the encoded password stored in the database.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Return the username used for login.
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Account has not expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Account is not locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Credentials (password) have not expired.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account is enabled.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Returns the original User entity.
     * Useful throughout the application.
     */
    public User getUser() {
        return user;
    }

    /**
     * Convenience methods for future use.
     */
    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }
}
