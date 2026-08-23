package org.duckdns.whocaresleft.model;

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
    
    private static final Id DOCTOR_ID = Id.createId("doctor_id");
    private static final Id DEPARTMENT_ID = Id.createId("department_id");
    private static final LocalDate DATE = LocalDate.of(2026, 6, 15);
    private static final LocalTime START_TIME = LocalTime.of(14, 0);
    private static final LocalTime END_TIME = LocalTime.of(15, 0);
    
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_10_00 = LocalTime.of(10, 0);
    private static final LocalTime TIME_11_00 = LocalTime.of(11, 0);
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test @DisplayName("Shift created with valid parameters should have a valid state")
        void testShiftCreatedWithValidParametersShouldHaveValidState() {
            Shift shift = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, START_TIME, END_TIME);
            
            assertThat(shift).isNotNull();
            assertThat(shift.getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(shift.getDepartmentId()).isEqualTo(DEPARTMENT_ID);
            assertThat(shift.getDate()).isEqualTo(DATE);
            assertThat(shift.getStartTime()).isEqualTo(START_TIME);
            assertThat(shift.getEndTime()).isEqualTo(END_TIME);
        }

        @Test @DisplayName("Equals contract using EqualsVerifier")
        void testEqualsContractUsingEqualsVerifier() {
            EqualsVerifier.forClass(Shift.class).verify();
        }
        
        @Test
        void testToString() {
            Id doctorId = Id.createId("doc_id");
            Id departmentId = Id.createId("dep_id");
            LocalDate date = LocalDate.of(2026, 7, 24);
            LocalTime startTime = LocalTime.of(14, 0);
            LocalTime endTime = LocalTime.of(15, 0);
            
            Shift shift = Shift.createShift(doctorId, departmentId, date, startTime, endTime);
            String expected = "(doc_id-dep_id), 2026-07-24: (14:00-15:00)";
            
            assertThat(shift)
                .hasToString(expected);
        }
        
        @Test @DisplayName("Shifts on different dates should not overlap")
        void testShiftsWithDifferentDatesShouldNotOverlap() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            LocalDate anotherDate = LocalDate.of(2026, 6, 16);
            
            Shift first = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, date, START_TIME, END_TIME);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, anotherDate, START_TIME, END_TIME);
            
            assertThat(first.overlaps(second))
                .isFalse();
        }
        
        @Test @DisplayName("Shifts on the same day, where the first ends before the second starts, should not overlap")
        void testShiftsOnSameDateWhereTheFirstEndsBeforeTheSecondStartsShouldNotOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_09_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_10_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isFalse();
        }
        
        @Test @DisplayName("Shifts on the same day, where the first ends when the second starts, should not overlap")
        void testShiftsOnSameDateWhereTheFirstEndsWhenTheSecondStartsShouldNotOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_09_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isFalse();
        }
        
        @Test @DisplayName("Shifts on the same day, where the second ends before the first starts, should not overlap")
        void testShiftsOnSameDateWhereTheFirstStartsAfterTheSecondEndsShouldNotOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_10_00, TIME_11_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_09_00);
            
            assertThat(first.overlaps(second))
                .isFalse();
        }
        
        @Test @DisplayName("Shifts on the same day, where the secon ends when the second starts, should not overlap")
        void testShiftsOnSameDateWhereTheFirstStartsWhenTheSecondEndsShouldNotOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_11_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_09_00);
            
            assertThat(first.overlaps(second))
                .isFalse();
        }
        
        @Test @DisplayName("Shifts on the same day that start on the same time should overlap")
        void testShiftsOnSameDateThatStartAtTheSameTimeShouldOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_09_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isTrue();
        }
        
        @Test @DisplayName("Shifts on the same day that end on the same time should overlap")
        void testShiftsOnSameDateThatEndAtTheSameTimeShouldOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isTrue();
        }
        
        @Test @DisplayName("Shifts on the same day where the first starts before, and ends later, the second, should overlap")
        void testShiftsOnSameDateWhereFirstStartsBeforeAndEndsAfterTheSecondShouldOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_10_00);
            
            assertThat(first.overlaps(second))
                .isTrue();
        }
        
        @Test @DisplayName("Shifts on the same day where the second starts before, and ends later, the first, should overlap")
        void testShiftsOnSameDateWhereSecondStartsBeforeAndEndsAfterTheFirstShouldOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_10_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isTrue();
        }
        
        @Test @DisplayName("Shifts on the same day where the first and second both start and end at the same time should overlap")
        void testShiftsOnSameDateWhereTheyStartAndEndAtTheSameTimesShouldOverlap() {
            Shift first  = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            Shift second = Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_08_00, TIME_11_00);
            
            assertThat(first.overlaps(second))
                .isTrue();
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Null doctor id during creation should result in exception")
        void testNullDoctorIdShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(null, DEPARTMENT_ID, DATE, START_TIME, END_TIME))
                .withMessage("Doctor Id cannot be null");
        }
        
        @Test @DisplayName("Null department id during creation should result in exception")
        void testNullDepartmentIdShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, null, DATE, START_TIME, END_TIME))
                .withMessage("Department Id cannot be null");
        }
        
        @Test @DisplayName("Null date during creation should result in exception")
        void testNullDateShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, null, START_TIME, END_TIME))
                .withMessage("Date cannot be null");
        }
      
        @Test @DisplayName("Null start time during creation should result in exception")
        void testNullStartTimeShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, null, END_TIME))
                .withMessage("Starting time cannot be null");
        }
        
        @Test @DisplayName("Null end time during creation should result in exception")
        void testNullEndTimeShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, START_TIME, null))
                .withMessage("Ending time cannot be null");
        }
        
        @Test @DisplayName("Zero shift duration (starting time = ending time) should result in exception")
        void testZeroDurationShiftShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_10_00, TIME_10_00))
                .withMessage("Shift has zero duration, starting time equals ending time");
        }
        
        @Test @DisplayName("Negative shift duration (starting time > ending time) should result in exception")
        void testNegativeDurationShiftShouldThrow() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Shift.createShift(DOCTOR_ID, DEPARTMENT_ID, DATE, TIME_09_00, TIME_08_00))
                .withMessage("Shift has negative duration, starting time is after than ending time");
        }
    }
}
