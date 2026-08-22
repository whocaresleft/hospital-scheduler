package org.duckdns.whocaresleft.core;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdTest {

    @Nested
    class ErrorCases {
        
        @Test
        void testNullValueShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(null))
                .withMessage("Id value cannot be null");
        }
        
        @Test
        void testEmptyStringValueShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(""))
                .withMessage("Id value cannot be empty");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            " ", "\t", "   ", "\t\t\t", "\t   \t  \t "
        })
        void testValueWithOnlyWhitespacesShouldThrow(String value) {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(value))
                .withMessage("Id value cannot be empty");
        }
    }
}
