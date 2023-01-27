CREATE TABLE coding_failed
(
    id uuid NOT NULL PRIMARY KEY,
    system VARCHAR NOT NULL,
    code VARCHAR NOT NULL,
    display VARCHAR NULL,
    occurrence INTEGER NOT NULL,
    concept_id INTEGER NULL,
    valid_start TIMESTAMP NULL,
    valid_end TIMESTAMP NULL
);