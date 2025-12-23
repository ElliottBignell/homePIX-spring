package org.springframework.samples.homepix.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemDownloadRepository  extends JpaRepository<CartItemDownload, Long> {

	@Query("SELECT cart_item_download AS year FROM CartItemDownload cart_item_download WHERE cart_item_download.username = :username ")
	@Transactional(readOnly = true)
	List<CartItemDownload> findByUsername(@Param("username") String username);

	@Query("SELECT cart_item_download AS year FROM CartItemDownload cart_item_download WHERE cart_item_download.username = :username AND cart_item_download.filename = :filename")
	@Transactional(readOnly = true)
	List<CartItemDownload> findByUsernameAndFliename(@Param("username") String username, @Param("filename") String filename);
}
