-- Initialize embedded H2 database
-- Insert data into the account table
INSERT INTO accounts (id, balance, currency, created_at, version) VALUES
  (RANDOM_UUID(), 1000.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (RANDOM_UUID(), 500.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (RANDOM_UUID(), 800.00, 'EUR', CURRENT_TIMESTAMP, 0),
  (RANDOM_UUID(), 2000.00, 'EUR', CURRENT_TIMESTAMP, 0);
