package org.duckdns.whocaresleft.mvp.swing.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Map;
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
import org.duckdns.whocaresleft.transaction.mariadb.MariaTransactionManager;
import org.duckdns.whocaresleft.view.swing.SwingDepartmentView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests between SwingDepartmentView, DepartmentPresenter,"
    + "and MariaTransactionManager, with the goal of verifying the MVP architecture interaction")
class DepartmentMVPSwingMariaIT {
    
    private static final int TIMEOUT = 15;
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    
    private SwingDepartmentView view;
    private TransactionManager transactionManager;
    private DepartmentPresenter presenter;

    private static EntityManagerFactory emf;
    private FrameFixture window ;
    
    @BeforeAll
    static void setupEntityManagerFactory() {
        FailOnThreadViolationRepaintManager.install();
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("maria_repository_it", properties);
    }
    
    @AfterAll
    static void teardownEntityManagerFactory() {
        if (emf != null)
            emf.close();
    }
    
    @BeforeEach
    void setup() {
        transactionManager = new MariaTransactionManager(emf);
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
    void teardown()  {
        if (window != null)
            window.cleanUp();
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
        window.checkBox("editDepartment").click();
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