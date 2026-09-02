package org.duckdns.whocaresleft.view.swing;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("UI tests for SwingHospitalFrame")
class SwingHospitalFrameTest {
    
    private static final int TIMEOUT = 15;
    
    @Mock
    private DoctorPresenter doctorPresenter;
    @Mock
    private DepartmentPresenter departmentPresenter;
    @Mock
    private ShiftPresenter shiftPresenter;
    
    private AutoCloseable closeable;
    
    private SwingHospitalFrame frame;
    private FrameFixture window;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        GuiActionRunner.execute(() -> {
            frame = new SwingHospitalFrame();
            frame.setDoctorPresenter(doctorPresenter);
            frame.setDepartmentPresenter(departmentPresenter);
            frame.setShiftPresenter(shiftPresenter);
            return frame;
        });
        window = new FrameFixture(BasicRobot.robotWithCurrentAwtHierarchy(), frame);
        window.show();
    }
    
    @AfterEach
    void teardown() throws Exception {
        closeable.close();
        if (window != null)
            window.cleanUp();
    }
    
    @Test @GUITest
    void testInitialSetup() {
        window.label("hospitalLabel");
        
        window.tabbedPane().requireTabTitles("Doctors", "Departments", "Shifts");
        
        window.tabbedPane().selectTab("Doctors");
        window.panel("doctorView").requireVisible();
        
        window.tabbedPane().selectTab("Departments");
        window.panel("departmentView").requireVisible();
        
        window.tabbedPane().selectTab("Shifts");
        window.panel("shiftView").requireVisible();
    }
    
    @Test @GUITest
    void testWhenStartingTheApplicationInDoctorViewThenAllDoctorsShouldCalledInDoctorPresenter() {
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(doctorPresenter).allDoctors());
    }
    
    @Test @GUITest
    void testWhenChangingToDepartmentTabThenAllDepartmentsShouldCalledInDepartmentPresenter() {
        window.tabbedPane().selectTab("Departments");
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(departmentPresenter).allDepartments());
    }
    
    @Test @GUITest
    void testWhenChangingToShiftTabThenAllShiftsShouldCalledInShiftPresenter() {
        window.tabbedPane().selectTab("Shifts");
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(shiftPresenter).allShifts());
    }
    
    @Test @GUITest
    void testWhenChangingToATabAndBackToDoctorTabThenAllDoctorsShouldBeCalledInDoctorPresenter() {
        window.tabbedPane().selectTab("Shifts");
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(shiftPresenter).allShifts());
        
        clearInvocations(doctorPresenter);
        window.tabbedPane().selectTab("Doctors");
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(doctorPresenter).allDoctors());
    }
}
