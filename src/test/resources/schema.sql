CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    owner_name VARCHAR(20),
    balance DECIMAL(19, 4),
    currency VARCHAR(255),
    CREATED_AT TIMESTAMP,
    version INT
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    currency VARCHAR(255),
    STATUS SMALLINT,
    MESSAGE VARCHAR(255),
    HASHED_PAYLOAD INT
);

ALTER TABLE transactions
ADD CONSTRAINT FK_source_account
FOREIGN KEY (source_account_id)
REFERENCES accounts(id);

ALTER TABLE transactions
ADD CONSTRAINT FK_target_account
FOREIGN KEY (target_account_id)
REFERENCES accounts(id);

