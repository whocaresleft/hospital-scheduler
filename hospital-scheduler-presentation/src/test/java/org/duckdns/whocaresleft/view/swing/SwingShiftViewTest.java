package org.duckdns.whocaresleft.view.swing;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    
    class Helpers {
        
        private static JLabel searchDateLabel(FrameFixture window, int day) {
            return window.robot().finder().find(new GenericTypeMatcher<JLabel>(JLabel.class) {
                @Override
                protected boolean isMatching(JLabel label) {
                    return ("" + day).equals(label.getText()) && label.isShowing();
                }
            });
        }
        
        private static void slideDownToHourInMenu(FrameFixture window, int hour) {
            if (hour > 23) return;
            for(int i = 0; i < hour + 1; i++) {
                window.robot().pressAndReleaseKeys(KeyEvent.VK_DOWN);
            }
        }
    }
    
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
    
    @Nested @DisplayName("UI Enabling and Disabling")
    class UIEnablingAndDisabling {
        
        @Nested @DisplayName("Overall UI")
        class OverallUI {
            
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
            void testShowAllShiftsShouldEnableUI() {
                Shift s1 = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                Shift s2 = Shift.createShift(
                    Id.createId("doctor_2"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_00);
                
                view.showAllShifts(Arrays.asList(s1, s2));
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenAddButtonIsPressedThenUIShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("doctor_id");
                window.textBox("departmentIdTextBox").enterText("er");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("08:00");
                window.textBox("endTimeTextBox").enterText("09:00");
                
                window.button("addButton").click();
                
                window.textBox("doctorIdTextBox").requireNotEditable();
                window.textBox("departmentIdTextBox").requireNotEditable();
                window.textBox("dateTextBox").requireNotEditable();
                window.button("dateButton").requireDisabled();
                window.textBox("startTimeTextBox").requireNotEditable();
                window.button("startTimeButton").requireDisabled();
                window.textBox("endTimeTextBox").requireNotEditable();
                window.button("endTimeButton").requireDisabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireDisabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShiftAddedIsCalledThenUIShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.disableUI());
                
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                
                view.shiftAdded(shift);
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShowErrorOverlappedShiftIsCalledThenUIShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.disableUI());
                
                Shift shift1 = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                Shift shift2 = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_30);
                
                view.showErrorOverlappedShift(
                    shift1,
                    shift2);
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShowErrorDoctorNotFoundtIsCalledThenUIShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.disableUI());
                
                view.showErrorDoctorNotFound(Id.createId("doctor_id"));
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShowErrorDepartmentNotFoundIsCalledThenUIShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.disableUI());
                
                view.showErrorDepartmentNotFound(Id.createId("er"));
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenDeleteButtonIsPressedThenUIShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").click();
                
                window.textBox("doctorIdTextBox").requireNotEditable();
                window.textBox("departmentIdTextBox").requireNotEditable();
                window.textBox("dateTextBox").requireNotEditable();
                window.button("dateButton").requireDisabled();
                window.textBox("startTimeTextBox").requireNotEditable();
                window.button("startTimeButton").requireDisabled();
                window.textBox("endTimeTextBox").requireNotEditable();
                window.button("endTimeButton").requireDisabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireDisabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShiftRemovedIsCalledThenUIShouldBeEnabled() {
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.disableUI());
                
                view.shiftRemoved(shift);
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShowErrorShiftNotFoundIsCalledThenUIShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.disableUI());
                
                view.showErrorShiftNotFound(Shift.createShift(
                    Id.createId("doc_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_00));
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedThenUIShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").enterText("_new");
                window.button("updateButton").click();
                
                window.textBox("doctorIdTextBox").requireNotEditable();
                window.textBox("departmentIdTextBox").requireNotEditable();
                window.textBox("dateTextBox").requireNotEditable();
                window.button("dateButton").requireDisabled();
                window.textBox("startTimeTextBox").requireNotEditable();
                window.button("startTimeButton").requireDisabled();
                window.textBox("endTimeTextBox").requireNotEditable();
                window.button("endTimeButton").requireDisabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireDisabled();
                window.checkBox("editShift").requireDisabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireDisabled();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenShiftUpdatedIsCalledThenUIShouldBeEnabled() {
                Shift original = Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(original));
                GuiActionRunner.execute(() -> view.disableUI());
                
                view.shiftUpdated(
                    original,
                    Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30));
                
                window.textBox("doctorIdTextBox").requireEditable();
                window.textBox("departmentIdTextBox").requireEditable();
                window.textBox("dateTextBox").requireEditable();
                window.button("dateButton").requireEnabled();
                window.textBox("startTimeTextBox").requireEditable();
                window.button("startTimeButton").requireEnabled();
                window.textBox("endTimeTextBox").requireEditable();
                window.button("endTimeButton").requireEnabled();
                window.button("addButton").requireDisabled();
                window.list("shiftList").requireEnabled();
                window.checkBox("editShift").requireEnabled();
                window.textBox("selectedDoctorIdTextBox").requireNotEditable();
                window.textBox("selectedDepartmentIdTextBox").requireNotEditable();
                window.textBox("selectedDateTextBox").requireNotEditable();
                window.button("selectedDateButton").requireDisabled();
                window.textBox("selectedStartTimeTextBox").requireNotEditable();
                window.button("selectedStartTimeButton").requireDisabled();
                window.textBox("selectedEndTimeTextBox").requireNotEditable();
                window.button("selectedEndTimeButton").requireDisabled();
                window.button("deleteButton").requireEnabled();
                window.button("updateButton").requireDisabled();
            }
        }
        
        @Nested @DisplayName("Related to Add button")
        class AddButtonRelated {
            
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
            void testWhenEitherStartOrEndTimeAreNotValidThenAddButtonShouldBeDisabled() {
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
                window.robot().click(Helpers.searchDateLabel(window, 24));
                
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
                Helpers.slideDownToHourInMenu(window, 8);
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
                Helpers.slideDownToHourInMenu(window, 9);
                window.robot().pressAndReleaseKeys(KeyEvent.VK_ENTER);
                
                window.button("addButton").requireEnabled();
            }
            
            @Test @GUITest
            void testWhenAddButtonIsPressedThenTheInputTextFieldsShouldBeClearedAndAddButtonDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                JTextComponentFixture doctorIdTextBox = window.textBox("doctorIdTextBox");
                JTextComponentFixture departmentIdTextBox = window.textBox("departmentIdTextBox");
                JTextComponentFixture dateTextBox = window.textBox("dateTextBox");
                JTextComponentFixture startTimeTextBox = window.textBox("startTimeTextBox");
                JTextComponentFixture endTextBox = window.textBox("endTimeTextBox");
                JButtonFixture addButton = window.button("addButton");
                
                doctorIdTextBox.enterText("doctor_id");
                departmentIdTextBox.enterText("er");
                dateTextBox.enterText("24/07/2026");
                startTimeTextBox.enterText("08:00");
                endTextBox.enterText("09:00");
                addButton.requireEnabled();
                
                addButton.click();
                
                doctorIdTextBox.requireText("");
                departmentIdTextBox.requireText("");
                dateTextBox.requireText("");
                startTimeTextBox.requireText("");
                endTextBox.requireText("");
                addButton.requireDisabled();
            }
        }
        
        @Nested @DisplayName("Related to Delete button & Edit checkbox")
        class DeleteButtonEditCheckboxRelated {
            
            @Test @GUITest
            void testWhenShiftIsSelectedThenDeleteButtonShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                
                window.button("deleteButton").requireDisabled();
                window.list("shiftList").selectItem(0);
                window.button("deleteButton").requireEnabled();
            }
            
            @Test @GUITest
            void testWhenShiftIsSelectedThenEditCheckBoxShouldBeEnabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                
                window.checkBox("editShift").requireDisabled();
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").requireEnabled();
            }
            
            @Test @GUITest
            void testWhenEditCheckBoxIsTickedThenDeleteButtonShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").requireEnabled();
                window.checkBox("editShift").click();
                window.button("deleteButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenDeleteButtonIsPressedThenTheSelectedShiftShouldBeDeselected() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").click();
                
                window.list("shiftList").requireNoSelection();
            }
            
            @Test @GUITest
            void testWhenDeleteButtonIsPressedThenTheSelectedShiftTextBoxesShouldBeCleared() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").click();
                
                window.textBox("selectedDoctorIdTextBox").requireText("");
                window.textBox("selectedDepartmentIdTextBox").requireText("");
                window.textBox("selectedDateTextBox").requireText("");
                window.textBox("selectedStartTimeTextBox").requireText("");
                window.textBox("selectedEndTimeTextBox").requireText("");
            }
            
            @Test @GUITest
            void testWhenEditCheckBoxIsTickedThenTheSelectedShiftTextBoxesShouldBeEditable() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").requireEnabled().requireEditable();
                window.textBox("selectedDepartmentIdTextBox").requireEnabled().requireEditable();
                window.textBox("selectedDateTextBox").requireEnabled().requireEditable();
                window.button("selectedDateButton").requireEnabled();
                window.textBox("selectedStartTimeTextBox").requireEnabled().requireEditable();
                window.button("selectedStartTimeButton").requireEnabled();
                window.textBox("selectedEndTimeTextBox").requireEnabled().requireEditable();
                window.button("selectedEndTimeButton").requireEnabled();
            }
            
            @Test @GUITest
            void testWhenShiftIsSelectedAndEditIsTickedThenChangingSelectionShouldRemoveTheTickAndReEnableDeleteButton() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift s1 = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                Shift s2 = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_09_00, TIME_09_30);
                
                GuiActionRunner.execute(() -> {
                    DefaultListModel<Shift> dlm = view.getShiftListModel();
                    dlm.addElement(s1);
                    dlm.addElement(s2);
                });
                window.list("shiftList").selectItem(0);
                
                window.checkBox("editShift").click();
                window.button("deleteButton").requireDisabled();
                
                window.list("shiftList").selectItem(1);
                
                window.checkBox("editShift").requireEnabled().requireNotSelected();
                window.button("deleteButton").requireEnabled();
            }
        }
        
        @Nested @DisplayName("Related to Update button")
        class UpdateButtonRelated {
            
            @ParameterizedTest @GUITest
            @CsvSource({
                "selectedDoctorIdTextBox,another_doctor_id",
                "selectedDepartmentIdTextBox,sr",
                "selectedDateTextBox,01/09/2026",
                "selectedStartTimeTextBox,08:30",
                "selectedEndTimeTextBox,09:30",
            })
            void testWhenSelectedShiftDoctorIdIsModifiedThenUpdateButtonShouldBeEnabled(String textBoxName, String writeValue) {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.button("updateButton").requireDisabled();
                
                window.textBox(textBoxName).setText("");
                window.textBox(textBoxName).enterText(writeValue);
                
                window.button("updateButton").requireEnabled();
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedThenTheEditShiftCheckBoxShouldBeDeselected() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.checkBox("editShift").requireSelected();
                window.textBox("selectedDoctorIdTextBox").enterText("_new");
                window.button("updateButton").click();
                window.checkBox("editShift").requireNotSelected();
            }
            
            @Test @GUITest
            void testWhenEditShiftIsDeselectedWhileUpdateButtonIsEnabledThenUpdateButtonShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").enterText("_new");
                window.button("updateButton").requireEnabled();
                
                window.checkBox("editShift").click();
                window.button("updateButton").requireDisabled();
            }
            
            @Test @GUITest
            void testWhenEitherSelectedStartOrEndTimeAreNotValidThenUpdateButtonShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                JTextComponentFixture selectedStartTimeTextBox = window.textBox("selectedStartTimeTextBox");
                JTextComponentFixture selectedEndTextBox = window.textBox("selectedEndTimeTextBox");
                JButtonFixture updateButton = window.button("updateButton");
                selectedStartTimeTextBox.setText("").enterText("07:00");
                updateButton.requireEnabled();
                
                selectedStartTimeTextBox.setText("").enterText("08:00");
                selectedEndTextBox.setText("").enterText("   ");
                updateButton.requireDisabled();
                
                selectedStartTimeTextBox.setText("").enterText("   ");
                selectedEndTextBox.setText("").enterText("09:00");
                updateButton.requireDisabled();
            }
            
            @Test @GUITest
            void testWhenEitherSelectedDoctorOrDepartmentIdAreEmptyThenUpdateButtonShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                JTextComponentFixture selectedDoctorIdTextBox = window.textBox("selectedDoctorIdTextBox");
                JTextComponentFixture selectedDepartmentIdTextBox = window.textBox("selectedDepartmentIdTextBox");
                JButtonFixture updateButton = window.button("updateButton");
                selectedDoctorIdTextBox.enterText("_new");
                updateButton.requireEnabled();
                
                selectedDoctorIdTextBox.setText("").enterText("   ");
                selectedDepartmentIdTextBox.setText("").enterText("er_new");
                updateButton.requireDisabled();
                
                selectedDoctorIdTextBox.setText("").enterText("doctor_id_new");
                selectedDepartmentIdTextBox.setText("").enterText("   ");
                updateButton.requireDisabled();
            }
            
            @Test @GUITest
            void testWhenSelectedDateIsNotValidThenUpdateButtonShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                JTextComponentFixture selectedDateTextBox = window.textBox("selectedDateTextBox");
                JButtonFixture updateButton = window.button("updateButton");
                selectedDateTextBox.setText("").enterText("01/01/2026");
                updateButton.requireEnabled();
                
                selectedDateTextBox.setText("").enterText("   ");
                updateButton.requireDisabled();
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsEnabledButAllFieldsAreBroughtBackToOriginalValueThenUpdateShouldBeDisabled() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                JTextComponentFixture selectedDoctorIdTextBox = window.textBox("selectedDoctorIdTextBox");
                JTextComponentFixture selectedDepartmentIdTextBox = window.textBox("selectedDepartmentIdTextBox");
                JTextComponentFixture selectedDateTextBox = window.textBox("selectedDateTextBox");
                JTextComponentFixture selectedStartTimeTextBox = window.textBox("selectedStartTimeTextBox");
                JTextComponentFixture selectedEndTextBox = window.textBox("selectedEndTimeTextBox");
                JButtonFixture updateButton = window.button("updateButton");
                
                selectedDoctorIdTextBox.enterText("_new");
                updateButton.requireEnabled();
                selectedDoctorIdTextBox.setText("").enterText("doctor_id");
                updateButton.requireDisabled();
                
                selectedDepartmentIdTextBox.enterText("_new");
                updateButton.requireEnabled();
                selectedDepartmentIdTextBox.setText("").enterText("er");
                updateButton.requireDisabled();
                
                selectedDateTextBox.setText("").enterText("01/01/2026");
                updateButton.requireEnabled();
                selectedDateTextBox.setText("").enterText("24/07/2026");
                updateButton.requireDisabled();
                
                selectedStartTimeTextBox.setText("").enterText("01:00");
                updateButton.requireEnabled();
                selectedStartTimeTextBox.setText("").enterText("08:00");
                updateButton.requireDisabled();
                
                selectedEndTextBox.setText("").enterText("02:00");
                updateButton.requireEnabled();
                selectedEndTextBox.setText("").enterText("09:00");
                updateButton.requireDisabled();
            }
        }
    }
    
    @Nested @DisplayName("UI Logic")
    class UILogic {
        
        @Nested @DisplayName("Generic")
        class Generic {
            
            @Test @GUITest
            void testWhenShiftIsSelectedThenTheSelectedShiftTextFieldsShouldContainItsValues() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                
                window.list("shiftList").selectItem(0);
                
                window.textBox("selectedDoctorIdTextBox").requireText("doctor_id");
                window.textBox("selectedDepartmentIdTextBox").requireText("er");
                window.textBox("selectedDateTextBox").requireText("24/07/2026");
                window.textBox("selectedStartTimeTextBox").requireText("08:00");
                window.textBox("selectedEndTimeTextBox").requireText("09:00");
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedThenItShouldKeepUpdatedInfoBothInSelectedShiftTextBoxesAndListSelection() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").enterText("_new");
                window.button("updateButton").click();
                
                window.list("shiftList").requireSelection(0);
                window.textBox("selectedDoctorIdTextBox").requireText("doctor_id_new");
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsEnabledAndEditCheckboxIsDeselectedAndReselectedThenUpdateShouldBeEnabledAgainWithoutFurtherFieldsModifications() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedStartTimeTextBox").setText("");
                window.textBox("selectedStartTimeTextBox").enterText("08:30");
                
                window.button("updateButton").requireEnabled();
                window.checkBox("editShift").click();
                window.button("updateButton").requireDisabled();
                window.checkBox("editShift").click();
                window.button("updateButton").requireEnabled();
            }
            
            @Test @GUITest
            void testShowErrorOverlappedShiftWhenOriginalIsNotInTheListAddsTheOriginalAgainToTheList() {
                Shift original = Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                
                assertThat(window.list("shiftList").contents())
                    .isEmpty();
                
                view.showErrorOverlappedShift(
                    original,
                    Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(original.toString());
            }
            
            @Test @GUITest
            void testShowErrorOverlappedShiftWhenOriginalIsInTheListDoesNotAddTheOriginalToTheList() {
                Shift original = Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(original));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(original.toString());
                
                view.showErrorOverlappedShift(
                    original,
                    Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(original.toString());
            }
            
            @Test @GUITest
            void testShowErrorShiftNotFoundShouldRemoveShiftFromList() {
                Shift shift = Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(shift.toString());
                
                view.showErrorShiftNotFound(shift);
                
                assertThat(window.list("shiftList").contents())
                    .isEmpty();
            }
            
            @Test @GUITest
            void testShowErrorShiftNotFoundWithAShiftNotInTheListShouldDoNothing() {
                Shift shift = Shift.createShift(Id.createId("Doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                
                assertThat(window.list("shiftList").contents())
                    .isEmpty();
                
                view.showErrorShiftNotFound(shift);
                
                assertThat(window.list("shiftList").contents())
                    .isEmpty();
            }
        }
        
        @Nested @DisplayName("Info and Error labels behaviour")
        class InfoErrorLabels {
            
            @Test @GUITest
            void testWhenAddButtonIsPressedThenInfoLabelShouldShowActionMessage() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("doctor_id");
                window.textBox("departmentIdTextBox").enterText("er");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("08:00");
                window.textBox("endTimeTextBox").enterText("09:00");
                
                window.button("addButton").click();
                
                window.label("infoLabel").requireText("Adding Shift...");
            }
            
            @Test @GUITest
            void testWhenDeleteButtonIsPressedThenInfoLabelShouldShowActionMessage() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").click();
                
                window.label("infoLabel").requireText("Deleting Shift...");
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedThenInfoLabelShouldShowActionMessage() {
                GuiActionRunner.execute(() -> view.enableUI());
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(
                        Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").enterText("_new");
                window.button("updateButton").click();
                
                window.label("infoLabel").requireText("Updating Shift...");
            }
            
            @Test @GUITest
            void testShowErrorMessageShouldClearInformationLabel() {
                GuiActionRunner.execute(() -> view.showInfoMessage("Hello"));
                
                window.label("infoLabel").requireText("Hello");
                GuiActionRunner.execute(() -> view.showErrorMessage("Err"));
                window.label("infoLabel").requireText(" ");
            }
            
            @Test @GUITest
            void testShowInfoMessageShouldClearErrorLabel() {
                GuiActionRunner.execute(() -> view.showErrorMessage("Err"));
                
                window.label("errorLabel").requireText("Err");
                GuiActionRunner.execute(() -> view.showInfoMessage("Hello"));
                window.label("errorLabel").requireText(" ");
            }
        }
        
        @Nested @DisplayName("ShiftView interface")
        class ShiftViewInterface {
            
            @Test @GUITest
            void testShowAllShiftsShouldAddEachShiftDescriptionToTheList() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift s1 = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                Shift s2 = Shift.createShift(
                        Id.createId("doctor_2"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_00);
                
                view.showAllShifts(Arrays.asList(s1, s2));
                
                String[] listContents = window.list("shiftList").contents();
                assertThat(listContents)
                    .containsExactlyInAnyOrder(s1.toString(), s2.toString());
            }
            
            @Test @GUITest
            void testShowAllShiftsWhenListAlreadyContainsShiftsShouldReplaceTheExtinsgOnesWithTheNewOnes() {
                Shift old  = Shift.createShift(Id.createId("doc"), Id.createId("dep"), DATE_24_07_2026, TIME_08_00, TIME_09_30);
                Shift new1 = Shift.createShift(Id.createId("doc"), Id.createId("dep"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
                Shift new2 = Shift.createShift(Id.createId("doc"), Id.createId("dep"), DATE_24_07_2026, TIME_09_00, TIME_09_30);
                
                GuiActionRunner.execute(() ->
                    view.getShiftListModel().addElement(old));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(old.toString());
                
                view.showAllShifts(Arrays.asList(new1, new2));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactlyInAnyOrder(new1.toString(), new2.toString());
            }
            
            @Test @GUITest
            void testShowErrorOverlappedShiftShouldShowMessageInErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift original = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                Shift overlapped = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_30);
                
                view.showErrorOverlappedShift(original, overlapped);
                
                window.label("errorLabel").requireText("Shift doctor_1-er overlaps with doctor_1-sr on 2026-07-24 (08:00-09:00/08:30-09:30)");
            }
            
            @Test @GUITest
            void testShowErrorShiftNotFoundShouldShowMessageInErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift notFound = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                
                view.showErrorShiftNotFound(notFound);
                
                window.label("errorLabel").requireText("No Shift matching (doctor_1-er), 2026-07-24: (08:00-09:00) was found");
            }
            
            @Test @GUITest
            void testShowErrorDoctorNotFoundShouldShowMessageInErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Id nonExistentDoctorId = Id.createId("doctor_id");
                
                view.showErrorDoctorNotFound(nonExistentDoctorId);
                
                window.label("errorLabel").requireText("No Doctor with id doctor_id was found");
            }
            
            @Test @GUITest
            void testShowErrorDepartmentNotFoundShouldShowMessageInErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Id nonExistentDepartmentId = Id.createId("er");
                
                view.showErrorDepartmentNotFound(nonExistentDepartmentId);
                
                window.label("errorLabel").requireText("No Department with id er was found");
            }
            
            @Test @GUITest
            void testShiftAddedShouldAddTheShiftToTheListShowInfoMessageAndClearErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                
                view.shiftAdded(shift);
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(shift.toString());
                window.label("infoLabel").requireText("Shift added!");
                window.label("errorLabel").requireText(" ");
            }
            
            @Test @GUITest
            void testShiftRemovedShouldRemoveTheShiftFromTheListShowInfoMessageAndClearErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift s1 = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                Shift s2 = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_09_00, TIME_09_30);
                
                GuiActionRunner.execute(() -> {
                    DefaultListModel<Shift> dlm = view.getShiftListModel();
                    dlm.addElement(s1);
                    dlm.addElement(s2);
                });
                
                view.shiftRemoved(Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_09_00, TIME_09_30));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(s1.toString());
                window.label("infoLabel").requireText("Shift removed!");
                window.label("errorLabel").requireText(" ");
            }
            
            @Test @GUITest
            void testShiftUpdatedShouldUpdateTheShiftInTheListShowInfoMessageAndClearErrorLabel() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift oldShift = Shift.createShift(
                    Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                Shift newShift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_00);
                
                GuiActionRunner.execute(() -> 
                    view.getShiftListModel().addElement(oldShift));
                
                view.shiftUpdated(
                    Shift.createShift(Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00),
                    Shift.createShift(Id.createId("doctor_1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_00));
                
                assertThat(window.list("shiftList").contents())
                    .containsExactly(newShift.toString());
                window.label("infoLabel").requireText("Shift updated!");
                window.label("errorLabel").requireText(" ");
            }
        }
        
        @Nested @DisplayName("Interaction (Mocked) with presenter")
        class PresenterInteraction {
            
            @Test @GUITest
            void testWhenAddButtonIsPressedThenItShouldDelegateToPresenterAddShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("doctor_id");
                window.textBox("departmentIdTextBox").enterText("er");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("08:00");
                window.textBox("endTimeTextBox").enterText("09:00");
                
                window.button("addButton").click();
                
                await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                    verify(presenter)
                        .addShift(Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00)));
            }
            
            @Test @GUITest
            void testWhenDeleteButtonIsPressedItShouldDelegateToPresenterRemoveShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                
                window.button("deleteButton").click();
                
                await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                    verify(presenter)
                        .removeShift(shift));
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedItShouldDelegateToPresenterUpdateShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedStartTimeTextBox").setText("");
                window.textBox("selectedEndTimeTextBox").setText("");
                window.textBox("selectedStartTimeTextBox").enterText("08:30");
                window.textBox("selectedEndTimeTextBox").enterText("09:30");
                
                window.button("updateButton").click();
                
                await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                    verify(presenter)
                        .updateShift(
                            shift,
                            Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30)));
            }
            @Test @GUITest
            void testWhenAddButtonIsPressedAndDoctorIdCreationFailsThenItDoesNotDelegateToPresenterAddShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("invalid-doctor-id");
                window.textBox("departmentIdTextBox").enterText("er");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("08:00");
                window.textBox("endTimeTextBox").enterText("09:00");
                
                window.button("addButton").click();
                
                window.label("errorLabel").requireText("Doctor Id contains invalid value: Letters, digits, and underscores only");
                verifyNoInteractions(presenter);
            }
            
            @Test @GUITest
            void testWhenAddButtonIsPressedAndDepartmentIdCreationFailsThenItDoesNotDelegateToPresenterAddShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("doctor_id");
                window.textBox("departmentIdTextBox").enterText("invalid-department-id");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("08:00");
                window.textBox("endTimeTextBox").enterText("09:00");
                
                window.button("addButton").click();
                
                window.label("errorLabel").requireText("Department Id contains invalid value: Letters, digits, and underscores only");
                verifyNoInteractions(presenter);
            }
            
            @Test @GUITest
            void testWhenAddButtonIsPressedAndShiftCreationFailsThenItDoesNotDelegateToPresenterAddShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                window.textBox("doctorIdTextBox").enterText("doctor_id");
                window.textBox("departmentIdTextBox").enterText("er");
                window.textBox("dateTextBox").enterText("24/07/2026");
                window.textBox("startTimeTextBox").enterText("09:00");
                window.textBox("endTimeTextBox").enterText("08:00");
                
                window.button("addButton").click();
                
                window.label("errorLabel").requireText("Shift has negative duration, starting time is after than ending time");
                verifyNoInteractions(presenter);
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedAndDoctorIdCreationFailsThenItDoesNotDelegateToPresenterUpdateShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDoctorIdTextBox").setText("").enterText("invalid-doctor-id");
                
                window.button("updateButton").click();
                
                window.label("errorLabel").requireText("Doctor Id contains invalid value: Letters, digits, and underscores only");
                verifyNoInteractions(presenter);
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedAndDepartmentIdCreationFailsThenItDoesNotDelegateToPresenterUpdateShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedDepartmentIdTextBox").setText("").enterText("invalid-department-id");
                
                window.button("updateButton").click();
                
                window.label("errorLabel").requireText("Department Id contains invalid value: Letters, digits, and underscores only");
                verifyNoInteractions(presenter);
            }
            
            @Test @GUITest
            void testWhenUpdateButtonIsPressedAndShiftCreationFailsThenItDoesNotDelegateToPresenterUpdateShift() {
                GuiActionRunner.execute(() -> view.enableUI());
                Shift shift = Shift.createShift(
                        Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
                GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
                window.list("shiftList").selectItem(0);
                window.checkBox("editShift").click();
                
                window.textBox("selectedStartTimeTextBox").setText("").enterText("09:00");
                window.textBox("selectedEndTimeTextBox").setText("").enterText("08:50");
                
                window.button("updateButton").click();
                
                window.label("errorLabel").requireText("Shift has negative duration, starting time is after than ending time");
                verifyNoInteractions(presenter);
            }
        }
    }
}
