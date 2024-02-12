-- Insert new account 1
INSERT INTO accounts (ID, OWNER_NAME, BALANCE, CURRENCY, VERSION)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 'Bill Gates', 1000.00, 'EUR', 1);

-- Insert new account 2
INSERT INTO accounts (ID, OWNER_NAME, BALANCE, CURRENCY, VERSION)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 'Elon Musk', 750.50, 'EUR', 1);

-- Insert new account 3
INSERT INTO accounts (ID, OWNER_NAME, BALANCE, CURRENCY, VERSION)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bcb', 'Onasis', 100.00, 'USD', 1);

-- Insert new account 4
INSERT INTO accounts (ID, OWNER_NAME, BALANCE, CURRENCY, VERSION)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe9','Bakogiannis', 75.50, 'CAD', 1);

-- Insert default account
INSERT INTO accounts (ID, OWNER_NAME, BALANCE, CURRENCY, VERSION)
VALUES ('00000000-0000-0000-0000-000000000000', 'Unnamed' ,0.0, 'NOT_DEFINED', 1);
