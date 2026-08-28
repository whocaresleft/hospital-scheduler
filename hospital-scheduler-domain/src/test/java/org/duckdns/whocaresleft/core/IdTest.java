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

        @Test
        void testValidIdWithBothLeadingAndTrailingWhitespacesGetTrimmed() {
            
            Id id = Id.createId(" valid_id ");
            assertThat(id.getValue())
                .isEqualTo("valid_id"); 
        }
        
        @Test
        void testValidIdWithNoLeadingOrTrailingWhitespacesRemainsItself() {
            Id id = Id.createId(VALID_ID);
            assertThat(id.getValue())
                .isEqualTo(VALID_ID); 
        }

        @Test
        void testEqualsContractUsingEqualsVerifier() {
            EqualsVerifier.forClass(Id.class).verify();
        }
        
        @Test
        void testIdToStringShouldJustBeTheValue() {
            Id id = Id.createId(VALID_ID);
            
            assertThat(id)
                .hasToString(VALID_ID);
        }
    }
    
    @Nested @DisplayName("ErrorCases")
    class ExceptionCases {
        
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
        
        @ParameterizedTest
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
