-- Insert new account 1
INSERT INTO accounts (id, balance, currency, version, CREATED_AT)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 10.00, 'EUR', 0, CURRENT_TIMESTAMP);

-- Insert new account 2
INSERT INTO accounts (id, balance, currency, version, CREATED_AT)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 10.00, 'EUR', 0, CURRENT_TIMESTAMP);

-- Insert new account 3
INSERT INTO accounts (id, balance, currency, version, CREATED_AT)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe1', 10.00, 'CAD', 0, CURRENT_TIMESTAMP);

-- Insert default account
INSERT INTO accounts (id, balance, currency, version, CREATED_AT)
VALUES ('00000000-0000-0000-0000-000000000000', 0.0, 'NOT_DEFINED', 1, CURRENT_TIMESTAMP);