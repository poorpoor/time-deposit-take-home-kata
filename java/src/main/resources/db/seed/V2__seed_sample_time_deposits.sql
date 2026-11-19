-- Sample data for local exploration via Swagger UI.
-- Loaded only when the "demo" Spring profile is active.

INSERT INTO "timeDeposits" (id, "planType", days, balance) VALUES
    (1, 'basic',   45,  1234567.00),
    (2, 'basic',   20,  5000.00),
    (3, 'student', 120, 10000.00),
    (4, 'student', 366, 8500.00),
    (5, 'premium', 60,  50000.00),
    (6, 'premium', 45,  25000.00);

INSERT INTO withdrawals (id, "timeDepositId", amount, date) VALUES
    (1, 1, 500.00,  DATE '2026-01-15'),
    (2, 1, 1200.50, DATE '2026-02-01'),
    (3, 3, 250.00,  DATE '2026-01-20'),
    (4, 5, 3000.00, DATE '2026-02-10');

SELECT setval(pg_get_serial_sequence('"timeDeposits"', 'id'), (SELECT COALESCE(MAX(id), 1) FROM "timeDeposits"));
SELECT setval(pg_get_serial_sequence('withdrawals', 'id'), (SELECT COALESCE(MAX(id), 1) FROM withdrawals));
