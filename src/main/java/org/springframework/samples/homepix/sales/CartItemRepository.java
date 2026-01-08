package org.springframework.samples.homepix.sales;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>
{
    List<CartItem> findByUserAndStatus(String user, CartStatus status);

	@Transactional
	@Modifying
	void deleteByUser_UserId(long userId);
}
