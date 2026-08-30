package org.duckdns.whocaresleft.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import static org.awaitility.Awaitility.await;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("UI tests for SwingDoctorView")
class SwingDoctorViewTest {
    
    private static final int TIMEOUT = 15;
    
    @Mock
    private DoctorPresenter presenter;
    private AutoCloseable closeable;
    
    private FrameFixture window;
    private SwingDoctorView view;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        GuiActionRunner.execute(() -> {
            view = new SwingDoctorView();
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
    void testInitialSetup() {
        window.label("idLabel");
        window.textBox("idTextBox").requireEnabled();
        window.label("firstNameLabel");
        window.textBox("firstNameTextBox").requireEnabled();
        window.label("lastNameLabel");
        window.textBox("lastNameTextBox").requireEnabled();
        window.button("addButton");
        
        window.list("doctorList");
        
        window.label("selectedDoctorLabel");
        window.checkBox("editDoctor").requireDisabled();
        
        window.label("selectedIdLabel");
        window.textBox("selectedIdTextBox").requireEnabled().requireNotEditable();
        window.label("selectedFirstNameLabel");
        window.textBox("selectedFirstNameTextBox").requireEnabled().requireNotEditable();
        window.label("selectedLastNameLabel");
        window.textBox("selectedLastNameTextBox").requireEnabled().requireNotEditable();
        
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
        
        window.label("infoLabel").requireText(" ");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testWhenIdFirstNameAndLastNameAreNotEmptyThenAddButtonShouldBeEnabled() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        window.button("addButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenEitherIdFirstNameOrLastNameAreEmptyThenAddButtonShouldBeDisabled() {
        JTextComponentFixture idTextBox = window.textBox("idTextBox");
        JTextComponentFixture firstNameTextBox = window.textBox("firstNameTextBox");
        JTextComponentFixture lastNameTextBox = window.textBox("lastNameTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        idTextBox.enterText("doctor_id");
        firstNameTextBox.enterText("doc");
        lastNameTextBox.enterText(" ");
        addButton.requireDisabled();
        
        idTextBox.setText("");
        firstNameTextBox.setText("");
        lastNameTextBox.setText("");
        
        idTextBox.enterText("doctor_id");
        firstNameTextBox.enterText(" ");
        lastNameTextBox.enterText("tor");
        addButton.requireDisabled();
        
        idTextBox.setText("");
        firstNameTextBox.setText("");
        lastNameTextBox.setText("");
        
        idTextBox.enterText(" ");
        firstNameTextBox.enterText("doc");
        lastNameTextBox.enterText("tor");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedTheTextFieldsShouldBeClearedAndAddButtonDisabled() {
        JTextComponentFixture idTextBox = window.textBox("idTextBox");
        JTextComponentFixture firstNameTextBox = window.textBox("firstNameTextBox");
        JTextComponentFixture lastNameTextBox = window.textBox("lastNameTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        idTextBox.enterText("doctor_id");
        firstNameTextBox.enterText("doc");
        lastNameTextBox.enterText("tor");
        addButton.requireEnabled();
        
        addButton.click();
        
        idTextBox.requireText("");
        firstNameTextBox.enterText("");
        lastNameTextBox.enterText("");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDoctorIsSelectedThenTheSelectedDoctorTextFieldsShouldContainSuchValues() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.textBox("selectedIdTextBox").requireText("doctor_id");
        window.textBox("selectedFirstNameTextBox").requireText("doc");
        window.textBox("selectedLastNameTextBox").requireText("tor");
    }
    
    @Test @GUITest
    void testWhenDoctorIsSelectedThenTheDeleteButtonShouldBeEnabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        
        window.button("deleteButton").requireDisabled();
        window.list("doctorList").selectItem(0);
        window.button("deleteButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenDoctorIsSelectedThenTheEditCheckBoxShouldBeEnabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        
        window.checkBox("editDoctor").requireDisabled();
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenEditDoctorCheckBoxIsTickedTheDeleteButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.checkBox("editDoctor").click();
        window.button("deleteButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenEditDoctorCheckBoxIsTickedThenTheFirstAndLastNameTextBoxesShouldBeEditable() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);

        window.checkBox("editDoctor").click();
        window.textBox("selectedIdTextBox").requireEnabled().requireNotEditable();
        window.textBox("selectedFirstNameTextBox").requireEnabled().requireEditable();
        window.textBox("selectedLastNameTextBox").requireEnabled().requireEditable();
    }
    
    @Test @GUITest
    void testWhenSelectedDoctorFirstAndLastNameTextBoxesAreModifiedThenTheUpdateSelectedButtonShouldBeEnabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedFirstNameTextBox").setText("");
        window.textBox("selectedLastNameTextBox").setText("");
        
        window.textBox("selectedFirstNameTextBox").enterText("Another");
        window.textBox("selectedLastNameTextBox").enterText("Doctor's name");
        
        window.button("updateButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenSelectedDoctorFirstAndLastNameTextBoxesAreEmptyOrEqualToTheCurrentDoctorsValuesThenUpdateButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedFirstNameTextBox").setText("");
        window.textBox("selectedLastNameTextBox").setText("");
        window.textBox("selectedFirstNameTextBox").enterText("Another");
        window.textBox("selectedLastNameTextBox").enterText(" ");
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedFirstNameTextBox").setText("");
        window.textBox("selectedLastNameTextBox").setText("");
        window.textBox("selectedFirstNameTextBox").enterText(" ");
        window.textBox("selectedLastNameTextBox").enterText("Name");
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedFirstNameTextBox").setText("");
        window.textBox("selectedLastNameTextBox").setText("");
        window.textBox("selectedFirstNameTextBox").enterText("DOCK");
        window.textBox("selectedLastNameTextBox").enterText("tor");
        window.button("updateButton").requireEnabled();
        
        window.textBox("selectedFirstNameTextBox").setText("");
        window.textBox("selectedLastNameTextBox").setText("");
        window.textBox("selectedFirstNameTextBox").enterText("doc");
        window.textBox("selectedLastNameTextBox").enterText("tor");
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenTheSelectedDoctorShouldBeDeselected() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        window.list("doctorList").requireNoSelection();
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenTheSelectedDoctorsInfoShouldBeCleared() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        
        window.textBox("selectedIdTextBox").requireNotEditable().requireText("");
        window.textBox("selectedFirstNameTextBox").requireNotEditable().requireText("");
        window.textBox("selectedLastNameTextBox").requireNotEditable().requireText("");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenTheEditDoctorCheckBoxShouldBeDeselected() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();

        window.checkBox("editDoctor").requireSelected();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        window.button("updateButton").click();
        window.checkBox("editDoctor").requireNotSelected();
    }
    
    @Test @GUITest
    void testWhenEditDoctorIsDeselectedWhileUpdateIsEnabledThenUpdateShouldBeDisabled() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        window.button("updateButton").requireEnabled();
        
        window.checkBox("editDoctor").click();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        
        window.button("addButton").click();
        
        window.label("infoLabel").requireText("Adding Doctor...");
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        
        window.label("infoLabel").requireText("Deleting Doctor...");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        
        window.button("updateButton").click();
        
        window.label("infoLabel").requireText("Updating Doctor...");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenItShouldKeepUpdatedInfoInSelectedTextBoxesAndDoctorSelectedInTheList() {
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel()
                .addElement(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        
        window.button("updateButton").click();
        
        window.list("doctorList").requireSelection(0);
        window.textBox("selectedIdTextBox").requireText("doctor_id");
        window.textBox("selectedFirstNameTextBox").requireText("docextension");
        window.textBox("selectedLastNameTextBox").requireText("tor");
    }
    
    @Test @GUITest
    void testShowAllDoctorsShouldAddEachDoctorsDescriptionToTheList() {
        Doctor d1 = Doctor.createDoctor(Id.createId("doctor_1"), "Doctor", "One");
        Doctor d2 = Doctor.createDoctor(Id.createId("doctor_2"), "Doktor", "Two");
        
        view.showAllDoctors(Arrays.asList(d1, d2));
        
        String[] listContents = window.list("doctorList").contents();
        assertThat(listContents)
            .containsExactlyInAnyOrder(d1.toString(), d2.toString());
    }
    
    @Test @GUITest
    void testShowErrorDuplicateDoctorShouldShowMessageInErrorLabel() {
        Doctor duplicatedDoctor = Doctor.createDoctor(Id.createId("doctor_id"), "duplicated", "doctor");
        
        view.showErrorDuplicateDoctor(duplicatedDoctor);
        
        window.label("errorLabel").requireText("A Doctor with id doctor_id already exists");
    }
    
    @Test @GUITest
    void testShowErrorDoctorNotFoundShouldShowMessageInErrorLabel() {
        Doctor notFound = Doctor.createDoctor(Id.createId("doctor_id"), "not", "found");
        
        view.showErrorDoctorNotFound(notFound);
        
        window.label("errorLabel").requireText("No Doctor with id doctor_id was found");
    }
    
    @Test @GUITest
    void testDoctorAddedShouldAddTheDoctorToTheListShowInfoMessageAndResetErrorLabel() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor");
        
        view.doctorAdded(Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
        
        assertThat(window.list("doctorList").contents())
            .containsExactly(doctor.toString());
        window.label("infoLabel").requireText("Doctor added!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testDoctorRemovedShouldRemoveTheDoctorToTheListShowInfoMessageAndResetErrorLabel() {
        Doctor doctor1 = Doctor.createDoctor(Id.createId("doctor_1"), "Doc", "Tor");
        Doctor doctor2 = Doctor.createDoctor(Id.createId("doctor_2"), "Dok", "Ter");
        
        GuiActionRunner.execute(() -> {
            DefaultListModel<Doctor> dlm = view.getDoctorListModel();
            dlm.addElement(doctor1);
            dlm.addElement(doctor2);
        });
        
        view.doctorRemoved(Doctor.createDoctor(Id.createId("doctor_2"), "Dok", "Ter"));
        
        assertThat(window.list("doctorList").contents())
            .containsExactly(doctor1.toString());
        window.label("infoLabel").requireText("Doctor removed!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testDoctorUpdateShouldUpdateTheDoctorInTheListShowInfoMessageAndResetErrorLabel() {
        Doctor oldDoctor = Doctor.createDoctor(Id.createId("doctor_id"), "Old", "Doctor");
        Doctor newDoctor = Doctor.createDoctor(Id.createId("doctor_id"), "New", "Doktor");
        
        GuiActionRunner.execute(() -> 
            view.getDoctorListModel().addElement(oldDoctor));
        
        view.doctorUpdated(
                Doctor.createDoctor(Id.createId("doctor_id"), "Old", "Doctor"),
                Doctor.createDoctor(Id.createId("doctor_id"), "New", "Doktor"));
        
        assertThat(window.list("doctorList").contents())
            .containsExactly(newDoctor.toString());
        window.label("infoLabel").requireText("Doctor updated!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testWhenDoctorIsSelectedAndEditIsTickedThenChangingSelectionShouldRemoveTheTickAndReenableDeleteButton() {
        GuiActionRunner.execute(() -> {
            DefaultListModel<Doctor> listModel = view.getDoctorListModel();
            listModel.addElement(Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"));
            listModel.addElement(Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        });
        window.list("doctorList").selectItem(0);
        
        window.checkBox("editDoctor").click();
        window.button("deleteButton").requireDisabled();

        window.list("doctorList").selectItem(1);
        
        window.checkBox("editDoctor").requireEnabled().requireNotSelected();
        window.button("deleteButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenItShouldDelegateToPresenterAddDoctor() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .addDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenItShouldDelegateToPresenterRemoveDoctor() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        GuiActionRunner.execute(() -> {
            view.getDoctorListModel()
                .addElement(doctor);
        });
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .removeDoctor(doctor));
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenItShouldDelegateToPresenterUpdateDoctor() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        GuiActionRunner.execute(() -> {
            view.getDoctorListModel()
                .addElement(doctor);
        });
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText(" exte");
        window.textBox("selectedLastNameTextBox").enterText(" nsion");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .updateDoctor(
                    doctor,
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc exte", "tor nsion")));
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedAndIdCreationFailsThenItDoesNotDelegateToPresenterAndAnErrorMessageIsShown() {
        window.textBox("idTextBox").enterText("invalid-id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        
        window.button("addButton").click();
        
        window.label("errorLabel").requireText("Id contains invalid value: Letters, digits, and underscores only");
        verifyNoInteractions(presenter);
    }
}
