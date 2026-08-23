package org.duckdns.whocaresleft.presenter;

import static java.util.Arrays.asList;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.view.DoctorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class DoctorPresenterTest {

    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private DoctorView doctorView;
    
    @InjectMocks
    private DoctorController doctorController;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }
    
    @Nested
    class ExceptionalCases {

        @Test
        void testAllDoctorsShouldLoadAllDoctorsFromRepositoryAndDisplayThemOnView() {
            List<Doctor> doctors = asList(Doctor.createDoctor(
                Id.createId("doctor_1"), "doc", "tor"));
            
            when(doctorRepository);
            
            
            doctorController.allDoctors();
            verify(doctorView)
                .showAllDoctors(doctors);
        }
    }
}
