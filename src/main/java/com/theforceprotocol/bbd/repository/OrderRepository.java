package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.Order;
import com.theforceprotocol.bbd.domain.entity.Token;
import com.theforceprotocol.bbd.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByStatusIn(Collection<Order.OrderStatus> statuses);

    Page<Order> findAllByStatusIn(Collection<Order.OrderStatus> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.initiator=?1 OR o.recipient=?1) AND o.status IN ?2")
    Page<Order> findUserOrders(User user, Collection<Order.OrderStatus> statuses, Pageable pageable);

    List<Order> findByStatusInAndCreatedDateBefore(Collection<Order.OrderStatus> statuses, Instant time);

    List<Order> findByStatusInAndDeadlineBefore(Collection<Order.OrderStatus> statuses, Instant time);

    List<Order> findByStatusIn(Collection<Order.OrderStatus> statuses);

    @Query(
            "SELECT o.borrowedToken AS token,SUM(o.borrowedAmount) AS amount " +
                    " FROM Order o WHERE o.recipient=?1 AND o.status IN ?2 GROUP BY o.borrowedToken"
    )
    List<TokenInfo> findLoanedTokenInfo(User user, Collection<Order.OrderStatus> statuses);

    @Query(
            "SELECT o.borrowedToken AS token,SUM(o.borrowedAmount) AS amount " +
                    " FROM Order o WHERE o.initiator=?1 AND o.status IN ?2 GROUP BY o.borrowedToken"
    )
    List<TokenInfo> findBorrowedTokenInfo(User user, Collection<Order.OrderStatus> statuses);

    List<Order> findByStatusInAndDeadlineBetween(Collection<Order.OrderStatus> statuses, Instant start, Instant end);

    interface TokenInfo {
        Token getToken();

        BigDecimal getAmount();
    }
}

