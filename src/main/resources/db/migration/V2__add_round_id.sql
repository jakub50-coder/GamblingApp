ALTER TABLE coin_transactions
ADD COLUMN round_id VARCHAR(36);
CREATE INDEX idx_coin_transactions_round_id
    ON coin_transactions(round_id);