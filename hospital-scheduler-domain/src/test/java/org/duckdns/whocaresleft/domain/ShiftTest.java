package org.duckdns.whocaresleft.domain;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.duckdns.whocaresleft.core.Id;
import java.time.LocalDate;
import java.time.LocalTime;

import nl.jqno.equalsverifier.EqualsVerifier;

@DisplayName("Unit tests for Shift")
class ShiftTest {
    
    private static final Id WORKER_ID = Id.createId("worker_id");
    private static final Id DEPARTMENT_ID = Id.createId("department_id");
    private static final LocalDate DATE = LocalDate.of(2026, 6, 15);
    private static final LocalTime START_TIME = LocalTime.of(14, 0);
    private static final LocalTime END_TIME = LocalTime.of(15, 0);
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test @DisplayName("Shift created with valid parameters should have a valid state")
        void testShiftCreatedWithValidParametersShouldHaveValidState() {
            Shift shift = Shift.createShift(WORKER_ID, DEPARTMENT_ID, DATE, START_TIME, END_TIME);
            
            assertThat(shift).isNotNull();
            assertThat(shift.getWorkerId()).isEqualTo(WORKER_ID);
            assertThat(shift.getDepartmentId()).isEqualTo(DEPARTMENT_ID);
            assertThat(shift.getDate()).isEqualTo(DATE);
            assertThat(shift.getStartTime()).isEqualTo(START_TIME);
            assertThat(shift.getEndTime()).isEqualTo(END_TIME);
        }

        @Test
        void testEqualsContractUsingEqualsVerifier() {
            EqualsVerifier.forClass(Shift.class).verify();
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Null worker id during creation should result in exception")
        void testNullWorkerIdShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(null, DEPARTMENT_ID, DATE, START_TIME, END_TIME))
                .withMessage("Worker Id cannot be null");
        }
        
        @Test @DisplayName("Null department id during creation should result in exception")
        void testNullDepartmentIdShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, null, DATE, START_TIME, END_TIME))
                .withMessage("Department Id cannot be null");
        }
        
        @Test @DisplayName("Null date during creation should result in exception")
        void testNullDateShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, DEPARTMENT_ID, null, START_TIME, END_TIME))
                .withMessage("Date cannot be null");
        }
      
        @Test @DisplayName("Null start time during creation should result in exception")
        void testNullStartTimeShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, DEPARTMENT_ID, DATE, null, END_TIME))
                .withMessage("Starting time cannot be null");
        }
        
        @Test @DisplayName("Null end time during creation should result in exception")
        void testNullEndTimeShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, DEPARTMENT_ID, DATE, START_TIME, null))
                .withMessage("Ending time cannot be null");
        }
        
        @Test @DisplayName("Zero shift duration (starting time = ending time) should result in exception")
        void testZeroDurationShiftShouldThrow() {
            LocalTime time = LocalTime.of(14, 00);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, DEPARTMENT_ID, DATE, time, time))
                .withMessage("Shift has zero duration, starting time equals ending time");
        }
        
        @Test @DisplayName("Negative shift duration (starting time > ending time) should result in exception")
        void testNegativeDurationShiftShouldThrow() {
            LocalTime beforeTime = LocalTime.of(13, 30);
            LocalTime afterTime = LocalTime.of(14, 00);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(WORKER_ID, DEPARTMENT_ID, DATE, afterTime, beforeTime))
                .withMessage("Shift has negative duration, starting time is after than ending time");
        }
    }
}
