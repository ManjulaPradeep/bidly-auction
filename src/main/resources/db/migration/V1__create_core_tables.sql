CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DECIMAL(15,2) NOT NULL,
    current_highest_bid DECIMAL(15,2),
    auction_end_time DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by BIGINT NOT NULL,
    winner_bid_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    bid_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bids_item
        FOREIGN KEY (item_id)
        REFERENCES items(id),

    CONSTRAINT fk_bids_user
        FOREIGN KEY (bidder_id)
        REFERENCES users(id)
);
