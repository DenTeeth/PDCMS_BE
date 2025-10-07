-- Migration: create customer_contacts, contact_history, appointments
-- Use your migration tool to apply or run manually against the DB.

CREATE TABLE IF NOT EXISTS customer_contacts (
  contact_id VARCHAR(36) NOT NULL PRIMARY KEY,
  full_name VARCHAR(100),
  phone VARCHAR(15),
  email VARCHAR(100),
  source VARCHAR(50),
  status VARCHAR(50),
  assigned_to VARCHAR(36),
  converted_patient_id VARCHAR(36),
  notes TEXT,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  INDEX idx_contact_assigned_to (assigned_to)
  -- foreign key to employees added below after employees table existence verified
);

CREATE TABLE IF NOT EXISTS contact_history (
  history_id VARCHAR(36) NOT NULL PRIMARY KEY,
  contact_id VARCHAR(36) NOT NULL,
  employee_id VARCHAR(36),
  action VARCHAR(50),
  content TEXT,
  created_at DATETIME(6),
  INDEX idx_history_contact (contact_id)
  -- foreign keys added below
);

CREATE TABLE IF NOT EXISTS appointments (
  appointment_id VARCHAR(36) NOT NULL PRIMARY KEY,
  appointment_code VARCHAR(10),
  patient_id VARCHAR(36),
  doctor_id VARCHAR(36),
  appointment_date DATE,
  start_time TIME,
  end_time TIME,
  type VARCHAR(50),
  status VARCHAR(50),
  reason TEXT,
  notes TEXT,
  created_by VARCHAR(36),
  created_at DATETIME(6),
  updated_at DATETIME(6),
  INDEX idx_appointments_patient (patient_id),
  INDEX idx_appointments_doctor_date (doctor_id, appointment_date)
  -- foreign keys added below
);

-- Optionally add foreign keys if the referenced tables exist:
-- ALTER TABLE customer_contacts ADD CONSTRAINT fk_contact_assigned_employee FOREIGN KEY (assigned_to) REFERENCES employees(employee_id);
-- ALTER TABLE contact_history ADD CONSTRAINT fk_history_contact FOREIGN KEY (contact_id) REFERENCES customer_contacts(contact_id);
-- ALTER TABLE contact_history ADD CONSTRAINT fk_history_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);
-- ALTER TABLE appointments ADD CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id);
-- ALTER TABLE appointments ADD CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES employees(employee_id);
