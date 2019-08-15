package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.Token;
import com.theforceprotocol.bbd.domain.entity.TokenId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, TokenId> {
    Optional<Token> findByIdAndEnabledTrue(TokenId id);

    List<Token> findAllByEnabledTrue();
}
