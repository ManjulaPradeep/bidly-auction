CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DECIMAL(19,2) NOT NULL,
    current_highest_bid DECIMAL(19,2),
    auction_end_time DATETIME(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by BIGINT NOT NULL,
    winner_bid_id BIGINT,
    version BIGINT DEFAULT 0 NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_item_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    bid_amount DECIMAL(19,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_bid_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_bid_user FOREIGN KEY (bidder_id) REFERENCES users(id)
);

ALTER TABLE items
    ADD CONSTRAINT fk_items_winner_bid FOREIGN KEY (winner_bid_id) REFERENCES bids(id);

CREATE INDEX idx_items_status_end ON items(status, auction_end_time);
CREATE INDEX idx_bids_item_amount_created ON bids(item_id, bid_amount, created_at);

