package com.dental.clinic.management.authentication.dto.response;

/**
 * Response containing a new access token generated from a valid refresh token.
 */
public class RefreshTokenResponse {
    private String accessToken;
    private long accessTokenExpiresAt; // epoch seconds
    private String refreshToken; // May be rotated
    private Long refreshTokenExpiresAt; // nullable if not rotated

    public RefreshTokenResponse() {
    }

    public RefreshTokenResponse(String accessToken, long accessTokenExpiresAt, String refreshToken,
            Long refreshTokenExpiresAt) {
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(long accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(Long refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}
