-- Initialize embedded H2 database
-- Insert data into the account table
INSERT INTO accounts (balance, currency, created_at) VALUES
  (1000.00, 'EUR', CURRENT_TIMESTAMP),
  (500.00, 'EUR', CURRENT_TIMESTAMP),
  (2000.00, 'EUR', CURRENT_TIMESTAMP);
