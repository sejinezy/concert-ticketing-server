INSERT INTO venues (id, name, address, created_at, updated_at)
VALUES
    (1, '올림픽홀', '서울특별시 송파구 올림픽로 424', NOW(), NOW());

INSERT INTO venue_seats (
    id,
    venue_id,
    section,
    row_label,
    seat_no,
    seat_code,
    created_at,
    updated_at
)
VALUES
    (1, 1, 'A', '1', '1', 'A-1-1', NOW(), NOW()),
    (2, 1, 'A', '1', '2', 'A-1-2', NOW(), NOW()),
    (3, 1, 'A', '1', '3', 'A-1-3', NOW(), NOW()),
    (4, 1, 'A', '1', '4', 'A-1-4', NOW(), NOW()),
    (5, 1, 'A', '1', '5', 'A-1-5', NOW(), NOW()),

    (6, 1, 'A', '2', '1', 'A-2-1', NOW(), NOW()),
    (7, 1, 'A', '2', '2', 'A-2-2', NOW(), NOW()),
    (8, 1, 'A', '2', '3', 'A-2-3', NOW(), NOW()),
    (9, 1, 'A', '2', '4', 'A-2-4', NOW(), NOW()),
    (10, 1, 'A', '2', '5', 'A-2-5', NOW(), NOW()),

    (11, 1, 'B', '1', '1', 'B-1-1', NOW(), NOW()),
    (12, 1, 'B', '1', '2', 'B-1-2', NOW(), NOW()),
    (13, 1, 'B', '1', '3', 'B-1-3', NOW(), NOW()),
    (14, 1, 'B', '1', '4', 'B-1-4', NOW(), NOW()),
    (15, 1, 'B', '1', '5', 'B-1-5', NOW(), NOW());