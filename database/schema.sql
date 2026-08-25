-- =====================================================================
-- HostelCare - Database Schema
-- Run this whole file once: mysql -u root -p < schema.sql
-- (or paste it into MySQL Workbench and execute)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hostel_maintenance;
USE hostel_maintenance;

-- ---------------------------------------------------------------------
-- students
-- ---------------------------------------------------------------------
CREATE TABLE students (
    student_id    INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(15),
    password      VARCHAR(255) NOT NULL,   -- "salt:hash", both Base64 (see service.PasswordUtil)
    room_number   VARCHAR(20)  NOT NULL,
    hostel_block  VARCHAR(50)
);

-- ---------------------------------------------------------------------
-- admins
-- ---------------------------------------------------------------------
CREATE TABLE admins (
    admin_id   INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------------------
-- maintenance_staff
-- (email/password added beyond the original spec so staff can log in)
-- ---------------------------------------------------------------------
CREATE TABLE maintenance_staff (
    staff_id        INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(15),
    password        VARCHAR(255) NOT NULL,
    specialization  VARCHAR(30) NOT NULL,   -- matches enums.ComplaintCategory name()
    availability    BOOLEAN DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- rooms  (simple reference table; not heavily used yet, kept per spec)
-- ---------------------------------------------------------------------
CREATE TABLE rooms (
    room_number   VARCHAR(20) PRIMARY KEY,
    hostel_block  VARCHAR(50),
    capacity      INT DEFAULT 2
);

-- ---------------------------------------------------------------------
-- complaints
-- ---------------------------------------------------------------------
CREATE TABLE complaints (
    complaint_id       VARCHAR(20) PRIMARY KEY,   -- e.g. CMP1001
    student_id         INT NOT NULL,
    room_number        VARCHAR(20) NOT NULL,
    category           VARCHAR(30) NOT NULL,      -- enums.ComplaintCategory name()
    description        TEXT NOT NULL,
    priority           VARCHAR(10) NOT NULL,      -- LOW / MEDIUM / HIGH / CRITICAL
    status             VARCHAR(15) NOT NULL,      -- SUBMITTED..CLOSED
    assigned_staff_id  INT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at        TIMESTAMP NULL,

    -- optional, category-specific fields
    meal               VARCHAR(20)  NULL,   -- FOOD_*: Breakfast / Lunch / Dinner
    food_item          VARCHAR(100) NULL,
    machine_number     VARCHAR(20)  NULL,   -- WASHING_MACHINE
    machine_location   VARCHAR(100) NULL,

    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (assigned_staff_id) REFERENCES maintenance_staff(staff_id)
);

-- ---------------------------------------------------------------------
-- complaint_images
-- ---------------------------------------------------------------------
CREATE TABLE complaint_images (
    image_id      INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id  VARCHAR(20) NOT NULL,
    image_name    VARCHAR(255) NOT NULL,
    image_path    VARCHAR(500) NOT NULL,
    image_type    VARCHAR(10),
    image_size    BIGINT,
    image_kind    VARCHAR(12) NOT NULL DEFAULT 'BEFORE',  -- BEFORE | RESOLUTION
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id)
);

-- ---------------------------------------------------------------------
-- complaint_updates  (timeline / work log)
-- ---------------------------------------------------------------------
CREATE TABLE complaint_updates (
    update_id     INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id  VARCHAR(20) NOT NULL,
    staff_id      INT NULL,
    old_status    VARCHAR(15),
    new_status    VARCHAR(15),
    comment       VARCHAR(500),
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id),
    FOREIGN KEY (staff_id) REFERENCES maintenance_staff(staff_id)
);

