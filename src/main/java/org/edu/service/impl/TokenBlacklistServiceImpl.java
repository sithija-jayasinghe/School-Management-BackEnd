package org.edu.service.impl;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.edu.entity.BlacklistedToken;
import org.edu.repository.BlacklistedTokenRepository;
import org.edu.service.TokenBlacklistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    public void blacklist(String token, Instant expiresAt) {
        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiresAt(expiresAt);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
