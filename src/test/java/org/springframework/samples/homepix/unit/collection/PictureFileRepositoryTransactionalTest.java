package org.springframework.samples.homepix.unit.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Transactional
class PictureFileRepositoryTransactionalTest {

    @Autowired
    private PictureFileRepository repository;

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
    void testDatabaseIsEmptyAgain() {
        // This test runs after the first one, but sees an empty database
        assertEquals(0, repository.count());
    }
}
