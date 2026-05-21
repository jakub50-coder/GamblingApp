/*Data model for a single coin event. It creates a table and stores the transaction of coins where it was a win or lose
*/
package com.gambingapp.gaminghub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "coin_transactions")
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 50)
    private String game;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CoinTransaction() {}

    public CoinTransaction(User user, int amount, String type, String game) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.game = game;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}