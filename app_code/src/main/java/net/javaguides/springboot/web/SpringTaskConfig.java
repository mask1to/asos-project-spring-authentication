package net.javaguides.springboot.web;

import net.javaguides.springboot.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Date;

@Configuration
@EnableScheduling
public class SpringTaskConfig {
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Scheduled(cron = "${purge.cron.expression}")
    public void purgeExpired() {
        Date now = Date.from(Instant.now());
        tokenRepository.deleteByExpiryDateLessThan(now);
    }
}