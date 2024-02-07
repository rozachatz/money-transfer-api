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
    message VARCHAR(255),
    hashed_payload int
);

ALTER TABLE transactions
ADD CONSTRAINT FK_source_account
FOREIGN KEY (source_account_id)
REFERENCES accounts(id);

ALTER TABLE transactions
ADD CONSTRAINT FK_target_account
FOREIGN KEY (target_account_id)
REFERENCES accounts(id);


-- Insert new account 1
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 'Bill Gates', 1000.00, 'EUR', 1);

-- Insert new account 2
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 'Elon Musk', 750.50, 'EUR', 1);

-- Insert new account 3
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bcb', 'Onasis', 100.00, 'USD', 1);

-- Insert new account 4
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe9','Bakogiannis', 75.50, 'CAD', 1);

-- Insert default account
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES ('00000000-0000-0000-0000-000000000000', 'Unnamed' ,0.0, 'NOT_DEFINED', 1);
