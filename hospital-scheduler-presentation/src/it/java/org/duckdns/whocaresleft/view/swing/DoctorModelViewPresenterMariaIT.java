package org.duckdns.whocaresleft.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.mariadb.MariaDoctorRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests for SwingDoctorView with DoctorPresenter and MariaDoctorRepository for testing actual MVP interactions")
class DoctorModelViewPresenterMariaIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager entityManager;
    private FrameFixture window;
    private SwingDoctorView view;
    private DoctorPresenter presenter;
    private MariaDoctorRepository repository;
    
    @BeforeAll
    static void setupEntityManagerFactory() {
        FailOnThreadViolationRepaintManager.install();
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("maria_doctor_presenter_it", properties);
    }
    
    @AfterAll
    static void teardownEntityManagerFactory() {
        if (emf != null)
            emf.close();
    }
    
    @BeforeEach
    void setup() {
        entityManager = emf.createEntityManager();
        
        repository = new MariaDoctorRepository(entityManager);
        for (Doctor d : repository.findAll())
            repository.delete(d.getId());
        
        GuiActionRunner.execute(() -> {
            view = new SwingDoctorView();
            presenter = new DoctorPresenter(repository, view);
            view.setPresenter(presenter);
            return view;
        });
        window = Containers.showInFrame(view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        if (entityManager.isOpen()) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            entityManager.close();
        }
        if (window != null)
            window.cleanUp();
    }
    
    @Test
    void testAddDoctor() {
        window.textBox("idTextBox").enterText("doctor_id");
        window.textBox("firstNameTextBox").enterText("doc");
        window.textBox("lastNameTextBox").enterText("tor");
        
        window.button("addBtn").click();
        
        assertThat(repository.findById(Id.createId("doctor_id")))
            .isEqualTo(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
    }
    
    @Test
    void testDeleteDoctor() {
        repository.save(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        
        GuiActionRunner.execute(() -> presenter.allDoctors());
        
        window.list("doctorList").selectItem(0);
        window.button("deleteBtn").click();
        
        assertThat(repository.findById(Id.createId("doctor_id")))
            .isNull();
    }
    
    @Test
    void testUpdateDoctor() {
        repository.save(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        
        GuiActionRunner.execute(() -> presenter.allDoctors());
        
        window.list("doctorList").selectItem(0);
        window.checkBox("editDoctor").click();
        window.textBox("selectedFirstNameTextBox").enterText("ker");
        window.textBox("selectedLastNameTextBox").enterText("rent");
        
        window.button("updateBtn").click();
        
        assertThat(repository.findById(Id.createId("doctor_id")))
            .isEqualTo(Doctor.createDoctor(Id.createId("doctor_id"), "docker", "torrent"));
    }
}
