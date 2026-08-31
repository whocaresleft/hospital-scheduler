package org.duckdns.whocaresleft.mvp.swing.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

@Testcontainers @DisplayName("Integration tests between SwingDepartmentView, DepartmentPresenter,"
    + "and MongoTransactionManager, with the goal of verifying the MVP architecture interaction")
public class DepartmentMVPSwingMongoIT {
    
    private static final int TIMEOUT = 15;
    
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
        
        transactionManager = new MongoTransactionManager(client, db);
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
    
    
    @Test
    void testAddDepartment() {
        window.textBox("idTextBox").enterText("er");
        window.textBox("nameTextBox").enterText("Emergency Room");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            Department found =
                transactionManager.doInTransaction(provider ->
                    provider.getDepartmentRepository().findById(Id.createId("er")));
            
            assertThat(found)
                .isEqualTo(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        });
    }
    
    @Test
    void testDeleteDepartment() {
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository().save(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            return null;
        });
        
        presenter.allDepartments();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            window.list("departmentList").requireItemCount(1));
        
        window.list("departmentList").selectItem(0);
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            Department found =
                transactionManager.doInTransaction(provider ->
                    provider.getDepartmentRepository().findById(Id.createId("er")));
            
            assertThat(found).isNull();
        });
    }
    
    @Test
    void testUpdateDepartment() {
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository().save(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            return null;
        });
        
        presenter.allDepartments();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            window.list("departmentList").requireItemCount(1));
        
        window.list("departmentList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedNameTextBox").enterText("-new");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            Department found =
                transactionManager.doInTransaction(provider ->
                    provider.getDepartmentRepository().findById(Id.createId("er")));
            
            assertThat(found)
                .isEqualTo(Department.createDepartment(Id.createId("er"), "Emergency Room-new"));
        });
    }
}
