/* This is the data model for each player. It creates a user table in PostgreSQL.
This is where user data is stored such as: id, username, password, coin balance, when the coins were last refilled,
account creation date, and blackjack tutorial.
*/
package com.gambingapp.gaminghub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    // Named 'coins' to match the database column exactly
    @Column(name = "coins", nullable = false)
    private int coins;

    @Column(name = "last_refill_at", nullable = false)
    private LocalDateTime lastRefillAt;

    @Column(name = "has_seen_blackjack_tutorial", nullable = false)
    private boolean hasSeenBlackjackTutorial;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String email;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.coins = 1000;
        this.lastRefillAt = LocalDateTime.now();
        this.hasSeenBlackjackTutorial = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public LocalDateTime getLastRefillAt() { return lastRefillAt; }
    public void setLastRefillAt(LocalDateTime lastRefillAt) { this.lastRefillAt = lastRefillAt; }

    public boolean isHasSeenBlackjackTutorial() { return hasSeenBlackjackTutorial; }
    public void setHasSeenBlackjackTutorial(boolean seen) { this.hasSeenBlackjackTutorial = seen; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
}