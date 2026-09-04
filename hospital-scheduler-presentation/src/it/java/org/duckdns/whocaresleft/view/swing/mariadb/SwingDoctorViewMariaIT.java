package org.duckdns.whocaresleft.view.swing.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mariadb.MariaTransactionManager;
import org.duckdns.whocaresleft.view.swing.SwingDoctorView;
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

@Testcontainers @DisplayName("Integration tests between SwingDoctorView, DoctorPresenter, and MariaTransactionManager")
class SwingDoctorViewMariaIT {
    
    private static final int TIMEOUT = 15;
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.11");
    
    private SwingDoctorView view;
    private TransactionManager transactionManager;
    private DoctorPresenter presenter;

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
            DoctorRepository repository = provider.getDoctorRepository();
            
            for (Doctor d : repository.findAll())
                repository.delete(d.getId());
            
            return null;
        });
        
        GuiActionRunner.execute(() -> {
            view = new SwingDoctorView();
            presenter = new DoctorPresenter(transactionManager, view);
            view.setPresenter(presenter);
            view.showAllDoctors(Arrays.asList());
            return view;
        });
        window = Containers.showInFrame(view);
    }
    
    @AfterEach
    void teardown()  {
        if (window != null)
            window.cleanUp();
    }
    
    @Test @GUITest
    void testAllDoctors() {
        Doctor d1 = Doctor.createDoctor(Id.createId("doc1"), "doc", "one");
        Doctor d2 = Doctor.createDoctor(Id.createId("doc2"), "dok", "two");
        transactionManager.doInTransaction(provider -> {
            DoctorRepository repository = provider.getDoctorRepository();
            repository.save(d1);
            repository.save(d2);
            return null;
        });
        
        presenter.allDoctors();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list("doctorList").contents())
                .containsExactlyInAnyOrder(SwingDoctorView.displayDoctor(d1), SwingDoctorView.displayDoctor(d2)));
    }
    
    @Test @GUITest
    void testAddButtonSuccess() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("doctorList").contents())
                .containsExactly(
                    SwingDoctorView.displayDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor")));
            
            window.label("infoLabel").requireText("Doctor added!");
        });
    }
    
    @Test @GUITest
    void testAddButtonError() {
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(Doctor.createDoctor(Id.createId("doctor_id"), "ORIGINAL", "DOCTOR"));
            return null;
        });
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("DUPLICATED");
        window.textBox("lastNameTextBox").enterText("ONE");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("doctorList").contents())
                .containsExactly(
                    SwingDoctorView.displayDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "ORIGINAL", "DOCTOR")));
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("A Doctor with id doctor_id already exists");
        });
    }
    
    @Test @GUITest
    void testDeleteButtonSuccess() {
        presenter.addDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("doctorList").contents()).isEmpty();
            
            window.label("infoLabel").requireText("Doctor removed!");
        });
    }
    
    @Test @GUITest
    void testDeleteButtonError() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        
        GuiActionRunner.execute(() -> view.getDoctorListModel().addElement(doctor));
        window.list("doctorList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list().contents()).isEmpty();
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("No Doctor with id doctor_id was found");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonSuccess() {
        presenter.addDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        window.textBox("selectedLastNameTextBox").enterText("extension");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("doctorList").contents())
                .containsExactly(
                    SwingDoctorView.displayDoctor(Doctor.createDoctor(Id.createId("doctor_id"), "docextension", "torextension")));
            
            window.label("infoLabel").requireText("Doctor updated!");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonError() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        
        GuiActionRunner.execute(() -> view.getDoctorListModel().addElement(doctor));
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("extension");
        window.textBox("selectedLastNameTextBox").enterText("extension");
        
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list().contents()).isEmpty();
            
            window.label("infoLabel").requireText(" ");
            window.label("errorLabel").requireText("No Doctor with id doctor_id was found");
        });
    }
}
