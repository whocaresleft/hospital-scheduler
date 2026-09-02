package org.duckdns.whocaresleft.view.swing;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UI tests for SwingHospitalFrame")
class SwingHospitalFrameTest {

    private SwingHospitalFrame frame;
    private FrameFixture window;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        GuiActionRunner.execute(() -> {
            frame = new SwingHospitalFrame();
            return frame;
        });
        window = new FrameFixture(BasicRobot.robotWithCurrentAwtHierarchy(), frame);
        window.show();
    }
    
    @AfterEach
    void teardown(){
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
}
