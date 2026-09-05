package org.duckdns.whocaresleft.mvp.swing.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

@Testcontainers @DisplayName("Integration tests between SwingShiftView, ShiftPresenter,"
        + "and MongoTransactionManager, with the goal of verifying the MVP architecture interaction")
class ShiftMVPSwingMongoIT {
    
    private static final int TIMEOUT = 25;
    private static final String DOCTOR_COLLECTION = "doctor";
    private static final String DEPARTMENT_COLLECTION = "department";
    private static final String SHIFT_COLLECTION = "shift";
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    
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
    
    @Test 
    void testAddShift() {
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
            provider.getDepartmentRepository().save(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            return null;
        });
        
        window.textBox("doctorIdTextBox").enterText("doctor_id");
        window.textBox("departmentIdTextBox").enterText("er");
        window.textBox("dateTextBox").enterText("24/07/2026");
        window.textBox("startTimeTextBox").enterText("08:00");
        window.textBox("endTimeTextBox").enterText("09:00");
        
        window.button("addButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Shift> foundShifts = transactionManager.doInTransaction(provider ->
                provider.getShiftRepository().findByDoctorId(Id.createId("doctor_id")));
            
            assertThat(foundShifts)
                .containsExactly(
                    Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
        });
    }
    
    @Test
    void testDeleteShift() {
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(
                Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
            provider.getDepartmentRepository().save(
                Department.createDepartment(Id.createId("er"), "Emergency Room"));
            provider.getShiftRepository().save(
                Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
            return null;
        });
        
        presenter.allShifts();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> 
            window.list("shiftList").requireItemCount(1));
        
        window.list("shiftList").selectItem(0);
        window.button("deleteButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Shift> foundShifts = transactionManager.doInTransaction(provider ->
                provider.getShiftRepository().findByDoctorId(Id.createId("doctor_id")));
            
            assertThat(foundShifts).isEmpty();
        });
    }
    
    @Test
    void testUpdateShift() {
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(
                Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
            provider.getDepartmentRepository().save(
                Department.createDepartment(Id.createId("er"), "Emergency Room"));
            provider.getDepartmentRepository().save(
                    Department.createDepartment(Id.createId("sr"), "Surgery Room"));
            provider.getShiftRepository().save(
                Shift.createShift(Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
            return null;
        });
        
        presenter.allShifts();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> 
            window.list("shiftList").requireItemCount(1));
        
        window.list("shiftList").selectItem(0);
        window.checkBox("editShift").click();
        window.textBox("selectedDepartmentIdTextBox").setText("").enterText("sr");
        window.button("updateButton").click();
        
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Shift> foundShifts = transactionManager.doInTransaction(provider ->
                provider.getShiftRepository().findByDoctorId(Id.createId("doctor_id")));
            
            assertThat(foundShifts)
                .containsExactly(
                    Shift.createShift(Id.createId("doctor_id"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_00));
        });
    }
}
