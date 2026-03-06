INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('BIDDER');

INSERT INTO users (username, email, password, role_id, enabled, created_at, updated_at)
VALUES
    ('admin', 'admin@auction.local', 'Admin@123', (SELECT id FROM roles WHERE name = 'ADMIN'), 1, NOW(6), NOW(6)),
    ('bidder1', 'bidder1@auction.local', 'Bidder@123', (SELECT id FROM roles WHERE name = 'BIDDER'), 1, NOW(6), NOW(6));

