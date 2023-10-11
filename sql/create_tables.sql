CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    balance DECIMAL(19, 4),
    currency VARCHAR(255),
    createdAt TIMESTAMP,
    version INT
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    currency VARCHAR(255)
);

ALTER TABLE transactions
ADD CONSTRAINT FK_source_account
FOREIGN KEY (source_account_id)
REFERENCES accounts(id);

ALTER TABLE transactions
ADD CONSTRAINT FK_target_account
FOREIGN KEY (target_account_id)
REFERENCES accounts(id);


CREATE TABLE transaction_requests (
    requestId UUID PRIMARY KEY,
    transaction_id UUID,
    requestStatus VARCHAR(255),
    jsonBody TEXT,
    errorMessage TEXT
);

ALTER TABLE transaction_requests
ADD CONSTRAINT FK_transaction
FOREIGN KEY (transaction_id)
REFERENCES transactions(id);

-- Insert new account 1
INSERT INTO accounts (id, balance, currency, createdAt, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 1000.00, 'EUR', NOW(), 1);

-- Insert new account 2
INSERT INTO accounts (id, balance, currency, createdAt, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 750.50, 'EUR', NOW(), 1);