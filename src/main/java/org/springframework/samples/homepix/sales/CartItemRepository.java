package org.springframework.samples.homepix.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>
{
    List<CartItem> findByUserAndStatus(User user, CartStatus status);
}
