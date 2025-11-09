CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- roles
INSERT INTO roles (id, code, name, is_active, opt_lock, created_at, created_by)
VALUES
    (gen_random_uuid(), 'SA',       'Super Admin',   TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'CUSTOMER', 'Customer',      TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'GATEWAY',  'Gateway',       TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'SYSTEM',   'System',        TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001');

-- status_types
INSERT INTO status_types (id, code, name, is_active, opt_lock, created_at, created_by)
VALUES
    (gen_random_uuid(), 'PROCESSING', 'Processing', TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'APPROVED',   'Approved',   TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'REJECTED',   'Rejected',   TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'CANCELLED',  'Cancelled',  TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001');

-- product_types
INSERT INTO product_types (id, code, name, is_active, opt_lock, created_at, created_by)
VALUES
    (gen_random_uuid(), 'ELECTRICITY_TOKEN', 'Electricity Token', TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'INTERNET_BILL',     'Internet Bill',     TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'WATER_BILL',        'Water Bill',        TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001');

-- payment_types
INSERT INTO payment_types (id, code, name, is_active, opt_lock, created_at, created_by)
VALUES
    (gen_random_uuid(), 'QRIS',          'QRIS',            TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'VA',            'Virtual Account', TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), 'BANK_TRANSFER', 'Bank Transfer',   TRUE, 0, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001');

-- users
INSERT INTO users (id, full_name, email, password, role_id, is_active, opt_lock, created_at, created_by)
VALUES
  (
    gen_random_uuid(),
    'System User',
    'system@ops.local',
    '$2a$12$CspfemU8FcWj2jRkUKXUXOpv9/jcvDQHWpMXOytCXRrtGMkCB1PMC',
    (SELECT id FROM roles WHERE code = 'SYSTEM'),
    TRUE, 0, CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE email = 'sa@ops.local')
  ),
  (
    gen_random_uuid(),
    'Super Admin',
    'sa@ops.local',
    '$2a$12$CspfemU8FcWj2jRkUKXUXOpv9/jcvDQHWpMXOytCXRrtGMkCB1PMC',
    (SELECT id FROM roles WHERE code = 'SA'),
    TRUE, 0, CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE email = 'sa@ops.local')
  ),
  (
    '65e5db34-ad5b-438e-8934-73cd38e3d921',
    'Customer 1',
    'user@example.com',
    '$2a$12$CspfemU8FcWj2jRkUKXUXOpv9/jcvDQHWpMXOytCXRrtGMkCB1PMC',
    (SELECT id FROM roles WHERE code = 'CUSTOMER'),
    TRUE, 0, CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE email = 'sa@ops.local')
  );

-- payments
INSERT INTO payments (
    id,
    customer_id,
    product_type_id,
    payment_type_id,
    customer_number,
    amount,
    status_type_id,
    description,
    gateway_note,
    reference_no,
    received_at,
    is_active,
    opt_lock,
    created_at,
    created_by)
VALUES
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0001', 50000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 1', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0002', 150000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 1', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0003', 75000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 1', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0004', 90000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 2', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0005', 180000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 2', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0006', 82000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 2', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0007', 60000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 3', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0008', 200000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 3', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0009', 70000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 3', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0010', 105000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 4', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0011', 165000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 4', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0012', 91000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 4', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0013', 88000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 5', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0014', 210000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 5', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0015', 76000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 5', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0016', 64000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 6', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'BANK_TRANSFER'),
        'CUST-0017', 175000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 6', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'WATER_BILL'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0018', 84000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Water bill 6', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'ELECTRICITY_TOKEN'),
        (SELECT id FROM payment_types WHERE code = 'QRIS'),
        'CUST-0019', 99000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Token purchase 7', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local')),
    (gen_random_uuid(), '65e5db34-ad5b-438e-8934-73cd38e3d921',
        (SELECT id FROM product_types WHERE code = 'INTERNET_BILL'),
        (SELECT id FROM payment_types WHERE code = 'VA'),
        'CUST-0020', 190000, (SELECT id FROM status_types WHERE code = 'PROCESSING'),
        'Internet bill 7', NULL, NULL, NULL, TRUE, 0, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'sa@ops.local'));
