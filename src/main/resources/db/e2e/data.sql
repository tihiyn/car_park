INSERT INTO roles (id, name) VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_MANAGER'),
  (3, 'ROLE_USER');

INSERT INTO users (id, username, password) VALUES
  (1, 'АнисимовВС', '$2a$10$R3/FIqXARnpJXcRHoe9Qlu51qc7QaQuCZKWyi.PqDkQhaAQqa0NZa'),
  (2, 'БородинДИ',  '$2a$10$ZNrdJfqkaPpcl93PoZhr0ueoNExcDPmSp3xPLDHLY.eTgQqIBqth2');

INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 2),
  (2, 3);

INSERT INTO managers (id, surname, name, salary, email, user_id) VALUES
  (1, 'Анисимов', 'Владимир', 600000.00, 'anisimov.v.s@mail.ru', 1),
  (2, 'Бородин',  'Денис',    500000.00, 'borodin.d.i@mail.ru',  2);

INSERT INTO enterprises (id, name, city, registration_number, time_zone) VALUES
  (1, 'ТрансАвто',     'Чикаго',          '1234567890', 'America/Chicago'),
  (2, 'Северный путь', 'Санкт-Петербург', '0987654321', 'UTC'),
  (3, 'Магистраль',    'Казань',          '8309482916', 'UTC');

INSERT INTO manager_enterprise_assignments (manager_id, enterprise_id) VALUES
  (1, 1),
  (1, 2),
  (2, 2),
  (2, 3);

INSERT INTO brand (id, name, type, transmission, engine_volume, engine_power, num_of_seats) VALUES
  (1,  'Toyota Camry', 'Седан',       'AUTOMATIC',  2.5, 203, 5),
  (2,  'Ford Transit', 'Фургон',      'MECHANICAL', 2.0, 170, 9),
  (3,  'BMW X5',       'Внедорожник', 'AUTOMATIC',  3.0, 340, 5),
  (9,  'Audi A5',      'Фастбэк',     'AUTOMATIC',  2.8, 250, 5),
  (10, 'Kia Rio',      'Седан',       'AUTOMATIC',  1.6, 150, 5),
  (14, 'Без названия', '',            'NONE',       0.0, 0,   0);

INSERT INTO drivers (id, first_name, last_name, driver_license, salary, phone_number, enterprise_id) VALUES
  (1,  'Иван',    'Иванов',   'DL-0001',  80000.00, '+7 900 000-00-01', 1),
  (2,  'Пётр',    'Петров',   'DL-0002',  95000.00, '+7 900 000-00-02', 1),
  (3,  'Сидор',   'Сидоров',  'DL-0003', 110000.00, '+7 900 000-00-03', 1),
  (4,  'Алексей', 'Кузнецов', 'DL-0004',  72000.00, '+7 900 000-00-04', 1),
  (5,  'Дмитрий', 'Смирнов',  'DL-0005',  88000.00, '+7 900 000-00-05', 1),
  (6,  'Андрей',  'Волков',   'DL-0006',  90000.00, '+7 900 000-00-06', 2),
  (7,  'Игорь',   'Морозов',  'DL-0007', 105000.00, '+7 900 000-00-07', 2),
  (8,  'Сергей',  'Новиков',  'DL-0008',  78000.00, '+7 900 000-00-08', 2),
  (9,  'Никита',  'Фёдоров',  'DL-0009',  99000.00, '+7 900 000-00-09', 3),
  (10, 'Павел',   'Егоров',   'DL-0010',  84000.00, '+7 900 000-00-10', 3);

