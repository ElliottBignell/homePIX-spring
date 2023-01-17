INSERT INTO vets VALUES (1, 'James', 'Carter');
INSERT INTO vets VALUES (2, 'Helen', 'Leary');
INSERT INTO vets VALUES (3, 'Linda', 'Douglas');
INSERT INTO vets VALUES (4, 'Rafael', 'Ortega');
INSERT INTO vets VALUES (5, 'Henry', 'Stevens');
INSERT INTO vets VALUES (6, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (1, 'radiology');
INSERT INTO specialties VALUES (2, 'surgery');
INSERT INTO specialties VALUES (3, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (1, 'cat');
INSERT INTO types VALUES (2, 'dog');
INSERT INTO types VALUES (3, 'lizard');
INSERT INTO types VALUES (4, 'snake');
INSERT INTO types VALUES (5, 'bird');
INSERT INTO types VALUES (6, 'hamster');

INSERT INTO albums VALUES (1, 'George', 0, 762);
INSERT INTO albums VALUES (2, 'Betty', 0, 10152);
INSERT INTO albums VALUES (3, 'Eduardo', 0, 7577);
INSERT INTO albums VALUES (4, 'Harold', 0, 9191);

INSERT IGNORE INTO picture_file VALUES (1, 'Leo', '2000-09-07', 1, 1);
INSERT IGNORE INTO picture_file VALUES (2, 'Basil', '2002-08-06', 1, §);
INSERT IGNORE INTO picture_file VALUES (3, 'Rosy', '2001-04-17', 2, 3);
INSERT IGNORE INTO picture_file VALUES (4, 'Jewel', '2000-03-07', 2, 3);
INSERT IGNORE INTO picture_file VALUES (5, 'Iggy', '2000-11-30', 1, 4);
INSERT IGNORE INTO picture_file VALUES (6, 'George', '2000-01-20', 1, 5);
INSERT IGNORE INTO picture_file VALUES (7, 'Samantha', '1995-09-04', 1, 6);
INSERT IGNORE INTO picture_file VALUES (8, 'Max', '1995-09-04', 1, 6);
INSERT IGNORE INTO picture_file VALUES (9, 'Lucky', '1999-08-06', 1, 7);
INSERT IGNORE INTO picture_file VALUES (10, 'Mulligan', '1997-02-24', 2, 8);
INSERT IGNORE INTO picture_file VALUES (11, 'Freddy', '2000-03-09', 1, 9);
INSERT IGNORE INTO picture_file VALUES (12, 'Lucky', '2000-06-24', 2, 10);
INSERT IGNORE INTO picture_file VALUES (13, 'Sly', '2002-06-08', 1, 10);

INSERT INTO visits VALUES (1, 7, '2013-01-01', 'rabies shot');
INSERT INTO visits VALUES (2, 8, '2013-01-02', 'rabies shot');
INSERT INTO visits VALUES (3, 8, '2013-01-03', 'neutered');
INSERT INTO visits VALUES (4, 7, '2013-01-04', 'spayed');
