package org.duckdns.whocaresleft.view.swing;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.TypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("UI tests for SwingShiftDepartment")
class SwingShiftViewTest {
    
    private static final int TIMEOUT = 15;
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Mock
    private ShiftPresenter presenter;
    private AutoCloseable closeable;

    private SwingShiftView view;
    private FrameFixture window;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        GuiActionRunner.execute(() -> {
            view = new SwingShiftView();
            view.setPresenter(presenter);
            return view;
        });
        window = Containers.showInFrame(view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        closeable.close();
        if (window != null)
            window.cleanUp();
    }
    
    @Test @GUITest
    void testInitialSetupDisabledUI() {
        window.label("shiftCreation");
        
        window.label("doctorIdLabel");
        window.textBox("doctorIdTextBox").requireEnabled().requireNotEditable();
        window.label("departmentIdLabel");
        window.textBox("departmentIdTextBox").requireEnabled().requireNotEditable();
        window.label("dateLabel");
        window.textBox("dateTextBox").requireEnabled().requireNotEditable();
        window.button("dateButton").requireDisabled();
        window.label("startTimeLabel");
        window.textBox("startTimeTextBox").requireEnabled().requireNotEditable();
        window.button("startTimeButton").requireDisabled();
        window.label("endTimeLabel");
        window.textBox("endTimeTextBox").requireEnabled().requireNotEditable();
        window.button("endTimeButton").requireDisabled();
        
        window.button("addButton").requireDisabled();
        
        window.list("shiftList").requireDisabled();
        
        window.label("selectedShiftLabel");
        window.checkBox("editShift").requireDisabled();
        
        window.label("selectedDoctorIdLabel");
        window.textBox("selectedDoctorIdTextBox").requireEnabled().requireNotEditable();
        window.label("selectedDepartmentIdLabel");
        window.textBox("selectedDepartmentIdTextBox").requireEnabled().requireNotEditable();
        window.label("selectedDateLabel");
        window.textBox("selectedDateTextBox").requireEnabled().requireNotEditable();
        window.button("selectedDateButton").requireDisabled();
        window.label("selectedStartTimeLabel");
        window.textBox("selectedStartTimeTextBox").requireEnabled().requireNotEditable();
        window.button("selectedStartTimeButton").requireDisabled();
        window.label("selectedEndTimeLabel");
        window.textBox("selectedEndTimeTextBox").requireEnabled().requireNotEditable();
        window.button("selectedEndTimeButton").requireDisabled();
        
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
        
        window.label("infoLabel").requireText(" ");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testWhenAllInputFieldsInSectionShiftCreationAreNotEmptyThenAddButtonShouldBeEnabled() {
        GuiActionRunner.execute(() -> view.enableUI());
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("addButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenDateIsNotValidThenAddButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> view.enableUI());
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.textBox("dateTextBox").enterText("xxx");
        window.button("addButton").requireDisabled();
    }
    
    @Test @GUITest
    void testEitherWhenStartOrEndTimeAreNotValidThenAddButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> view.enableUI());
        JTextComponentFixture startTimeTextBox = window.textBox("startTimeTextBox");
        JTextComponentFixture endTextBox = window.textBox("endTimeTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("dateTextBox").enterText("24/07/2026");
        
        startTimeTextBox.enterText("08:00");
        endTextBox.enterText("   ");
        addButton.requireDisabled();
        
        startTimeTextBox.setText("");
        endTextBox.setText("");
        
        startTimeTextBox.enterText("   ");
        endTextBox.enterText("09:00");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenEitherDoctorOrDepartmentIdAreEmptyThenAddButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> view.enableUI());
        JTextComponentFixture doctorIdTextBox = window.textBox("doctorIdTextBox");
        JTextComponentFixture departmentIdTextBox = window.textBox("departmentIdTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        doctorIdTextBox.enterText("doctor_id");
        departmentIdTextBox.enterText(" ");
        addButton.requireDisabled();
        
        doctorIdTextBox.setText("");
        departmentIdTextBox.setText("");
        
        doctorIdTextBox.enterText(" ");
        departmentIdTextBox.enterText("er");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDateIsChosenFromCalendarAndOtherInputsArePresentThenTheAddButtonShouldBeEnabled() {
        GuiActionRunner.execute(() -> view.enableUI());
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("dateButton").click();
        window.robot().click(searchDateLabel(24));
        
        window.button("addButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenStartTimeIsChosenFromDropdownAndOtherInputsArePresentThenTheAddButtonShouldBeEnabled()  {
        GuiActionRunner.execute(() -> view.enableUI());
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("startTimeButton").click();
        slideDownToHourInMenu(8);
        window.robot().pressAndReleaseKeys(KeyEvent.VK_ENTER);
        
        window.button("addButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenEndTimeIsChosenFromDropdownAndOtherInputsArePresentThenTheAddButtonShouldBeEnabled()  {
        GuiActionRunner.execute(() -> view.enableUI());
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        
        window.button("endTimeButton").click();
        slideDownToHourInMenu(9);
        window.robot().pressAndReleaseKeys(KeyEvent.VK_ENTER);
        
        window.button("addButton").requireEnabled();
    }
    
    private JLabel searchDateLabel(int day) {
        return window.robot().finder().find(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override
            protected boolean isMatching(JLabel label) {
                return ("" + day).equals(label.getText()) && label.isShowing();
            }
        });
    }
    
    private void slideDownToHourInMenu(int hour) {
        if (hour > 23) return;
        for(int i = 0; i < hour + 1; i++) {
            window.robot().pressAndReleaseKeys(KeyEvent.VK_DOWN);
        }
    }
}
