package org.springframework.samples.homepix.portfolio.comments;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {

	@Query("SELECT DISTINCT Comment FROM Comment comment WHERE comment.picture_id = :picture_id")
	@Transactional(readOnly = true)
	Collection<Comment> findByPictureId(@Param("picture_id") String picture_id);
}
