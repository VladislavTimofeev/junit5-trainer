package com.dmdev.entity;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProviderTest {

    @Test
    void findByName() {
        assertEquals(Provider.GOOGLE, Provider.findByName("GOOGLE"));
        assertEquals(Provider.APPLE, Provider.findByName("APPLE"));
    }

    @Test
    void findByNameOpt() {
        Optional<Provider> google = Provider.findByNameOpt("GOOGLE");
        assertTrue(google.isPresent());
        assertEquals(Provider.GOOGLE, google.get());

        Optional<Provider> apple = Provider.findByNameOpt("APPLE");
        assertTrue(apple.isPresent());
        assertEquals(Provider.APPLE, apple.get());
    }
}