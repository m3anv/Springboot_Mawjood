CREATE DATABASE IF NOT EXISTS mawjood_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mawjood_db;

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(80),
    address VARCHAR(180),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS offices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    location_id BIGINT NOT NULL,
    contact_email VARCHAR(120),
    contact_phone VARCHAR(40),
    created_at DATETIME,
    CONSTRAINT fk_offices_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    organization_name VARCHAR(150),
    entity_type VARCHAR(50),
    office_id BIGINT,
    created_at DATETIME,
    CONSTRAINT fk_users_office FOREIGN KEY (office_id) REFERENCES offices(id)
);

CREATE TABLE IF NOT EXISTS lost_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    category VARCHAR(100) NOT NULL,
    color VARCHAR(50),
    description TEXT,
    lost_date DATE NOT NULL,
    lost_area VARCHAR(160) NOT NULL,
    contact_info VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_lost_reports_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_lost_reports_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS found_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    office_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    category VARCHAR(100) NOT NULL,
    color VARCHAR(50),
    description TEXT,
    found_date DATE NOT NULL,
    found_area VARCHAR(160) NOT NULL,
    storage_reference VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'IN_INVENTORY',
    created_at DATETIME,
    CONSTRAINT fk_found_items_office FOREIGN KEY (office_id) REFERENCES offices(id),
    CONSTRAINT fk_found_items_location FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT fk_found_items_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS case_matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lost_report_id BIGINT NOT NULL,
    found_item_id BIGINT NOT NULL,
    office_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at DATETIME,
    CONSTRAINT fk_matches_lost_report FOREIGN KEY (lost_report_id) REFERENCES lost_reports(id),
    CONSTRAINT fk_matches_found_item FOREIGN KEY (found_item_id) REFERENCES found_items(id),
    CONSTRAINT fk_matches_office FOREIGN KEY (office_id) REFERENCES offices(id)
);

CREATE TABLE IF NOT EXISTS case_updates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lost_report_id BIGINT NOT NULL,
    office_id BIGINT,
    created_by BIGINT,
    status VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    visible_to_user TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME,
    CONSTRAINT fk_updates_lost_report FOREIGN KEY (lost_report_id) REFERENCES lost_reports(id),
    CONSTRAINT fk_updates_office FOREIGN KEY (office_id) REFERENCES offices(id),
    CONSTRAINT fk_updates_user FOREIGN KEY (created_by) REFERENCES users(id)
);

INSERT IGNORE INTO locations (id, name, type, address, created_at) VALUES
(1, 'Red Sea Mall', 'Mall', 'Jeddah - King Abdulaziz Road', NOW()),
(2, 'Airport Terminal 1', 'Airport', 'King Abdulaziz International Airport', NOW()),
(3, 'University Campus', 'University', 'Main campus services building', NOW()),
(4, 'Central Park', 'Park', 'Main visitor center', NOW());

INSERT IGNORE INTO offices (id, name, location_id, contact_email, contact_phone, created_at) VALUES
(1, 'Red Sea Mall Lost & Found Office', 1, 'redsea.office@mawjood.com', '+966500000001', NOW()),
(2, 'Airport Terminal 1 Lost & Found Office', 2, 'airport.t1@mawjood.com', '+966500000002', NOW()),
(3, 'University Campus Lost & Found Office', 3, 'campus.office@mawjood.com', '+966500000003', NOW()),
(4, 'Central Park Lost & Found Office', 4, 'park.office@mawjood.com', '+966500000004', NOW());

INSERT IGNORE INTO users (id, name, email, password, role, office_id, created_at) VALUES
(1, 'System Admin', 'admin@mawjood.com', 'admin123', 'ADMIN', NULL, NOW()),
(2, 'Red Sea Mall Office', 'redsea.office@mawjood.com', 'office123', 'OFFICE', 1, NOW()),
(3, 'Airport Terminal 1 Office', 'airport.office@mawjood.com', 'office123', 'OFFICE', 2, NOW()),
(4, 'University Campus Office', 'campus.office@mawjood.com', 'office123', 'OFFICE', 3, NOW()),
(5, 'Central Park Office', 'park.office@mawjood.com', 'office123', 'OFFICE', 4, NOW());

UPDATE users SET role = 'OFFICE', office_id = COALESCE(office_id, 1) WHERE role IN ('ORG_APPROVED', 'ORG_PENDING');
