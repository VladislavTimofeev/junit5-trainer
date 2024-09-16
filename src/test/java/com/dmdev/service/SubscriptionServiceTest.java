package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void shouldThrowValidationExceptionWhenValidationFails() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider("INVALID_PROVIDER")
                .expirationDate(Instant.now().plus(Duration.ofDays(7)))
                .build();

        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(101, "name is invalid"));
        validationResult.add(Error.of(102, "provider is invalid"));

        doReturn(validationResult).when(createSubscriptionValidator).validate(dto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(dto));
    }


    @Test
    void shouldCreateNewSubscriptionWhenNotExists() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("New Subscription")
                .provider("GOOGLE")
                .expirationDate(Instant.now())
                .build();
        ValidationResult validationResult = new ValidationResult();
        when(createSubscriptionValidator.validate(dto)).thenReturn(validationResult);

        doReturn(Collections.emptyList()).when(subscriptionDao).findByUserId(dto.getUserId());

        Subscription updateSubscription = new Subscription();
        when(createSubscriptionMapper.map(dto)).thenReturn(updateSubscription);
        doReturn(updateSubscription).when(subscriptionDao).upsert(any(Subscription.class));

        Subscription actualResult = subscriptionService.upsert(dto);

        assertNotNull(actualResult);
    }

    @Test
    void shouldFailCancelSubscriptionWithNotFoundId() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.ACTIVE)
                .build();
        doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(subscription.getId()));
    }

    @Test
    void shouldFailCancelSubscriptionWithWrongStatus() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.CANCELED)
                .build();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(subscription.getId()));
        assertThat(exception.getMessage()).isEqualTo(String.format("Only active subscription %d can be canceled", subscription.getId()));
    }


    @Test
    void shouldSuccessfullyCancelActiveSubscription() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.ACTIVE)
                .build();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        subscriptionService.cancel(subscription.getId());

        assertThat(subscription.getStatus()).isEqualTo(Status.CANCELED);
        verify(subscriptionDao).update(subscription);
    }

    @Test
    void shouldFailExpireSubscriptionWithNotFoundById() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.EXPIRED)
                .build();

        doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(subscription.getId()));
    }

    @Test
    void shouldFailExpireSubscriptionWithExistedStatus() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.EXPIRED)
                .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(subscription.getId()));
        assertThat(exception.getMessage()).isEqualTo(String.format("Subscription %d has already expired", subscription.getId()));
    }

    @Test
    void shouldSuccessExpireSubscription() {
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(11)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().minus(Duration.ofDays(3)))
                .status(Status.ACTIVE)
                .build();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());
        subscriptionService.expire(subscription.getId());

        assertThat(subscription.getStatus()).isEqualTo(Status.EXPIRED);
        verify(subscriptionDao).update(subscription);
    }
}