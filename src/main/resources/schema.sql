
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(500) NOT NULL,
    date_of_birth DATE NOT NULL,
    password VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Добавить колонку role
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE TABLE IF NOT EXISTS accounts (
                                        id BIGSERIAL PRIMARY KEY,
                                        user_id BIGINT UNIQUE NOT NULL,
                                        balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    initial_balance DECIMAL(19,2) NOT NULL,
    last_interest_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS email_data (
                                          id BIGSERIAL PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          email VARCHAR(200) UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS phone_data (
                                          id BIGSERIAL PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          phone VARCHAR(13) UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Таблица истории импортов
CREATE TABLE IF NOT EXISTS import_history (
                                              id BIGSERIAL PRIMARY KEY,
                                              filename VARCHAR(255) NOT NULL,
                                              format VARCHAR(10) NOT NULL,
                                              total_records INTEGER NOT NULL DEFAULT 0,
                                              successful_records INTEGER NOT NULL DEFAULT 0,
                                              failed_records INTEGER NOT NULL DEFAULT 0,
                                              status VARCHAR(20) NOT NULL,
                                              error_message TEXT,
                                              imported_by BIGINT NOT NULL,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (imported_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX  IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX  IF NOT EXISTS idx_users_date_of_birth ON users(date_of_birth);
CREATE INDEX  IF NOT EXISTS idx_email_data_email ON email_data(email);
CREATE INDEX  IF NOT EXISTS idx_phone_data_phone ON phone_data(phone);
CREATE INDEX IF NOT EXISTS idx_import_history_created_at ON import_history(created_at);
CREATE INDEX IF NOT EXISTS idx_import_history_imported_by ON import_history(imported_by);