INSERT INTO vehicle (id, reg_num, price, mileage, production_year, color, is_available, purchase_datetime, brand_id, enterprise_id, active_driver_id) VALUES
  (1,  'A083AE', 2046000, 184611, 2018, 'Золотой',     TRUE,  NULL, 3,  1, NULL),
  (2,  'B120KX', 1450000,  52300, 2020, 'Белый',       TRUE,  NULL, 1,  1, NULL),
  (3,  'C455MH',  980000, 121000, 2015, 'Чёрный',      TRUE,  NULL, 10, 1, NULL),
  (4,  'E777OP', 3100000,  15400, 2022, 'Синий',       TRUE,  NULL, 9,  1, NULL),
  (5,  'K301TY', 1750000,  88900, 2018, 'Серый',       FALSE, NULL, 2,  1, NULL),
  (6,  'M512BC',  640000, 240500, 1998, 'Зелёный',     TRUE,  NULL, 1,  1, NULL),
  (7,  'H233AB', 1290000,  67400, 2019, 'Красный',     TRUE,  NULL, 10, 2, NULL),
  (8,  'O884EK', 2410000,  33200, 2021, 'Белый',       TRUE,  NULL, 3,  2, NULL),
  (9,  'T105CX',  870000, 195700, 2010, 'Синий',       TRUE,  NULL, 2,  2, NULL),
  (10, 'Y640HP', 1980000,  45900, 2017, 'Серебристый', TRUE,  NULL, 9,  3, NULL),
  (11, 'P661YC', 2565000,  77621, 1970, 'Бордовый',    TRUE,  NULL, 10, 3, NULL);

INSERT INTO vehicle_driver_assignments (vehicle_id, driver_id) VALUES
  (2, 1),
  (2, 2),
  (3, 3),
  (7, 6);

INSERT INTO vehicle_locations (id, location, timestamp, vehicle_id) VALUES
  (1, 'SRID=4326;POINT(37.6173 55.7558)', TIMESTAMP WITH TIME ZONE '2023-12-11 17:13:30+03', 1),
  (2, 'SRID=4326;POINT(37.6512 55.7301)', TIMESTAMP WITH TIME ZONE '2023-12-11 18:25:00+03', 1),
  (3, 'SRID=4326;POINT(37.5891 55.7712)', TIMESTAMP WITH TIME ZONE '2024-02-18 16:29:10+03', 1),
  (4, 'SRID=4326;POINT(37.6045 55.7489)', TIMESTAMP WITH TIME ZONE '2024-02-18 17:12:20+03', 1),
  (5, 'SRID=4326;POINT(37.5320 55.7025)', TIMESTAMP WITH TIME ZONE '2024-04-14 05:33:29+03', 1),
  (6, 'SRID=4326;POINT(37.6788 55.8134)', TIMESTAMP WITH TIME ZONE '2024-04-14 07:06:29+03', 1),
  (7, 'SRID=4326;POINT(37.6402 55.7566)', TIMESTAMP WITH TIME ZONE '2024-08-26 15:41:05+03', 1),
  (8, 'SRID=4326;POINT(37.5677 55.7890)', TIMESTAMP WITH TIME ZONE '2024-08-26 16:31:15+03', 1);

INSERT INTO trips (id, begin, "end", length, vehicle_id, begin_location_id, end_location_id) VALUES
  (1, TIMESTAMP WITH TIME ZONE '2023-12-11 17:13:30+03', TIMESTAMP WITH TIME ZONE '2023-12-11 18:25:00+03', 27, 1, 1, 2),
  (2, TIMESTAMP WITH TIME ZONE '2024-02-18 16:29:10+03', TIMESTAMP WITH TIME ZONE '2024-02-18 17:12:20+03', 15, 1, 3, 4),
  (3, TIMESTAMP WITH TIME ZONE '2024-04-14 05:33:29+03', TIMESTAMP WITH TIME ZONE '2024-04-14 07:06:29+03', 49, 1, 5, 6),
  (4, TIMESTAMP WITH TIME ZONE '2024-08-26 15:41:05+03', TIMESTAMP WITH TIME ZONE '2024-08-26 16:31:15+03', 18, 1, 7, 8);

ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE managers ALTER COLUMN id RESTART WITH 100;
ALTER TABLE enterprises ALTER COLUMN id RESTART WITH 100;
ALTER TABLE brand ALTER COLUMN id RESTART WITH 100;
ALTER TABLE drivers ALTER COLUMN id RESTART WITH 100;
ALTER TABLE vehicle ALTER COLUMN id RESTART WITH 100;
ALTER TABLE vehicle_locations ALTER COLUMN id RESTART WITH 100;
ALTER TABLE trips ALTER COLUMN id RESTART WITH 100;
