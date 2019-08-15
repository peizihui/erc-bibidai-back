package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.domain.entity.UserTx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserTxRepository extends JpaRepository<UserTx, Long> {
    @Query(
            "SELECT o FROM UserTx o WHERE o.user=?1 " +
                    " AND o.token.id.account=?2 " +
                    " AND o.token.id.symbol=?3 "
    )
    Page<UserTx> findTxs(User user, String account, String symbol, Pageable pageable);

    @Query(
            "SELECT SUM(o.amount) FROM UserTx o WHERE o.user=?1 " +
                    " AND o.token.id.account=?2 " +
                    " AND o.token.id.symbol=?3 "
    )
    Optional<BigDecimal> findSumAmount(User user, String account, String symbol);
}