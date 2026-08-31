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
    void testWhenEditDepartmentIsDeselectedWhileUpdateIsEnabledThenUpdateShouldBeDisabled() {
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
        
        window.label("errorLabel").requireText("A Department with id er already exists");
    }
    
    @Test @GUITest
    void testShowErrorDepartmentNotFoundShouldShowMessageInErrorLabel() {
        Department notFound = Department.createDepartment(Id.createId("er"), "Not Found");
        
        view.showErrorDepartmentNotFound(notFound);
        
        window.label("errorLabel").requireText("No Department with id er was found");
    }
    
    @Test @GUITest
    void testDepartmentAddedShouldAddTheDepartmentToTheListShowInfoMessageAndClearErrorLabel() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency room");
        
        view.departmentAdded(department);
        
        assertThat(window.list("departmentList").contents())
            .containsExactly(department.toString());
        window.label("infoLabel").requireText("Department added!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testDepartmentRemovedShouldRemoveTheDepartmentFromTheListShowInfoMessageAndClearErrorLabel() {
        Department d1 = Department.createDepartment(Id.createId("er"), "Emergency room");
        Department d2 = Department.createDepartment(Id.createId("sr"), "Surgery room");
        
        GuiActionRunner.execute(() -> {
            DefaultListModel<Department> dlm = view.getDepartmentListModel();
            dlm.addElement(d1);
            dlm.addElement(d2);
        });
        
        view.departmentRemoved(Department.createDepartment(Id.createId("sr"), "Surgery room"));
        
        assertThat(window.list("departmentList").contents())
            .containsExactly(d1.toString());
        window.label("infoLabel").requireText("Department removed!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testDepartmentUpdatedShouldRemoveTheDepartmentInTheListShowInfoMessageAndClearErrorLabel() {
        Department oldDepartment = Department.createDepartment(Id.createId("er"), "Emergency room");
        Department newDepartment = Department.createDepartment(Id.createId("er"), "New Emergency room");
        
        GuiActionRunner.execute(() -> {
            view.getDepartmentListModel().addElement(oldDepartment);
        });
        
        view.departmentUpdated(
            Department.createDepartment(Id.createId("er"), "Emergency room"),
            Department.createDepartment(Id.createId("er"), "New Emergency room"));
        
        assertThat(window.list("departmentList").contents())
            .containsExactly(newDepartment.toString());
        window.label("infoLabel").requireText("Department updated!");
        window.label("errorLabel").requireText(" ");
    }
    
    @Test @GUITest
    void testWhenDepartmentIsSelectedAndEditIsTickedThenChangingSelectionShouldRemoveTheTickAndReEnableDeleteButton() {
        GuiActionRunner.execute(() -> {
            DefaultListModel<Department> listModel = view.getDepartmentListModel();
            listModel.addElement(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            listModel.addElement(Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        });
        window.list("departmentList").selectItem(0);
        
        window.checkBox("editDepartment").click();
        window.button("deleteButton").requireDisabled();

        window.list("departmentList").selectItem(1);
        
        window.checkBox("editDepartment").requireEnabled().requireNotSelected();
        window.button("deleteButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenItShouldDelegateToPresenterAddDepartment() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .addDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room")));
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedItShouldDelegateToPresenterRemoveDepartment() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> {
            view.getDepartmentListModel()
                .addElement(department);
        });
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .removeDepartment(department));
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedItShouldDelegateToPresenterUpdateDepartment() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> {
            view.getDepartmentListModel()
                .addElement(department);
        });
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText(" new");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            verify(presenter)
                .updateDepartment(
                    department,
                    Department.createDepartment(Id.createId("er"), "Emergency Room new")));
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsEnabledAndEditIsFirstDeselectedAndReselectedThenUpdateShouldBeEnableAgain() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> {
            view.getDepartmentListModel()
                .addElement(department);
        });
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText(" new");
        
        window.button("updateButton").requireEnabled();
        window.checkBox("editDepartment").click();
        window.button("updateButton").requireDisabled();
        window.checkBox("editDepartment").click();
        window.button("updateButton").requireEnabled();
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedAndIdCreationFailsThenItDoesNotDelegateToPresenterAddDepartment() {
        window.textBox("idTextBox").enterText("invalid-id");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        window.label("errorLabel").requireText("Id contains invalid value: Letters, digits, and underscores only");
        verifyNoInteractions(presenter);
    }
    
    @Test @GUITest
    void testWhenAddButtonIsPressedThenUIIsDisabled() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        window.textBox("idTextBox").requireNotEditable();
        window.textBox("nameTextBox").requireNotEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireDisabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDepartmentAddedIsCalledThenUIIsEnabledAgain() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        view.departmentAdded(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        
        window.textBox("idTextBox").requireEditable();
        window.textBox("nameTextBox").requireEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireEnabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenShowErrorDuplicateDepartmentIsCalledThenItShouldReEnableTheUI() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        view.showErrorDuplicateDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        
        window.textBox("idTextBox").requireEditable();
        window.textBox("nameTextBox").requireEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireEnabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDeleteButtonIsPressedThenUIIsDisabled() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(department));
        
        window.list("departmentList").selectItem(0);
        window.button("deleteButton").click();
        
        window.textBox("idTextBox").requireNotEditable();
        window.textBox("nameTextBox").requireNotEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireDisabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDepartmentRemovedIsCalledThenUIIsEnabledAgain() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(department));
        
        window.list("departmentList").selectItem(0);
        window.button("deleteButton").click();
        
        view.departmentRemoved(department);
        
        window.textBox("idTextBox").requireEditable();
        window.textBox("nameTextBox").requireEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireEnabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenShowErrorDepartmentNotFoundIsCalledThenItShouldReEnableTheUI() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(department));
        
        window.list("departmentList").selectItem(0);
        window.button("deleteButton").click();
        
        view.showErrorDepartmentNotFound(department);
        
        window.textBox("idTextBox").requireEditable();
        window.textBox("nameTextBox").requireEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireEnabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenUpdateButtonIsPressedThenUIIsDisabled() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(department));
        
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText("-new");
        
        window.button("updateButton").click();
        
        window.textBox("idTextBox").requireNotEditable();
        window.textBox("nameTextBox").requireNotEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireDisabled();
        window.checkBox("editDepartment").requireDisabled();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireDisabled();
        window.button("updateButton").requireDisabled();
    }
    
    @Test @GUITest
    void testWhenDepartmentUpdatedIsCalledThenUIIsEnabledAgain() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        GuiActionRunner.execute(() -> 
            view.getDepartmentListModel()
                .addElement(department));
        
        window.list("departmentList").selectItem(0);
        window.textBox("selectedNameTextBox").enterText("-new");
        
        window.button("updateButton").click();
        
        view.departmentUpdated(
            department,
            Department.createDepartment(Id.createId("er"), "Emergency Room-new"));
        
        window.textBox("idTextBox").requireEditable();
        window.textBox("nameTextBox").requireEditable();
        window.button("addButton").requireDisabled();
        window.list("departmentList").requireEnabled();
        window.checkBox("editDepartment").requireEnabled().requireNotSelected();
        window.textBox("selectedNameTextBox").requireNotEditable();
        window.button("deleteButton").requireEnabled();
        window.button("updateButton").requireDisabled();
    }
}
