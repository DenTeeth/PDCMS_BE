package com.dental.clinic.management.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.authentication.domain.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * Tìm refresh token theo hash
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Xóa token theo hash (logout)
     */
    void deleteByTokenHash(String tokenHash);

    // /**
    // * Xóa tất cả tokens của 1 user (logout all devices)
    // */
    // void deleteByAccountId(String accountId);

    // /**
    // * Xóa token đã hết hạn (cleanup job)
    // */
    // @Modifying
    // @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    // int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
