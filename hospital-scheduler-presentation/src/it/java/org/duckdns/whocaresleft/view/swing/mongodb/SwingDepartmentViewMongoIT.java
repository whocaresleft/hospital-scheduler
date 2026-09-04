package org.duckdns.whocaresleft.view.swing.mongodb;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.swing.SwingDepartmentView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Testcontainers @DisplayName("Integration tests between SwingDepartmentView, DepartmentPresenter, and MongoTransactionManager")
class SwingDepartmentViewMongoIT {
    
    private static final int TIMEOUT = 15;
    private static final String DOCTOR_COLLECTION = "doctor";
    private static final String DEPARTMENT_COLLECTION = "department";
    private static final String SHIFT_COLLECTION = "shift";
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private SwingDepartmentView view;
    private TransactionManager transactionManager;
    private DepartmentPresenter presenter;
    
    private MongoClient client;
    private FrameFixture window;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase db = client.getDatabase("hospital");
        
        transactionManager = new MongoTransactionManager(client, db, DOCTOR_COLLECTION, DEPARTMENT_COLLECTION, SHIFT_COLLECTION);
        transactionManager.doInTransaction(provider -> {
            DepartmentRepository repository = provider.getDepartmentRepository();
            
            for (Department d : repository.findAll())
                repository.delete(d.getId());
            
            return null;
        });
        
        GuiActionRunner.execute(() -> {
            view = new SwingDepartmentView();
            presenter = new DepartmentPresenter(transactionManager, view);
            view.setPresenter(presenter);
            view.showAllDepartments(Arrays.asList());
            return view;
        });
        window = Containers.showInFrame(view);
    }
    
    @AfterEach
    void teardown() {
        if (window != null)
            window.cleanUp();
        client.close();
    }
        
    @Test @GUITest
    void testAllDepartments() {
        Department d1 = Department.createDepartment(Id.createId("er"), "Emergency Room");
        Department d2 = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        transactionManager.doInTransaction(provider -> {
            DepartmentRepository repository = provider.getDepartmentRepository();
            repository.save(d1);
            repository.save(d2);
            return null;
        });
        
        presenter.allDepartments();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list("departmentList").contents())
                .containsExactlyInAnyOrder(SwingDepartmentView.displayDepartment(d1), SwingDepartmentView.displayDepartment(d2)));
    }
    
    @Test @GUITest
    void testAddButtonSuccess() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("departmentList").contents())
                .containsExactly(
                    SwingDepartmentView.displayDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room")));
            
            window.label("infoLabel").requireText("Department added!");
        });
        
    }
    
    @Test @GUITest
    void testAddButtonError() {
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository().save(Department.createDepartment(Id.createId("er"), "Original Emergency Room"));
            return null;
        });
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Duplicated Emergency Room");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("departmentList").contents())
                .containsExactly(
                    SwingDepartmentView.displayDepartment(Department.createDepartment(Id.createId("er"), "Original Emergency Room")));
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("A Department with id er already exists");
        });
    }
    
    @Test @GUITest
    void testDeleteButtonSuccess() {
        presenter.addDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("departmentList").contents()).isEmpty();
            
            window.label("infoLabel").requireText("Department removed!");
        });
    }
    
    @Test @GUITest
    void testDeleteButtonError() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        
        GuiActionRunner.execute(() -> view.getDepartmentListModel().addElement(department));
        window.list("departmentList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list().contents()).isEmpty();
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("No Department with id er was found");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonSuccess() {
        presenter.addDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText("-new");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("departmentList").contents())
                .containsExactly(
                    SwingDepartmentView.displayDepartment(Department.createDepartment(Id.createId("er"), "Emergency Room-new")));
            
            window.label("infoLabel").requireText("Department updated!");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonError() {
        Department department = Department.createDepartment(Id.createId("er"), "Emergency Room");
        
        GuiActionRunner.execute(() -> view.getDepartmentListModel().addElement(department));
        window.list("departmentList").selectItem(0);
        window.checkBox("editDepartment").click();
        window.textBox("selectedNameTextBox").enterText("-new");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list().contents()).isEmpty();
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("No Department with id er was found");
        });
    }
}
