package org.springframework.samples.homepix.integration.collection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class PictureFileRepositoryTransactionalIT {

    private final PictureFileRepository repository;

	@Autowired
	PictureFileRepositoryTransactionalIT(PictureFileRepository repository) {
		this.repository = repository;
	}

	@Test
	void testTransactionalRollback() {
		// Database starts empty (assuming H2 or test DB)

		// Save a new PictureFile
		PictureFile pic = new PictureFile();
		pic.setFilename("test.jpg");
		pic.setTitle("Test Picture");

		repository.save(pic);

		// Verify it's persisted
		assertEquals(1, repository.count());
	}

    @Test
    void testTransactionalMultiplePicture() {
        // Database starts empty (assuming H2 or test DB)

		// Save a new PictureFile
		PictureFile pic = new PictureFile();
		pic.setFilename("test.jpg");
		pic.setTitle("Test Picture");

		repository.save(pic);

		// Save a new PictureFile
        pic = new PictureFile();
        pic.setFilename("test2.jpg");
        pic.setTitle("Test Picture 2");

        repository.save(pic);

        // Verify it's persisted
        assertEquals(2, repository.count());
    }

    @Test
    void testDatabaseIsEmptyAgain() {
        // This test runs after the first one, but sees an empty database
        assertEquals(0, repository.count());
    }
}
