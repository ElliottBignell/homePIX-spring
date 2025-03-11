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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;

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

	List<PictureFile> findAllById(Iterable<Integer> ids);

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

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE f.name = :folder " +
		"AND kr.entry_id IS NULL " + // Filter for picture_files with no keywords
		"AND pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE f.name = :folder " +
			"AND kr.entry_id IS NULL " + // Filter for picture_files with no keywords
			"AND pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL", // isValid check
		nativeQuery = true)
	List<PictureFile> findByFolderNameAndNoKeywords(
		@Param("folder") String folder,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	@Query("SELECT picture_file FROM PictureFile picture_file WHERE DATE(picture_file.taken_on) = :date")
	@Transactional(readOnly = true)
	List<PictureFile> findByDate(@Param("date") LocalDate date);

	@Query("SELECT DATE(p.taken_on), COUNT(p) FROM PictureFile p GROUP BY DATE(p.taken_on)")
	List<Object[]> countByTakenOn();
	@Query("SELECT DATE(p.taken_on), COUNT(p) FROM PictureFile p WHERE YEAR(p.taken_on) = :year GROUP BY DATE(p.taken_on)")
	List<Object[]> countByTakenOnForYear(@Param("year") int year);


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
		"LEFT JOIN location_relationships kr ON pf.id = kr.picture_id " +
		"LEFT JOIN location l ON kr.location_id = l.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
		"AND LOWER(l.location) = LOWER(:searchText) " +
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"LEFT JOIN location_relationships kr ON pf.id = kr.picture_id " +
			"LEFT JOIN location l ON kr.location_id = l.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
			"AND LOWER(l.location) = LOWER(:searchText)" +
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) ",
		nativeQuery = true)
	Page<PictureFile> findByLocation(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"LEFT JOIN location_relationships kr ON pf.id = kr.picture_id " +
		"LEFT JOIN location k ON kr.location_id = k.id " +
		"WHERE pf.width IS NOT NULL AND pf.height IS NOT NULL " +
		"AND LOWER(k.location) = LOWER(:searchText) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"LEFT JOIN location_relationships kr ON pf.id = kr.picture_id " +
			"LEFT JOIN location k ON kr.location_id = k.id " +
			"WHERE pf.width IS NOT NULL AND pf.height IS NOT NULL " +
			"AND LOWER(k.location) = LOWER(:searchText) " +
			"GROUP BY pf.id",
		nativeQuery = true)
	Page<PictureFile> findByLocationOnly(
		@Param("searchText") String searchText,
		Pageable pageable
	);
	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
		"AND LOWER(k.word) = LOWER(:searchText) " +
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " + // isValid check
			"AND LOWER(k.word) = LOWER(:searchText)" +
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) ",
		nativeQuery = true)
	Page<PictureFile> findByWordInKeyword(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
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
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
		"AND (LOWER(pf.title) = LOWER(:searchText) " +
		"OR f.name = :searchText) " +  // Fixed parentheses
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id " +
		"ORDER BY pf.filename ASC", // Added ORDER BY
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
			"AND (LOWER(pf.title) = LOWER(:searchText) " +
			"OR f.name = :searchText) " +
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%'))",
		nativeQuery = true)
	Page<PictureFile> findByExactWordInTitleOrFolderOrKeywordAndDateRangeAndValidityAndAuthorization(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
		"AND (LOWER(pf.title) REGEXP CONCAT('(^|[^0-9A-Za-zÜüÖöÄäé])', LOWER(:searchText), '([^0-9A-Za-zÜüÖöÄäé]|$)') " +
		"OR f.name = :searchText) " +
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id " +
		"ORDER BY pf.filename ASC",
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"LEFT JOIN keyword k ON kr.keyword_id = k.id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
			"AND (LOWER(pf.title) REGEXP CONCAT('(^|[^0-9A-Za-zÜüÖöÄäé])', LOWER(:searchText), '([^0-9A-Za-zÜüÖöÄäé]|$)') " +
			"OR f.name = :searchText) " +
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%'))",
		nativeQuery = true)
	Page<PictureFile> findByWholeWordInTitleOrFolderOrKeywordAndDateRangeAndValidityAndAuthorization(
		@Param("searchText") String searchText,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);

	@Query(value = "SELECT pf.* FROM picture_file pf " +
		"JOIN folders f ON pf.folder = f.id " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
		"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
		"AND kr.entry_id IS NULL " + // Filter for picture_files with no keywords
		"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%')) " +
		"GROUP BY pf.id " +
		"ORDER BY pf.filename ASC", // Sort results by filename
		countQuery = "SELECT count(DISTINCT pf.id) FROM picture_file pf " +
			"JOIN folders f ON pf.folder = f.id " +
			"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
			"WHERE pf.taken_on BETWEEN :startDate AND :endDate " +
			"AND pf.width IS NOT NULL AND pf.height IS NOT NULL " +
			"AND kr.entry_id IS NULL " + // Filter for picture_files with no keywords
			"AND (:isAdmin = true OR pf.roles LIKE CONCAT('%', :userRoles, '%'))",
		nativeQuery = true)
	Page<PictureFile> findByNoKeywordsAndDateRangeAndValidityAndAuthorization(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDateTime endDate,
		@Param("isAdmin") boolean isAdmin,
		@Param("userRoles") String userRoles,
		Pageable pageable
	);

	@Query(value = "SELECT id, latitude, longitude, " +
		"ST_Distance_Sphere(POINT(latitude, longitude), POINT(?1, ?2)) AS distance " +
		"FROM picture_file " +
		"HAVING distance <= ?3 AND distance > 100", nativeQuery = true)
	List<Object[]> findPositionsWithinRadiusWithoutCrowding(double latitude, double longitude, int radius);

	@Query(value = "SELECT pf.*, " +
		"ST_Distance_Sphere(POINT(latitude, longitude), POINT(?1, ?2)) AS distance " +
		"FROM picture_file AS pf " +
		"HAVING distance <= ?3", nativeQuery = true)
	List<PictureFile> findPicturesWithinRadiusWithoutCrowding(double latitude, double longitude, int radius);

	@Query(value = "SELECT pf.* " +
		"FROM picture_file AS pf " +
		"WHERE ABS(pf.latitude - :latitude) < :tolerance AND ABS(pf.longitude - :longitude) < :tolerance", nativeQuery = true)
	List<PictureFile> findPicturesByLocation(
		@Param("latitude") BigDecimal latitude,
		@Param("longitude") BigDecimal longitude,
		@Param("tolerance") Float tolerance
	);

	@Query(value = "SELECT latitude, longitude, COUNT(*) as count " +
		"FROM picture_file " +
		"WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
		"GROUP BY latitude, longitude " +
		"HAVING COUNT(*) > 1", nativeQuery = true)
	List<Object[]> findClusters();

	@Query(value = "SELECT p1.id AS id1, p2.id AS id2, p1.latitude, p1.longitude " +
		"FROM picture_file p1 " +
		"JOIN picture_file p2 ON p1.id < p2.id " +
		"WHERE p1.latitude IS NOT NULL " +
		"AND p1.longitude IS NOT NULL " +
		"AND p2.latitude IS NOT NULL " +
		"AND p2.longitude IS NOT NULL " +
		"AND ABS(p1.latitude - p2.latitude) < 0.00001 " +
		"AND ABS(p1.longitude - p2.longitude) < 0.00001;", nativeQuery = true)
	List<Object[]> findNearClusters();

	@Query(value = "SELECT pf.id, pf.latitude, pf.longitude,\n" +
		"       ROUND(pf.latitude, 4) AS rounded_latitude,\n" +
		"       ROUND(pf.longitude, 4) AS rounded_longitude\n" +
		"FROM picture_file pf\n" +
		"JOIN (\n" +
		"    SELECT ROUND(latitude, 4) AS rounded_latitude,\n" +
		"           ROUND(longitude, 4) AS rounded_longitude\n" +
		"    FROM picture_file\n" +
		"    WHERE latitude IS NOT NULL AND longitude IS NOT NULL\n" +
		"    GROUP BY ROUND(latitude, 4), ROUND(longitude, 4) \n" +
		"    HAVING COUNT(*) > 1\n" +
		") clusters ON ROUND(pf.latitude, 4) = clusters.rounded_latitude\n" +
		"          AND ROUND(pf.longitude, 4) = clusters.rounded_longitude\n" +
		"WHERE pf.latitude IS NOT NULL AND pf.longitude IS NOT NULL;",
		nativeQuery = true)
	List<Object[]> findAllWithRoundedCoordinates();

	@Query(value = "WITH RECURSIVE word_splitter AS (\n" +
		"	SELECT\n" +
		"		id,\n" +
		"	SUBSTRING_INDEX(title, ' ', 1) AS word,\n" +
		"SUBSTRING(title, LOCATE(' ', title) + 1) AS remaining\n" +
		"FROM picture_file\n" +
		"WHERE title IS NOT NULL AND title != ''\n" +
		"UNION ALL\n" +
		"SELECT\n" +
		"	id,\n" +
		"SUBSTRING_INDEX(remaining, ' ', 1) AS word,\n" +
		"SUBSTRING(remaining, LOCATE(' ', remaining) + 1) AS remaining\n" +
		"FROM word_splitter\n" +
		"WHERE LOCATE(' ', remaining) > 0\n" +
		"UNION ALL\n" +
		"SELECT\n" +
		"	id,\n" +
		"remaining AS word,\n" +
		"	'' AS remaining\n" +
		"FROM word_splitter\n" +
		"WHERE LOCATE(' ', remaining) = 0 AND remaining != ''\n" +
		"	)\n" +
		"SELECT DISTINCT LOWER(TRIM(word)) AS unique_word\n" +
		"FROM word_splitter\n" +
		"WHERE word != ''\n" +
		"ORDER BY unique_word;",
		nativeQuery = true)
	List<String> findAllWordsOld();

	@Query(value = "WITH RECURSIVE word_splitter AS (\n" +
		"    SELECT\n" +
		"       id,\n" +
		"       SUBSTRING_INDEX(REGEXP_REPLACE(title, '[^a-zA-ZÜüÖöÄäé0-9-/ \t\r\n\f]', ''), ' ', 1) AS word,\n" +
		"       SUBSTRING(REGEXP_REPLACE(title, '[^a-zA-ZÜüÖöÄäé0-9-/ \t\n\f]', ''), LOCATE(' ', " +
		"           REGEXP_REPLACE(title, '[^a-zA-ZÜüÖöÄäé0-9-/ \t\n\f]', '')) + 1) " +
		"           AS remaining\n" +
		"    FROM picture_file\n" +
		"    WHERE title IS NOT NULL AND title != ''\n" +
		"    UNION ALL\n" +
		"    SELECT\n" +
		"       id,\n" +
		"       SUBSTRING_INDEX(remaining, ' ', 1) AS word,\n" +
		"       SUBSTRING(remaining, LOCATE(' ', remaining) + 1) AS remaining\n" +
		"    FROM word_splitter\n" +
		"    WHERE LOCATE(' ', remaining) > 0\n" +
		"    UNION ALL\n" +
		"    SELECT\n" +
		"       id,\n" +
		"       remaining AS word,\n" +
		"       '' AS remaining\n" +
		"    FROM word_splitter\n" +
		"    WHERE LOCATE(' ', remaining) = 0 AND remaining != ''\n" +
		")\n" +
		"SELECT DISTINCT LOWER(TRIM(word)) AS unique_word\n" +
		"FROM word_splitter\n" +
		"WHERE word != ''\n" +
		"ORDER BY unique_word;\n",
		nativeQuery = true)
	List<String> findAllWords();

	@Modifying
	@Query(value = "UPDATE picture_file " +
		"SET title = REPLACE(title, :oldWord, :newWord) " +
		"WHERE title LIKE CONCAT('%', :oldWord, '%')",
		nativeQuery = true)
	void replaceWords(
		@Param("oldWord") String oldWord,
		@Param("newWord") String newWord
	);

	@Query(value = "SELECT pf.* " +
		"FROM picture_file pf " +
		"LEFT JOIN keyword_relationships_new kr ON pf.id = kr.entry_id " +
		"WHERE pf.id BETWEEN :start AND :end " +
		"AND kr.entry_id IS NULL",
		nativeQuery = true)
	List<PictureFile> findAllWithNoKeywordsInIdRange(
		@Param("start") Long start,
		@Param("end") Long end
	);

	@Query(value = "SELECT pf.* " +
		"FROM picture_file pf " +
		"WHERE pf.id BETWEEN :start AND :end",
		nativeQuery = true)
	List<PictureFile> findAllIdRange(
		@Param("start") Long start,
		@Param("end") Long end
	);

	default Map<LocalDate, Long> getCountByTakenOn(int year) {

		List<Object[]> result = countByTakenOnForYear(year);

		return result.stream()
			.filter(entry -> entry[0] != null && entry[1] != null)
			.collect(Collectors.toMap(
				entry -> ((Date) entry[0]).toLocalDate(), // Correctly cast to LocalDateTime
				entry -> ((Number) entry[1]).longValue() // Handle any potential type variations for the count
			));
	}


	List<PictureFile> findAll(Specification<PictureFile> spec, Sort sort);
}
