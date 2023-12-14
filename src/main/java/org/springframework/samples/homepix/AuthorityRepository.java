package org.springframework.samples.homepix;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.Album;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Repository class for <code>Authority</code> domain objects All method names are
 * compliant with Spring Data naming conventions so this interface can easily be extended
 * for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
@Repository
public interface AuthorityRepository extends CrudRepository<Authority, Long> {

	@Query("SELECT DISTINCT authority FROM Authority authority")
	@Transactional(readOnly = true)
	Collection<Authority> findByName(@Param("name") String name);

}
