package com.example.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();
    @Test void encodesBase62Boundaries() {
        assertEquals("0", encoder.encode(0));
        assertEquals("z", encoder.encode(61));
        assertEquals("10", encoder.encode(62));
        assertEquals("100", encoder.encode(3844));
    }
    @Test void rejectsNegativeValues() { assertThrows(IllegalArgumentException.class, () -> encoder.encode(-1)); }
}
