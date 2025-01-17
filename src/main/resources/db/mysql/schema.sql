CREATE TABLE types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  forename  VARCHAR(30),
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS albums (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(200),
  picture_count INT(4) UNSIGNED NOT NULL,
  thumbnail_id INT(8) UNSIGNED NOT NULL
);
CREATE INDEX albums_name ON albums (name);

CREATE TABLE keywords (
  id              INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  content         VARCHAR(200),
  keyword_count   INT(4) UNSIGNED NOT NULL
);
CREATE INDEX keywords_content ON keywords (content);

CREATE TABLE picture_file (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  filename            VARCHAR(200),
  title               VARCHAR(200),
  last_modified       DATE,
  path_id             INTEGER NOT NULL,
  width               INTEGER NOT NULL,
  height              INTEGER NOT NULL,
  keywords_id         INTEGER NOT NULL,
  sortkey             INTEGER NOT NULL,
  added_on            DATE,
  taken_on            DATETIME,
  location            INTEGER NOT NULL,
  primary_category    INTEGER NOT NULL,
  secondary_category  INTEGER NOT NULL,
  hits                INTEGER NOT NULL
	cameraModel         VARCHAR(32),
	exposureTime        VARCHAR(8),
	fNumber             VARCHAR(8),
	exposureProgram     VARCHAR(32),
	meteringMode        VARCHAR(32),
	lightSource         VARCHAR(32),
	focalLength         VARCHAR(16)
) ;

ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_albums      FOREIGN KEY (album_id)           REFERENCES   albums (id);
ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_path        FOREIGN KEY (path_id)            REFERENCES    types (id);
ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_keywords    FOREIGN KEY (keywords_id)        REFERENCES keywords (id);
ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_location    FOREIGN KEY (location)           REFERENCES    types (id);
ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_category1   FOREIGN KEY (primary_category)   REFERENCES    types (id);
ALTER TABLE picture_file ADD CONSTRAINT fk_picture_file_category2   FOREIGN KEY (secondary_category) REFERENCES    types (id);
CREATE INDEX picture_file_title ON picture_file (title);

CREATE TABLE albumcontent (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  album_id integer NOT NULL,
  entry_id integer NOT NULL
 );
ALTER TABLE albumcontent ADD CONSTRAINT fk_album_album_id FOREIGN KEY (album_id) REFERENCES albums (id);
ALTER TABLE albumcontent ADD CONSTRAINT fk_album_entry_id FOREIGN KEY (entry_id) REFERENCES picture_file (id);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN
);

CREATE TABLE authorities (
    authority_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
