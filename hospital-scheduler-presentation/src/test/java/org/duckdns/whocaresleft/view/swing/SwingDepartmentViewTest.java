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
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("UI tests for SwingDepartmentView")
class SwingDepartmentViewTest {
    
    private static final int TIMEOUT = 15;
    
    @Mock
    private DepartmentPresenter presenter;
    private AutoCloseable closeable;
    
    private FrameFixture window;
    private SwingDepartmentView view;
    
    @BeforeAll
    static void setupOne() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        GuiActionRunner.execute(() -> {
            view = new SwingDepartmentView();
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
        window.textBox("idTextBox").requireEnabled().requireEditable();
        window.label("nameLabel");
        window.textBox("nameTextBox").requireEnabled().requireEditable();
        window.button("addButton");
        
        window.list("departmentList");
        
        window.label("selectedDepartmentLabel");
        window.checkBox("editDepartment").requireDisabled();
        
        window.label("selectedIdLabel");
        window.textBox("selectedIdTextBox").requireEnabled().requireNotEditable();
        window.label("selectedNameLabel");
        window.textBox("selectedNameTextBox").requireEnabled().requireNotEditable();
        
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
        
        window.label("infoLabel").requireText(" ");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testWhenIdAndNameAreNotEmptyThenAddButtonShouldBeEnabled() {
        window.textBox("idTextBox").enterText("e_r");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenEitherIdOrNameAreEmptyThenAddButtonShouldBeDisabled() {
        JTextComponentFixture idTextBox = window.textBox("idTextBox");
        JTextComponentFixture nameTextBox = window.textBox("nameTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        idTextBox.enterText("e_r");
        nameTextBox.enterText(" ");
        addButton.requireDisabled();
        
        idTextBox.setText("");
        nameTextBox.setText("");
        
        idTextBox.enterText(" ");
        nameTextBox.enterText("Emergency Room");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenTextFieldsShouldBeClearedAndAddButtonDisabled() {
        JTextComponentFixture idTextBox = window.textBox("idTextBox");
        JTextComponentFixture nameTextBox = window.textBox("nameTextBox");
        JButtonFixture addButton = window.button("addButton");
        
        idTextBox.enterText("e_r");
        nameTextBox.enterText("Emergency Room");
        addButton.requireEnabled();
        
        addButton.click();
        
        idTextBox.requireText("");
        nameTextBox.enterText("");
        addButton.requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDepartmentIsSelectedThenTheSelectedDepartmentTextFieldsShouldContainSuchValues() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.textBox("selectedIdTextBox").requireText("er");
        window.textBox("selectedNameTextBox").requireText("Emergency Room");
    }
    
    @Test @GUITest
    void testWhenDepartmentIsSelectedThenTheDeleteButtonShouldBeEnabled() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        
        window.button("deleteButton").requireDisabled();
        window.list("departmentList").selectItem(0);
        window.button("deleteButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenDepartmentIsSelectedThenTheEditCheckBoxShouldBeEnabled() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        
        window.checkBox("editDepartment").requireDisabled();
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenEditDepartmentCheckBoxIsTickedThenTheDeleteButtonShouldBeDisabled() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.checkBox("editDepartment").click();
        window.button("deleteButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenEditDepartmentCheckBoxIsTickedThenTheSelectedNameTextBoxShouldBeEditable() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.checkBox("editDepartment").click();
        window.textBox("selectedIdTextBox").requireEnabled().requireNotEditable();
        window.textBox("selectedNameTextBox").requireEnabled().requireEditable();
    }
    
    @Test @GUITest
    void testWhenSelectedDepartmentNameTextBoxIsModifiedThenTheUpdateSelectedButtonShouldBeEnabled() {
        GuiActionRunner.execute(() ->
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedNameTextBox").setText("");
        window.textBox("selectedNameTextBox").enterText("New Emergency Room");
        
        window.button("updateButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenSelectedDepartmentNameTextBoxeIsEmptyOrEqualToTheCurrentNameThenUpdateButtonShouldBeDisabled() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        
        window.button("updateButton").requireDisabled();
        
        window.textBox("selectedNameTextBox").setText("");
        window.textBox("selectedNameTextBox").enterText(" ");
        window.button("updateButton").requireDisabled();

        window.textBox("selectedNameTextBox").setText("");
        window.textBox("selectedNameTextBox").enterText("New Emergency Room");
        window.button("updateButton").requireEnabled();

        window.textBox("selectedNameTextBox").setText("");
        window.textBox("selectedNameTextBox").enterText("Emergency Room");
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenTheSelectedDepartmentShouldBeDeselected() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        window.list("departmentList").requireNoSelection();
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenTheSelectedDepartmentsInfoShouldBeCleared() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        
        window.textBox("selectedIdTextBox").requireNotEditable().requireText("");
        window.textBox("selectedNameTextBox").requireNotEditable().requireText("");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThanTheEditDepartmentCheckBoxShouldBeDeselected() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();

        window.checkBox("editDepartment").requireSelected();
        window.textBox("selectedNameTextBox").enterText("-new");
        window.button("updateButton").click();
        window.checkBox("editDepartment").requireNotSelected();
    }
    
    @Test @GUITest
    void testWhenEditDoctorIsDeselectedWhileUpdateIsEnabledThenUpdateShouldBeDisabled() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText("-new");
        window.button("updateButton").requireEnabled();

        window.checkBox("editDepartment").click();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        window.label("infoLabel").requireText("Adding Department...");
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        
        window.label("infoLabel").requireText("Deleting Department...");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenInfoLabelShouldShowActionMessage() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        
        window.textBox("selectedNameTextBox").enterText("-new");
        window.button("updateButton").click();
        
        window.label("infoLabel").requireText("Updating Department...");
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenItShouldKeepUpdatedInfoInSelectedDepartmentTextBoxAndListSelection() {
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(Department.createDepartment(Id.createId("er"), "Emergency Room")));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        
        window.textBox("selectedNameTextBox").enterText("-new");
        window.button("updateButton").click();
        
        window.list("departmentList").requireSelection(0);
        window.textBox("selectedIdTextBox").requireText("er");
        window.textBox("selectedNameTextBox").requireText("Emergency Room-new");
    }
    
    @Test @GUITest
    void testShowAllDepartmentsShouldAddEachDepartmentDescriptionToTheList() {
        Department d1 = Department.createDepartment(Id.createId("er"), "Emergency Room");
        Department d2 = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        
        view.showAllDepartments(Arrays.asList(d1, d2));
        
        String[] listContents = window.list("departmentList").contents();
        assertThat(listContents)
            .containsExactlyInAnyOrder(d1.toString(), d2.toString());
    }
    
    @Test @GUITest
    void testShowErrorDuplicateDepartmentShouldShowMessageInErrorLabel() {
        Department duplicated = Department.createDepartment(Id.createId("er"), "Old ER");
        
        view.showErrorDuplicateDepartment(duplicated);
    }
}