CREATE INDEX idx_complaints_student ON complaints(student_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_staff ON complaints(assigned_staff_id);
CREATE INDEX idx_updates_complaint ON complaint_updates(complaint_id);

-- =====================================================================
-- SAMPLE DATA
-- Passwords below were generated with service.PasswordUtil (SHA-256 + salt).
--   Student login -> sai@hostel.com   / password123
--   Admin login   -> admin@hostel.com / admin123
--   Staff login   -> ravi@hostel.com  / staff123
-- =====================================================================

INSERT INTO students (name, email, phone, password, room_number, hostel_block) VALUES
('Sai Kumar',  'sai@hostel.com',  '9876543210',
 'B1n/VIN/amLDTyxRppn1rA==:NJ/9YkNP/oBQoDoAe42UlQ9xeivVRdNmj6Z16Kq9lRo=', 'A-204', 'Block A'),
('Ravi Teja',  'raviteja@hostel.com', '9876543211',
 'B1n/VIN/amLDTyxRppn1rA==:NJ/9YkNP/oBQoDoAe42UlQ9xeivVRdNmj6Z16Kq9lRo=', 'B-112', 'Block B'),
('Anil Reddy', 'anil@hostel.com', '9876543212',
 'B1n/VIN/amLDTyxRppn1rA==:NJ/9YkNP/oBQoDoAe42UlQ9xeivVRdNmj6Z16Kq9lRo=', 'A-105', 'Block A');

INSERT INTO admins (name, email, password) VALUES
('Warden Priya Sharma', 'admin@hostel.com',
 'SCfZE5hhIqMgIVlSa7YpeQ==:cvds8vmQ0+hnjBs4phnipZTQu//Quga+VtitxNpmXAU=');

INSERT INTO maintenance_staff (name, email, phone, password, specialization, availability) VALUES
('Ravi Kumar',  'ravi@hostel.com',  '9000000001',
 'SWkRqBp+MeW6jt1wJQnraQ==:xsJPJt9KP2uPteeRDfxe+edLhFhmmB2Jz3Gl4lwLieM=', 'PLUMBING', TRUE),
('Anil Verma',  'anilv@hostel.com', '9000000002',
 'SWkRqBp+MeW6jt1wJQnraQ==:xsJPJt9KP2uPteeRDfxe+edLhFhmmB2Jz3Gl4lwLieM=', 'ELECTRICAL', TRUE),
('Suresh Babu', 'suresh@hostel.com','9000000003',
 'SWkRqBp+MeW6jt1wJQnraQ==:xsJPJt9KP2uPteeRDfxe+edLhFhmmB2Jz3Gl4lwLieM=', 'WASHING_MACHINE', TRUE),
('Meena Iyer',  'meena@hostel.com', '9000000004',
 'SWkRqBp+MeW6jt1wJQnraQ==:xsJPJt9KP2uPteeRDfxe+edLhFhmmB2Jz3Gl4lwLieM=', 'FOOD_HYGIENE', TRUE);

INSERT INTO rooms (room_number, hostel_block, capacity) VALUES
('A-204', 'Block A', 2), ('B-112', 'Block B', 2), ('A-105', 'Block A', 3);

-- A couple of starter complaints so the dashboards aren't empty on first run.
INSERT INTO complaints (complaint_id, student_id, room_number, category, description, priority, status, assigned_staff_id) VALUES
('CMP1001', 1, 'A-204', 'PLUMBING', 'Bathroom pipe is leaking near the shower.', 'HIGH', 'ASSIGNED', 1),
('CMP1002', 2, 'B-112', 'WIFI', 'Wi-Fi disconnects every few minutes in the evening.', 'MEDIUM', 'PENDING', NULL);

INSERT INTO complaint_updates (complaint_id, staff_id, old_status, new_status, comment) VALUES
('CMP1001', NULL, 'SUBMITTED', 'PENDING', 'Awaiting admin review.'),
('CMP1001', NULL, 'PENDING', 'ASSIGNED', 'Assigned to Ravi Kumar.'),
('CMP1002', NULL, 'SUBMITTED', 'PENDING', 'Awaiting admin review.');
