package org.duckdns.whocaresleft.core;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nl.jqno.equalsverifier.EqualsVerifier;

@DisplayName("Unit tests for Id")
class IdTest {

    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        private static final String VALID_ID = "valid_id";

        @Test @DisplayName("Valid Id value with leading and trailing spaces gets trimmed")
        void testValidIdWithBothLeadingAndTrailingWhitespacesGetTrimmed() {
            
            Id id = Id.createId(" valid_id ");
            assertThat(id.getValue())
                .isEqualTo("valid_id"); 
        }
        
        @Test @DisplayName("Valid Id value with no leading or trailing spaces remains itself")
        void testValidIdWithNoLeadingOrTrailingWhitespacesRemainsItself() {
            Id id = Id.createId(VALID_ID);
            assertThat(id.getValue())
                .isEqualTo(VALID_ID); 
        }

        @Test @DisplayName("Equals contract using EqualsVerifier")
        void testEqualsContractUsingEqualsVerifier() {
            EqualsVerifier.forClass(Id.class).verify();
        }
        
        @Test @DisplayName("The string representation of Id is just its value")
        void testIdToStringShouldJustBeTheValue() {
            Id id = Id.createId(VALID_ID);
            
            assertThat(id)
                .hasToString(VALID_ID);
        }
    }
    
    @Nested @DisplayName("ErrorCases")
    class ExceptionCases {
        
        @Test @DisplayName("Creating an Id with null value should result in an exception")
        void testNullValueShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(null))
                .withMessage("Id value cannot be null");
        }
        
        @Test @DisplayName("Creating an Id with empty string should result in an exception")
        void testEmptyStringValueShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(""))
                .withMessage("Id value cannot be empty");
        }
        
        @ParameterizedTest @DisplayName("Creating an Id with a string consisting only of whitespaces should result in an exception")
        @ValueSource(strings = {
            " ", "\t", "   ", "\t\t\t", "\t   \t  \t "
        })
        void testValueWithOnlyWhitespacesShouldThrow(String value) {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(value))
                .withMessage("Id value cannot be empty");
        }
        
        @ParameterizedTest @DisplayName("Creating an Id with a non-permitted character should result in an exception")
        @ValueSource(strings = {
            "id.", "id?", "i d", "id#", "id@"
        })
        void testValueWithInvalidCharactersShouldThrow(String value) {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Id.createId(value))
                .withMessage("Id value contains invalid characters");
        }
    }
}
