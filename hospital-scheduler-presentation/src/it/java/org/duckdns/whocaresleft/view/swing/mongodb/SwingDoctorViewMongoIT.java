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
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.swing.SwingDoctorView;
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

@Testcontainers @DisplayName("Integration tests between SwingDoctorView, DoctorPresenter, and MongoTransactionManager")
class SwingDoctorViewMongoIT {
    
    private static final int TIMEOUT = 15;
    private static final String DOCTOR_COLLECTION = "doctor";
    private static final String DEPARTMENT_COLLECTION = "department";
    private static final String SHIFT_COLLECTION = "shift";
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private SwingDoctorView view;
    private TransactionManager transactionManager;
    private DoctorPresenter presenter;
    
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
    void teardown() {
        if (window != null)
            window.cleanUp();
        client.close();
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
