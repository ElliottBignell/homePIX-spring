package org.springframework.samples.homepix.sales;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.User;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
	Optional<Order> findFirstByStripeSessionId(String sessionId);
}
