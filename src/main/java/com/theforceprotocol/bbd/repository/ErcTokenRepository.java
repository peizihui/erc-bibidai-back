package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.ErcToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Mingliang
 */
public interface ErcTokenRepository extends JpaRepository<ErcToken, Integer> {
}
