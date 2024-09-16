package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionServiceIT extends IntegrationTestBase {

    private Clock clock;
    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock
        );
    }

    @Test
    void cancel() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.ACTIVE)
                .build();

        Subscription actualResult = subscriptionDao.insert(subscription);
        subscriptionService.cancel(actualResult.getId());
        Subscription updatedSubscription = subscriptionDao.findById(actualResult.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        assertThat(updatedSubscription.getStatus()).isEqualTo(Status.CANCELED);
    }
}
















