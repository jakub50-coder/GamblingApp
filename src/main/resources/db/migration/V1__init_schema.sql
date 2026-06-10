-- Users table
CREATE TABLE users (
    id                          BIGSERIAL PRIMARY KEY,
    username                    VARCHAR(50) NOT NULL UNIQUE,
    password                    VARCHAR(255) NOT NULL,
    coins                       INTEGER NOT NULL DEFAULT 100,
    last_refill_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    has_seen_blackjack_tutorial BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Coin transactions table
CREATE TABLE coin_transactions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    amount     INTEGER NOT NULL,
    type       VARCHAR(20) NOT NULL,
    game       VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index for fast transaction lookups per user
CREATE INDEX idx_coin_transactions_user_id
    ON coin_transactions(user_id);

-- Index for date filtering on transactions
CREATE INDEX idx_coin_transactions_created_at
    ON coin_transactions(created_at);