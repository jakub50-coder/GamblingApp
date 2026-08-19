//this entity is used for when you forget your password (email)
package com.gambingapp.gaminghub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, unique = true, length = 36)
    private String token;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PasswordResetToken(){}

    public PasswordResetToken(User user, String token, LocalDateTime expiresAt){
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }
    public Long getId(){
        return id;
    }
    public User getUser(){
        return user;
    }
    public String getToken(){
        return token;
    }
    public LocalDateTime getExpiresAt(){
        return expiresAt;
    }
    public boolean isUsed(){
        return used;
    }
    public void setUsed(boolean used){
        this.used = used;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    //return true if token is still valid
    public boolean isValid(){
        return !used  && LocalDateTime.now().isBefore(expiresAt);
    }
}