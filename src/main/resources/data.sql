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
