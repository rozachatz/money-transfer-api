-- Initialize embedded H2 database
-- Insert data into the account table
INSERT INTO accounts (balance, currency, created_at, version) VALUES
  (1000.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (500.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (800.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (2000.00, 'EUR', CURRENT_TIMESTAMP, 0);
