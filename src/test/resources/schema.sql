CREATE TABLE accounts (
    id UUID PRIMARY KEY,
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
    request_id UUID PRIMARY KEY,
    transaction_id UUID,
    REQUEST_STATUS VARCHAR(255),
    JSON_BODY TEXT,
    ERROR_MESSAGE TEXT
);

ALTER TABLE transaction_requests
ADD CONSTRAINT FK_transaction
FOREIGN KEY (transaction_id)
REFERENCES transactions(id);