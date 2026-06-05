-- Пользователь 1: Иван Петров (пароль: password123)
INSERT INTO users (id, name, date_of_birth, password)
SELECT 1, 'Иван Петров', '1990-05-15', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

INSERT INTO email_data (user_id, email)
SELECT 1, 'ivan@mail.ru'
    WHERE NOT EXISTS (SELECT 1 FROM email_data WHERE user_id = 1 AND email = 'ivan@mail.ru');

INSERT INTO phone_data (user_id, phone)
SELECT 1, '79201234567'
    WHERE NOT EXISTS (SELECT 1 FROM phone_data WHERE user_id = 1 AND phone = '79201234567');

INSERT INTO accounts (user_id, balance, initial_balance)
SELECT 1, 1000.00, 1000.00
    WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 1);

-- Пользователь 2: Мария Смирнова
INSERT INTO users (id, name, date_of_birth, password)
SELECT 2, 'Мария Смирнова', '1995-03-20', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 2);

INSERT INTO email_data (user_id, email)
SELECT 2, 'maria@mail.ru'
    WHERE NOT EXISTS (SELECT 1 FROM email_data WHERE user_id = 2 AND email = 'maria@mail.ru');

INSERT INTO phone_data (user_id, phone)
SELECT 2, '79209876543'
    WHERE NOT EXISTS (SELECT 1 FROM phone_data WHERE user_id = 2 AND phone = '79209876543');

INSERT INTO accounts (user_id, balance, initial_balance)
SELECT 2, 500.00, 500.00
    WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 2);

-- Пользователь 3: Алексей Сидоров
INSERT INTO users (id, name, date_of_birth, password)
SELECT 3, 'Алексей Сидоров', '1988-11-10', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 3);

INSERT INTO email_data (user_id, email)
SELECT 3, 'alexey@mail.ru'
    WHERE NOT EXISTS (SELECT 1 FROM email_data WHERE user_id = 3 AND email = 'alexey@mail.ru');

INSERT INTO phone_data (user_id, phone)
SELECT 3, '79205551234'
    WHERE NOT EXISTS (SELECT 1 FROM phone_data WHERE user_id = 3 AND phone = '79205551234');

INSERT INTO accounts (user_id, balance, initial_balance)
SELECT 3, 2000.00, 2000.00
    WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = 3);

-- Сброс последовательности ID (только если таблица пустая)
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 3), true);