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
