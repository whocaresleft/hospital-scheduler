package org.duckdns.whocaresleft.view.swing.mongodb;

import java.time.LocalDate;
import java.time.LocalTime;
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
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.swing.SwingShiftView;
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

@Testcontainers @DisplayName("Integration tests between SwingShiftView, ShiftPresenter, and MongoTransactionManager")
class SwingShiftViewMongoIT {
    
    private static final int TIMEOUT = 25;
    private static final String DOCTOR_COLLECTION = "doctor";
    private static final String DEPARTMENT_COLLECTION = "department";
    private static final String SHIFT_COLLECTION = "shift";
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private SwingShiftView view;
    private ShiftPresenter presenter;
    private TransactionManager transactionManager;
    
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
            DoctorRepository doctorRepository = provider.getDoctorRepository();
            DepartmentRepository departmentRepository = provider.getDepartmentRepository();
            ShiftRepository shiftRepository = provider.getShiftRepository();
            
            for (Shift s : shiftRepository.findAll()) shiftRepository.delete(s);
            for (Department d : departmentRepository.findAll()) departmentRepository.delete(d.getId());
            for (Doctor d : doctorRepository.findAll()) doctorRepository.delete(d.getId());
            
            return null;
        });
        
        GuiActionRunner.execute(() -> {
            view = new SwingShiftView();
            presenter = new ShiftPresenter(transactionManager, view);
            view.setPresenter(presenter);
            view.showAllShifts(Arrays.asList());
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
    void testAllShifts() {
        Doctor doc1 = Doctor.createDoctor(Id.createId("doc1"), "doc", "one");
        Doctor doc2 = Doctor.createDoctor(Id.createId("doc2"), "dok", "two");
        Department dep = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        Shift s1 = Shift.createShift(
            Id.createId("doc1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift s2 = Shift.createShift(
                Id.createId("doc2"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_30);
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(doc1);
            provider.getDoctorRepository().save(doc2);
            provider.getDepartmentRepository().save(dep);
            
            provider.getShiftRepository().save(s1);
            provider.getShiftRepository().save(s2);
            return null;
        });
        
        presenter.allShifts();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list("shiftList").contents())
                .containsExactlyInAnyOrder(s1.toString(), s2.toString()));
    }
    
    @Test @GUITest
    void testAddButtonSuccess() {
        Doctor doc = Doctor.createDoctor(Id.createId("doc"), "doc", "one");
        Department dep = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(doc);
            provider.getDepartmentRepository().save(dep);
            return null;
        });
        
        window.textBox("doctorIdTextBox").enterText("doc");
        window.textBox("departmentIdTextBox").enterText("sr");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents())
                .containsExactly(Shift.createShift(
                    Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00).toString());
            
            window.label("infoLabel").requireText("Shift added!");
        });
    }
    
    @Test @GUITest
    void testAddButtonError() {
        Doctor doc = Doctor.createDoctor(Id.createId("doc"), "doc", "one");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(doc);
            return null;
        });
        
        window.textBox("doctorIdTextBox").enterText("doc");
        window.textBox("departmentIdTextBox").enterText("sr");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents())
                .isEmpty();
            
            window.label("errorLabel").requireText("No Department with id sr was found");
        });
    }
    
    @Test @GUITest
    void testRemoveButtonSuccess() {
        Doctor doc = Doctor.createDoctor(Id.createId("doc"), "doc", "one");
        Department dep = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(doc);
            provider.getDepartmentRepository().save(dep);
            return null;
        });
        presenter.addShift(Shift.createShift(
            Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
        window.list("shiftList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents())
                .isEmpty();
            
            window.label("infoLabel").requireText("Shift removed!");
        });
    }
    
    @Test @GUITest
    void testRemoveButtonError() {
        Shift shift =
            Shift.createShift(Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
        GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
        window.list("shiftList").selectItem(0);
        
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents()).isEmpty();
            
            window.label("errorLabel").requireText("No Shift matching (doc-sr), 2026-07-24: (08:00-09:00) was found");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonSuccess() {
        Doctor doc = Doctor.createDoctor(Id.createId("doc"), "doc", "one");
        Department dep = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(doc);
            provider.getDepartmentRepository().save(dep);
            return null;
        });
        presenter.addShift(Shift.createShift(
            Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
        window.list("shiftList").selectItem(0);
        window.checkBox("editShift").click();
        
        window.textBox("selectedStartTimeTextBox").setText("").enterText("08:30");
        window.textBox("selectedEndTimeTextBox").setText("").enterText("09:30");
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents())
                .containsExactly(
                    Shift.createShift(Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_30, TIME_09_30).toString());
            
            window.label("infoLabel").requireText("Shift updated!");
        });
    }
    
    @Test @GUITest
    void testUpdateButtonError() {
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(
                Doctor.createDoctor(Id.createId("doc"), "doc", "one"));
            provider.getDepartmentRepository().save(
                Department.createDepartment(Id.createId("sr"), "Surgery Room"));
            return null;
        });
        Shift shift =
            Shift.createShift(Id.createId("doc"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00);
        GuiActionRunner.execute(() -> view.getShiftListModel().addElement(shift));
        window.list("shiftList").selectItem(0);
        
        window.checkBox("editShift").click();
        window.textBox("selectedStartTimeTextBox").setText("").enterText("08:30");
        window.textBox("selectedEndTimeTextBox").setText("").enterText("09:30");
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list("shiftList").contents()).isEmpty();
            
            window.label("errorLabel").requireText("No Shift matching (doc-sr), 2026-07-24: (08:00-09:00) was found");
        });
    }
}
