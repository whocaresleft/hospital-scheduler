package org.duckdns.whocaresleft.presenter;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class DoctorPresenterTest {

    @Nested
    class ExceptionalCases {
        
        @Test
        void testAllDoctorsShouldLoadAllDoctorsFromRepositoryAndDisplayThemOnView() {
            List<Doctor> doctors = Arrays.asList();
        }
    }
}
