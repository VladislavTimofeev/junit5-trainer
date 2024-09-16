package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(33)
                .name("name")
                .provider("APPLE")
                .expirationDate(
                        Instant.now().plus(Duration.ofDays(3))
                )
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertFalse(actualResult.hasErrors());
    }

    @Test
    void shouldFailInvalidUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("name")
                .provider("APPLE")
                .expirationDate(
                        Instant.now().plus(Duration.ofDays(3))
                )
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void shouldFailInvalidName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("")
                .provider("APPLE")
                .expirationDate(
                        Instant.now().plus(Duration.ofDays(3))
                )
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);
    }

    @Test
    void shouldFailInvalidProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("name")
                .provider("AMAZON")
                .expirationDate(
                        Instant.now().plus(Duration.ofDays(3))
                )
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @Test
    void shouldFailInvalidExpirationDateWithNull() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("name")
                .provider("APPLE")
                .expirationDate(null)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void shouldFailInvalidExpirationDateWithWrongTime() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("name")
                .provider("APPLE")
                .expirationDate(Instant.now().minus(Duration.ofDays(1)))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void shouldFailInvalidNameAndProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("")
                .provider("AMAZON")
                .expirationDate(Instant.now().plus(Duration.ofDays(3)))
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(2);
        List<Integer> errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();

        assertThat(errorCodes).contains(101, 102);
    }
}