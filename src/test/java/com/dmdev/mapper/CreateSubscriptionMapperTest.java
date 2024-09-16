package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        Instant now = Instant.now();

        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(33)
                .name("name")
                .provider("APPLE")
                .expirationDate(
                        now.plus(Duration.ofDays(3))
                )
                .build();

        Subscription actualResult = mapper.map(dto);
        Subscription expectedResult = Subscription.builder()
                .userId(33)
                .name("name")
                .provider(Provider.APPLE)
                .expirationDate(now.plus(Duration.ofDays(3)))
                .status(Status.ACTIVE)
                .build();

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}