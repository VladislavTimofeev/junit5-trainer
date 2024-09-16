package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        Subscription subscription1 = subscriptionDao.insert(getSubscription(11, "name11"));
        Subscription subscription2 = subscriptionDao.insert(getSubscription(22, "name22"));
        Subscription subscription3 = subscriptionDao.insert(getSubscription(33, "name33"));

        List<Subscription> actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        List<Integer> subscriptionIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();
        assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());
    }

    @Test
    void findById() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1, "name"));

        Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult).contains(subscription);
    }

    @Test
    void shouldNotFindById() {
        subscriptionDao.insert(getSubscription(1, "name"));

        Optional<Subscription> actualResult = subscriptionDao.findById(9999);

        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteExistingSubscription() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1, "name"));

        boolean actualResult = subscriptionDao.delete(subscription.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteNotExistingSubscription() {
        subscriptionDao.insert(getSubscription(1, "name"));

        boolean actualResult = subscriptionDao.delete(9999);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        Subscription subscription = getSubscription(1, "name");
        subscriptionDao.insert(subscription);
        subscription.setName("mamamamamaam");
        subscription.setProvider(Provider.GOOGLE);

        subscriptionDao.update(subscription);

        Subscription updatedSubscription = subscriptionDao.findById(subscription.getId()).get();
        assertThat(updatedSubscription).isEqualTo(subscription);
    }

    @Test
    void insert() {
        Subscription subscription = getSubscription(1, "name");

        Subscription currentSubscription = subscriptionDao.insert(subscription);

        assertNotNull(currentSubscription.getId());
    }

    @Test
    void findByUserId() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1, "name"));

        List<Subscription> currentValue = subscriptionDao.findByUserId(subscription.getUserId());

        assertThat(currentValue).hasSize(1);
        assertThat(currentValue.get(0).getUserId()).isEqualTo(subscription.getUserId());
    }

    @Test
    void shouldNotFindByUserId() {
        subscriptionDao.insert(getSubscription(1, "name"));

        List<Subscription> currentValue = subscriptionDao.findByUserId(9999);

        assertThat(currentValue).isEmpty();
    }

    private Subscription getSubscription(Integer userId, String name) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate(now.minus(Duration.ofDays(1)))
                .status(Status.ACTIVE)
                .build();
    }
}