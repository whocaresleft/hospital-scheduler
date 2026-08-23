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
        
        @Test
        void testNullWorkerIdShouldThrow() {
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime start = LocalTime.of(14, 0);
            LocalTime end = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(null, departmentId, date, start, end))
                .withMessage("Worker Id cannot be null");
        }
        
        @Test
        void testNullDepartmentIdShouldThrow() {
            Id workerId = Id.createId("worker_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime start = LocalTime.of(14, 0);
            LocalTime end = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, null, date, start, end))
                .withMessage("Department Id cannot be null");
        }
        
        @Test
        void testNullDateShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalTime start = LocalTime.of(14, 0);
            LocalTime end = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, null, start, end))
                .withMessage("Date cannot be null");
        }
      
        @Test
        void testNullStartTimeShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime end = LocalTime.of(15, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, null, end))
                .withMessage("Starting time cannot be null");
        }
        
        @Test
        void testNullEndTimeShouldThrow() {
            Id workerId = Id.createId("worker_id");
            Id departmentId = Id.createId("department_id");
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalTime start = LocalTime.of(14, 0);
            
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(workerId, departmentId, date, start, null))
                .withMessage("Ending time cannot be null");
        }
    }
}
