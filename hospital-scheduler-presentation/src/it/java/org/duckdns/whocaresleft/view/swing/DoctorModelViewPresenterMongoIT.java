package org.duckdns.whocaresleft.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.mongodb.MongoDoctorRepository;
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

@Testcontainers @DisplayName("Integration tests for SwingDoctorView with DoctorPresenter and MongoDoctorRepository for testing actual MVP interactions")
class DoctorModelViewPresenterMongoIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private FrameFixture window;
    private SwingDoctorView view;
    private DoctorPresenter presenter;
    private MongoDoctorRepository repository;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getConnectionString());
        
        repository = new MongoDoctorRepository(client);
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
        client.close();
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
