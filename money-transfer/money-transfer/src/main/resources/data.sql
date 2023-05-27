-- Initialize embedded H2 database

-- Create the account table
DROP TABLE IF EXISTS accounts ;

  CREATE TABLE accounts (
  account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  balance DECIMAL(10, 2),
  currency VARCHAR(5),
  created_at TIMESTAMP
);

-- Insert data into the account table
INSERT INTO accounts (balance, currency, created_at) VALUES
  (1000.00, 'EUR', CURRENT_TIMESTAMP),
  (500.00, 'EUR', CURRENT_TIMESTAMP),
  (2000.00, 'EUR', CURRENT_TIMESTAMP);
