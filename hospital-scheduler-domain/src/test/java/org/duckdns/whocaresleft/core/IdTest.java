package org.duckdns.whocaresleft.core;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdTest {

    @Nested
    class HappyCases {
        
        @Test
        void testValidIdWithBothLeadingAndTrailingWhitespacesGetTrimmed() {
            Id id = Id.createId(" valid_id ");
            assertThat(id.getValue())
                .isEqualTo("valid_id"); 
        }
        
        @Test
        void testValidIdWithOnlyCorrectCharactersRemainsItself() {
            Id id = Id.createId("valid_id");
            assertThat(id.getValue())
                .isEqualTo("valid_id"); 
        }
        
        @Test
        void testIdIsEqualToItself() {
            Id id = Id.createId("valid_id");
            assertThat(id)
                .isEqualTo(id);
        }
        
        @Test
        void testIdsWithSameValueAreEqual() {
            Id id1 = Id.createId("valid_id");
            Id id2 = Id.createId("valid_id");
            
            assertThat(id1)
                .isEqualTo(id2);
        }
        
        @Test
        void testIdsWithDifferentValuesAreNotEqual() {
            Id id1 = Id.createId("valid_id");
            Id id2 = Id.createId("another_valid_id");
            
            assertThat(id1)
                .isNotEqualTo(id2); 
        }
        
        @Test
        void testValidIdIsNotEqualToNull() {
            Id id1 = Id.createId("valid_id");
            
            assertThat(id1)
                .isNotEqualTo(null); 
        }
        
        @Test
        void testValidIdIsNotEqualToObjectOfDifferentClass() {
            Id id1 = Id.createId("valid_id");
            
            assertThat(id1)
                .isNotEqualTo("valid_id"); 
        }
        
        @Test
        void testEqualIdsShouldHaveSameHashCode() {
            Id id1 = Id.createId("valid_id");
            Id id2 = Id.createId("valid_id");
            
            assertThat(id1)
                .isEqualTo(id2);
            assertThat(id1.hashCode())
                .isEqualTo(id2.hashCode());
        }
        
        @Test
        void testIdToStringShouldJustBeTheValue() {
            Id id = Id.createId("valid_id");
            
            assertThat(id.toString())
                .isEqualTo("valid_id");
        }
    }
    
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
