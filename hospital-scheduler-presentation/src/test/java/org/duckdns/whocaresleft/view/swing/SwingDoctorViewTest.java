package org.duckdns.whocaresleft.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;

import javax.swing.DefaultListModel;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UI tests for SwingDoctorViewTest")
class SwingDoctorViewTest {
    
    private FrameFixture window;
    private SwingDoctorView doctorView;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        GuiActionRunner.execute(() -> {
            doctorView = new SwingDoctorView();
            return doctorView;
        });
        window = Containers.showInFrame(doctorView);
    }
    
    @AfterEach
    void teardown() {
        if (window != null)
            window.cleanUp();
    }
    
    @Test @DisplayName("Initial Setup")
    void testInitialSetup() {
        window.label("idLbl");
        window.textBox("idTextBox").requireEnabled();
        window.label("firstNameLbl");
        window.textBox("firstNameTextBox").requireEnabled();
        window.label("lastNameLbl");
        window.textBox("lastNameTextBox").requireEnabled();
        window.button("addBtn").requireDisabled();
        
        window.list("doctorList");
        
        window.button("deleteBtn").requireDisabled();
        
        window.label("selectedDoctor");
        window.checkBox("editDoctor").requireDisabled();
        
        window.label("selectedIdLbl");
        window.textBox("selectedIdTextBox").requireDisabled();
        window.label("selectedFirstNameLbl");
        window.textBox("selectedFirstNameTextBox").requireDisabled();
        window.label("selectedLastNameLbl");
        window.textBox("selectedLastNameTextBox").requireDisabled();
        
        window.button("updateBtn").requireDisabled();
        
        window.label("errorLabel").requireText(" ");
    }
    
    @Test
    void testWhenIdFirstNameAndLastNameAreNonEmptyThenAddButtonShouldBeEnabled() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        window.button("addBtn").requireEnabled();
    }
    
    @Test
    void testWhenEitherIdOrFirstNameOrLastNameAreBlankThenAddButtonShouldBeDisabled() {
        JTextComponentFixture idTextBox = window.textBox("idTextBox");
        JTextComponentFixture fnTextBox = window.textBox("firstNameTextBox");
        JTextComponentFixture lnTextBox = window.textBox("lastNameTextBox");
        JButtonFixture addBtn = window.button("addBtn");
        
        idTextBox.enterText("doctor_id");
        fnTextBox.enterText("doc");
        lnTextBox.enterText(" ");
        addBtn.requireDisabled();
        
        idTextBox.setText("");
        fnTextBox.setText("");
        lnTextBox.setText("");
        
        idTextBox.enterText("doctor_id");
        fnTextBox.enterText(" ");
        lnTextBox.enterText("tor");
        addBtn.requireDisabled();
        
        idTextBox.setText("");
        fnTextBox.setText("");
        lnTextBox.setText("");
        
        idTextBox.enterText(" ");
        fnTextBox.enterText("doc");
        lnTextBox.enterText("tor");
        addBtn.requireDisabled();
    }
    
    @Test
    void testDeleteButtonShouldBeEnabledOnlyWhenADoctorIsSelected() {
        GuiActionRunner.execute(() ->
            doctorView.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        JButtonFixture deleteBtn = window.button("deleteBtn");
        deleteBtn.requireEnabled();
        window.list("doctorList").clearSelection();
        deleteBtn.requireDisabled();
    }
    
    @Test
    void testEditCheckBoxShouldBeEnabledOnlyWhenADoctorIsSelected() {
        GuiActionRunner.execute(() ->
            doctorView.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        JCheckBoxFixture editCheckbox = window.checkBox("editDoctor");
        editCheckbox.requireEnabled();
        window.list("doctorList").clearSelection();
        editCheckbox.requireDisabled();
    }
    
    @Test
    void testEditCheckBoxShouldBeSelectableOnlyWhenEnabled() {
        GuiActionRunner.execute(() ->
            doctorView.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        JCheckBoxFixture editCheckbox = window.checkBox("editDoctor");
        
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> editCheckbox.check());
        
        window.list("doctorList").selectItem(0);
        editCheckbox.requireEnabled();
        
        editCheckbox.check();
        editCheckbox.requireSelected();
        
        window.list("doctorList").clearSelection();
        editCheckbox.requireDisabled();
        editCheckbox.requireNotSelected();
    }
    
    @Test
    void editTextFieldsShouldContainTheValuesFromTheSelectedDoctorEvenIfDisabled() {
        GuiActionRunner.execute(() ->
        doctorView.getDoctorListModel()
            .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        JTextComponentFixture selectedIdTextBox = window.textBox("selectedIdTextBox");
        JTextComponentFixture selectedFirstNameTextBox = window.textBox("selectedFirstNameTextBox");
        JTextComponentFixture selectedLastNameTextBox = window.textBox("selectedLastNameTextBox");
        
        selectedIdTextBox.requireDisabled().requireText("doctor_id");
        selectedFirstNameTextBox.requireDisabled().requireText("doc");
        selectedLastNameTextBox.requireDisabled().requireText("tor");
    }
    
    @Test
    void editTextFieldsShouldBeEmptyAfterStudentIsNotSelectedAnymore() {
        GuiActionRunner.execute(() ->
        doctorView.getDoctorListModel()
            .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        JTextComponentFixture selectedIdTextBox = window.textBox("selectedIdTextBox");
        JTextComponentFixture selectedFirstNameTextBox = window.textBox("selectedFirstNameTextBox");
        JTextComponentFixture selectedLastNameTextBox = window.textBox("selectedLastNameTextBox");
        
        selectedIdTextBox.requireDisabled().requireText("doctor_id");
        selectedFirstNameTextBox.requireDisabled().requireText("doc");
        selectedLastNameTextBox.requireDisabled().requireText("tor");
        
        window.list("doctorList").clearSelection();
        selectedIdTextBox.requireDisabled().requireText("");
        selectedFirstNameTextBox.requireDisabled().requireText("");
        selectedLastNameTextBox.requireDisabled().requireText("");
    }
    
    @Test
    void testEditFirstAndLastNameTextBoxesShouldBeEnabledOnlyWhenEditCheckboxIsTickedButNotId() {
        GuiActionRunner.execute(() ->
            doctorView.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        JCheckBoxFixture editCheckbox = window.checkBox("editDoctor");
        JTextComponentFixture selectedIdTextBox = window.textBox("selectedIdTextBox");
        JTextComponentFixture selectedFirstNameTextBox = window.textBox("selectedFirstNameTextBox");
        JTextComponentFixture selectedLastNameTextBox = window.textBox("selectedLastNameTextBox");
        
        editCheckbox.requireEnabled().requireNotSelected();
        selectedIdTextBox.requireDisabled();
        selectedFirstNameTextBox.requireDisabled();
        selectedLastNameTextBox.requireDisabled();
        
        editCheckbox.check();
        
        selectedIdTextBox.requireDisabled();
        selectedFirstNameTextBox.requireEnabled();
        selectedLastNameTextBox.requireEnabled();
        
        editCheckbox.uncheck();
        
        selectedIdTextBox.requireDisabled();
        selectedFirstNameTextBox.requireDisabled();
        selectedLastNameTextBox.requireDisabled();
    }
    
    @Test
    void testUpdateButtonShouldBeEnabledOnlyWhenAllEditTextfieldsAreNotBlank() {
        GuiActionRunner.execute(() ->
            doctorView.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor")
            .requireEnabled()
            .check()
            .requireSelected();
        
        JTextComponentFixture selectedFirstNameTextBox = window.textBox("selectedFirstNameTextBox");
        JTextComponentFixture selectedLastNameTextBox = window.textBox("selectedLastNameTextBox");
        JButtonFixture updateBtn = window.button("updateBtn");
        
        selectedFirstNameTextBox.setText("");
        selectedLastNameTextBox.setText("");
        
        selectedFirstNameTextBox.enterText("doc");
        selectedLastNameTextBox.enterText(" ");
        updateBtn.requireDisabled();
        
        selectedFirstNameTextBox.setText("");
        selectedLastNameTextBox.setText("");
        
        selectedFirstNameTextBox.enterText(" ");
        selectedLastNameTextBox.enterText("tor");
        updateBtn.requireDisabled();
        
        selectedFirstNameTextBox.enterText("doc");
        selectedLastNameTextBox.enterText("tor");
        updateBtn.requireEnabled();
    }
    
    @Test
    void testShowAllDoctorsShouldAddDoctorInformationToTheList() {
        Doctor doc1 = Doctor.createDoctor(Id.createId("doc1"), "doc", "tor1");
        Doctor doc2 = Doctor.createDoctor(Id.createId("doc2"), "doc", "tor2");
        GuiActionRunner.execute(() -> doctorView.showAllDoctors(Arrays.asList(doc1, doc2)));
        
        String[] listContents = window.list().contents();
        assertThat(listContents)
            .containsExactly(
                doc1.toString(),
                doc2.toString());
    }
    
    @Test
    void testShowErrorDuplicateDoctorShouldShowMessageInTheErrorLabel() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        GuiActionRunner.execute(() -> doctorView.showErrorDuplicateDoctor(doctor.getId()));
        
        window.label("errorLabel").requireText("There already is a Doctor with id doctor_id");
    }
    
    @Test
    void testShowErrorDoctorNotFoundShouldShowMessageInTheErrorLabel() {
        Id id = Id.createId("doctor_id");
        GuiActionRunner.execute(() -> doctorView.showErrorDoctorNotFound(id));
        
        window.label("errorLabel").requireText("No doctor with id doctor_id was found");
    }
    
    @Test
    void testDoctorAddedShouldAddDoctorToTheListAndClearErrorLabel() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        GuiActionRunner.execute(() ->
            doctorView.doctorAdded(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        
        String[] listContents = window.list().contents();
        assertThat(listContents)
            .containsExactly(doctor.toString());
        window.label("errorLabel").requireText(" ");
    }
    
    @Test
    void testDoctorRemovedShouldRemoveDoctorFromListAndClearErrorLabel() {
        Doctor doctor1 = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        Doctor doctor2 = Doctor.createDoctor(Id.createId("doctor_2"), "doc", "tor");
        
        GuiActionRunner.execute(() -> {
            DefaultListModel<Doctor> doctorListModel = doctorView.getDoctorListModel();
            doctorListModel.addElement(doctor1);
            doctorListModel.addElement(doctor2);
        });
            
        GuiActionRunner.execute(() -> {  
            doctorView.doctorRemoved(Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"));
        });
        
        String[] listContents = window.list().contents();
        assertThat(listContents)
            .containsExactly(doctor2.toString());
        window.label("errorLabel").requireText(" ");
    }
    
    @Test
    void testDoctorUpdateShouldChangeDoctorInTheListAndClearErrorLabel() {
        Doctor original = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        Doctor newDoctor = Doctor.createDoctor(Id.createId("doctor_id"), "new", "name");
        
        GuiActionRunner.execute(() -> {
            DefaultListModel<Doctor> doctorListModel = doctorView.getDoctorListModel();
            doctorListModel.addElement(original);
        });
            
        GuiActionRunner.execute(() -> {  
            doctorView.doctorUpdated(
                Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"),
                Doctor.createDoctor(Id.createId("doctor_id"), "new", "name"));
        });
        
        String[] listContents = window.list().contents();
        assertThat(listContents)
            .containsExactly(newDoctor.toString());
        window.label("errorLabel").requireText(" ");
    }
    
    // Test interaction with controller
}
