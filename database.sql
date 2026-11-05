-- Курсова робота «Кросплатформене програмування», варіант №13
-- Інформаційна система технічних заявок готельного комплексу
-- Скрипт створює базу даних з нуля та додає демонстраційні дані.

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS hotel_requests;
CREATE DATABASE hotel_requests CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_requests;

CREATE TABLE room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number VARCHAR(10) NOT NULL UNIQUE,
    floor INT NOT NULL,
    category VARCHAR(40) NOT NULL,
    capacity INT NOT NULL,
    equipment VARCHAR(500) NOT NULL
);

CREATE TABLE technician (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    specialization VARCHAR(80) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

CREATE TABLE service_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    technician_id BIGINT NULL,
    description VARCHAR(500) NOT NULL,
    guest_impact INT NOT NULL,
    severity INT NOT NULL,
    downtime_hours DOUBLE NOT NULL,
    state VARCHAR(20) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    priority_index DOUBLE NOT NULL,
    created_date DATE NOT NULL,
    FOREIGN KEY (room_id) REFERENCES room(id),
    FOREIGN KEY (technician_id) REFERENCES technician(id)
);

CREATE TABLE work_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    description VARCHAR(500) NOT NULL,
    hours_spent DOUBLE NOT NULL,
    FOREIGN KEY (request_id) REFERENCES service_request(id),
    FOREIGN KEY (technician_id) REFERENCES technician(id)
);

INSERT INTO room(number, floor, category, capacity, equipment) VALUES
('101', 1, 'Стандарт', 2, 'Кондиціонер, телевізор, холодильник, фен'),
('102', 1, 'Стандарт', 2, 'Кондиціонер, телевізор, фен'),
('205', 2, 'Напівлюкс', 3, 'Кондиціонер, телевізор, міні-бар, сейф, чайник'),
('301', 3, 'Люкс', 4, 'Кондиціонер, Smart TV, міні-бар, сейф, джакузі, кавомашина');

INSERT INTO technician(full_name, specialization, phone) VALUES
('Коваленко Петро Іванович', 'Електрик', '+380501234567'),
('Мельник Андрій Сергійович', 'Сантехнік', '+380672345678'),
('Бондар Олег Вікторович', 'Інженер з кондиціонування', '+380933456789');

-- F1 = guest_impact * 10; F2 = (severity - 1) * 25; F3 = min(100, downtime_hours / 48 * 100)
-- I = 0.40*F1 + 0.35*F2 + 0.25*F3
INSERT INTO service_request(room_id, technician_id, description, guest_impact, severity, downtime_hours,
                            state, f1, f2, f3, priority_index, created_date) VALUES
(1, NULL, 'Не працює кондиціонер, у номері +29 °C', 8, 4, 12, 'NEW', 80, 75, 25, 64.5, '2026-10-20'),
(3, 2, 'Протікає змішувач у ванній кімнаті', 6, 3, 24, 'ASSIGNED', 60, 50, 50, 54.0, '2026-10-24'),
(4, 1, 'Не вмикається освітлення у вітальні', 5, 2, 3, 'IN_PROGRESS', 50, 25, 6.25, 30.31, '2026-10-28'),
(2, 3, 'Шумить вентилятор кондиціонера', 3, 2, 0, 'DONE', 30, 25, 0, 20.75, '2026-10-22');

INSERT INTO work_record(request_id, technician_id, work_date, description, hours_spent) VALUES
(3, 1, '2026-10-29', 'Перевірено проводку, замінено автоматичний вимикач', 1.5),
(4, 3, '2026-10-23', 'Почищено фільтри, змащено підшипник вентилятора', 2.0);
