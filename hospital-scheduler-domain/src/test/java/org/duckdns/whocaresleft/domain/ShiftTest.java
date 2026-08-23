package org.duckdns.whocaresleft.domain;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.duckdns.whocaresleft.core.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@DisplayName("Unit tests for Shift")
class ShiftTest {
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Null worker id during creation should result in exception")
        void testNullWorkerIdShouldThrow() {
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime startTime = LocalTime.of(14, 0);
            LocalTime endTime = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(null, departmentId, date, startTime, endTime))
                .withMessage("Worker Id cannot be null");
        }
        
        @Test @DisplayName("Null department id during creation should result in exception")
        void testNullDepartmentIdShouldThrow() {
            Id workerId = Id.createId("worker_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime startTime = LocalTime.of(14, 0);
            LocalTime endTime = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, null, date, startTime, endTime))
                .withMessage("Department Id cannot be null");
        }
        
        @Test @DisplayName("Null date during creation should result in exception")
        void testNullDateShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalTime startTime = LocalTime.of(14, 0);
            LocalTime endTime = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, null, startTime, endTime))
                .withMessage("Date cannot be null");
        }
      
        @Test @DisplayName("Null start time during creation should result in exception")
        void testNullStartTimeShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime endTime = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, null, endTime))
                .withMessage("Starting time cannot be null");
        }
        
        @Test @DisplayName("Null end time during creation should result in exception")
        void testNullEndTimeShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime startTime = LocalTime.of(14, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, startTime, null))
                .withMessage("Ending time cannot be null");
        }
        
        @Test @DisplayName("Zero shift duration (starting time = ending time) should result in exception")
        void testZeroDurationShiftShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime startTime = LocalTime.of(14, 00);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, startTime, startTime))
                .withMessage("Shift has zero duration, starting time equals ending time");
        }
        
        @Test @DisplayName("Negative shift duration (starting time > ending time) should result in exception")
        void testNegativeDurationShiftShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime startTime = LocalTime.of(14, 00);
            LocalTime endTime = LocalTime.of(13, 30);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, startTime, endTime))
                .withMessage("Shift has negative duration, starting time is after than ending time");
        }
    }
}
