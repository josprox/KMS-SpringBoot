CREATE TABLE admin_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_ADMIN'
);

CREATE TABLE activations (
    id SERIAL PRIMARY KEY,
    machine_name VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    software_name VARCHAR(255) NOT NULL,
    activation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NOT NULL
);

-- Insert a default admin user if needed (password is admin123 hashed with BCrypt)
-- Hash for 'admin123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2
INSERT INTO admin_users (username, password_hash, role) 
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;
