/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.homepix.portfolio.collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>PictureFile</code> domain objects All method names are
 * compliant with Spring Data naming conventions so this interface can easily be extended
 * for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Elliott Bignell
 */
@Repository
public interface PictureFileRepository extends CrudRepository<PictureFile, Integer> {

	/**
	 * Retrieve all {@link PictureFileType}s from the data store.
	 * @return a PictureCollection of {@link PictureFileType}s.
	 */
	@Query("SELECT ptype FROM PictureFileType ptype ORDER BY ptype.name")
	@Transactional(readOnly = true)
	List<PictureFileType> findPictureFileTypes();

	@Query("SELECT DISTINCT YEAR(taken_on) AS year FROM PictureFile picture_file ORDER BY year")
	@Transactional(readOnly = true)
	List<String> findYears();

	@Query("SELECT picture_file FROM PictureFile picture_file WHERE picture_file.filename=:filename")
	@Transactional(readOnly = true)
	List<PictureFile> findByFilename(@Param("filename") String filename);

	@Query("SELECT pf.filename FROM PictureFile pf WHERE pf.filename IN :filenames")
	@Transactional(readOnly = true)
	List<String> findFilenamesByFilenames(@Param("filenames") List<String> filenames);

	@Query("SELECT pf FROM PictureFile pf WHERE pf.filename IN :filenames")
	List<PictureFile> findByFilenames(@Param("filenames") List<String> filenames);

	@Query("SELECT pf FROM PictureFile pf JOIN pf.folder f WHERE f.name = :filename")
	@Transactional(readOnly = true)
	List<PictureFile> findByFolderName(@Param("filename") String filename);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE f.name = :folder " +
		"AND pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
		"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(k.word) = LOWER(:searchText)) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE f.name = :folder " +
			"AND pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
			"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(k.word) = LOWER(:searchText))",
		nativeQuery = true)
	List<PictureFile> findByFolderName(
		@Param("folder") String folder,
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	@Query("SELECT picture_file FROM PictureFile picture_file WHERE DATE(picture_file.taken_on) = :date")
	@Transactional(readOnly = true)
	List<PictureFile> findByDate(@Param("date") LocalDate date);

	@Query("SELECT p.taken_on, COUNT(p) FROM PictureFile p GROUP BY p.taken_on")
	List<Object[]> countByTakenOn();

	@Query("SELECT p FROM PictureFile p WHERE p.filename LIKE %:searchText% AND p.taken_on BETWEEN :startDate AND :endDate")
	Page<PictureFile> findByFilenameContainingAndTakenOnBetween(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable
	);

	@Query(value = "SELECT pf.* FROM picture_file pf JOIN folders f ON pf.folder = f.id WHERE pf.title LIKE CONCAT('% ', :searchText, ' %') OR f.name LIKE CONCAT('% ', :searchText, ' %')\n",
		countQuery = "SELECT count(*) FROM picture_file pf JOIN folders f ON pf.folder = f.id WHERE pf.title LIKE CONCAT('% ', :searchText, ' %') OR f.name LIKE CONCAT('% ', :searchText, ' %')\n",
		nativeQuery = true)
	List<PictureFile> findByWordInTitleOrFolder( @Param("searchText") String searchText );

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
		"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(k.word) = LOWER(:searchText)) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
			"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(k.word) = LOWER(:searchText))",
		nativeQuery = true)
	List<PictureFile> findByWordInTitleOrFolderOrKeyword(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
		"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
		"OR LOWER(k.word) = LOWER(:searchText)) " +
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
			"AND (LOWER(pf.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
			"OR LOWER(k.word) = LOWER(:searchText)) " +
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%'))",
		nativeQuery = true)
	Page<PictureFile> findByWordInTitleOrFolderOrKeywordAndDateRangeAndValidityAndAuthorization(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);


	default Map<LocalDateTime, Long> getCountByTakenOn() {
		List<Object[]> result = countByTakenOn();

		return result.stream()
			.collect(Collectors.toMap(
				entry -> (LocalDateTime) entry[0],
				entry -> (Long) entry[1]
			));
	}

	List<PictureFile> findAll(Specification<PictureFile> spec, Sort sort);
}
