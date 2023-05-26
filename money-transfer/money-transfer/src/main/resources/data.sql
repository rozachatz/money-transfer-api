-- Create the account table
DROP TABLE IF EXISTS accounts ;

  CREATE TABLE accounts (
  account_id BIGINT PRIMARY KEY,
  balance DECIMAL(10, 2),
  currency VARCHAR(5),
  created_at TIMESTAMP
);

-- Insert data into the account table
INSERT INTO accounts (account_id, balance, currency, created_at) VALUES
  (1, 1000.00, 'USD', CURRENT_TIMESTAMP),
  (2, 500.00, 'EUR', CURRENT_TIMESTAMP),
  (3, 2000.00, 'GBP', CURRENT_TIMESTAMP);
