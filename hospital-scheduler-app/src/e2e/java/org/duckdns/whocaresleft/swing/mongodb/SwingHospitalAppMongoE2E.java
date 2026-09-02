package org.duckdns.whocaresleft.swing.mongodb;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;

@Testcontainers @DisplayName("End-to-End tests for the SwingHospitalApp, using MongoDB as database backend")
class SwingHospitalAppMongoE2E {
    
    private static final int TIMEOUT = 15;
    private static final String DB_NAME = "test-hospital";
    private static final String DOCTOR_COLLECTION = "test-doctor";
    private static final String DEPARTMENT_COLLECTION = "test-department";
    private static final String SHIFT_COLLECTION = "test-shift";
    
    private static final String DOCTOR_FIXTURE_1_ID = "doctor_1";
    private static final String DOCTOR_FIXTURE_1_FIRST_NAME = "Doc";
    private static final String DOCTOR_FIXTURE_1_LAST_NAME = "Tor";
    private static final String DOCTOR_FIXTURE_2_ID = "doctor_2";
    private static final String DOCTOR_FIXTURE_2_FIRST_NAME = "Dok";
    private static final String DOCTOR_FIXTURE_2_LAST_NAME = "Ter";
    
    private static final String DEPARTMENT_FIXTURE_1_ID = "er";
    private static final String DEPARTMENT_FIXTURE_1_NAME = "Emergency Room";
    private static final String DEPARTMENT_FIXTURE_NON_EXISTENT_ID = "sr";
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private FrameFixture window;
    
    @BeforeAll
    static void setupOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
    
    @BeforeEach
    void setup() {
        String connectionString = mongo.getReplicaSetUrl();
        
        client = MongoClients.create(connectionString);
        
        client.getDatabase(DB_NAME).drop();
        
        addTestDoctorToDatabase(DOCTOR_FIXTURE_1_ID, DOCTOR_FIXTURE_1_FIRST_NAME, DOCTOR_FIXTURE_1_LAST_NAME);
        addTestDoctorToDatabase(DOCTOR_FIXTURE_2_ID, DOCTOR_FIXTURE_2_FIRST_NAME, DOCTOR_FIXTURE_2_LAST_NAME);
        
        addTestDepartmentToDatabase(DEPARTMENT_FIXTURE_1_ID,DEPARTMENT_FIXTURE_1_NAME);
        
        addTestShiftToDatabase(DOCTOR_FIXTURE_2_ID, DEPARTMENT_FIXTURE_1_ID, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        application("org.duckdns.whocaresleft.app.swing.SwingHospitalApp")
            .withArgs(
                "--mongo-connection-string=" + connectionString,
                "--db-name=" + DB_NAME,
                "--db-mongo-doctor-collection=" + DOCTOR_COLLECTION,
                "--db-mongo-department-collection=" + DEPARTMENT_COLLECTION,
                "--db-mongo-shift-collection=" + SHIFT_COLLECTION
            ).start();
        
        window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                return "Hospital Scheduler X".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(BasicRobot.robotWithCurrentAwtHierarchy());
    }
    
    @AfterEach
    void teardown() {
        if (window != null)
            window.cleanUp();
        client.close();
    }
    
    @Nested @DisplayName("Success Cases")
    class SuccessCases {
        
        @Test @GUITest
        void testOnStartAllDoctorsAreShown() {
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("doctorList").contents())
                    .anySatisfy(e -> assertThat(e).contains(DOCTOR_FIXTURE_1_ID, DOCTOR_FIXTURE_1_FIRST_NAME, DOCTOR_FIXTURE_1_LAST_NAME))
                    .anySatisfy(e -> assertThat(e).contains(DOCTOR_FIXTURE_2_ID, DOCTOR_FIXTURE_2_FIRST_NAME, DOCTOR_FIXTURE_2_LAST_NAME)));
        }
        
        @Test @GUITest
        void testAddShiftSuccess() {
            window.tabbedPane().selectTab("Shifts");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.textBox("doctorIdTextBox").enterText(DOCTOR_FIXTURE_1_ID);
            window.textBox("departmentIdTextBox").enterText(DEPARTMENT_FIXTURE_1_ID);
            window.textBox("dateTextBox").enterText("24/07/2026");
            window.textBox("startTimeTextBox").enterText("08:00");
            window.textBox("endTimeTextBox").enterText("09:00");
            
            window.button("addButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents())
                    .anySatisfy(e -> assertThat(e).contains(
                        DOCTOR_FIXTURE_1_ID, DEPARTMENT_FIXTURE_1_ID,
                        DATE_24_07_2026.toString(), TIME_08_00.toString(), TIME_09_00.toString())));
        }
        
        @Test @GUITest
        void testRemoveShiftSuccess() {
            window.tabbedPane().selectTab("Shifts");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.button("deleteButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents())
                    .noneMatch(e -> e.contains(DOCTOR_FIXTURE_2_ID)));
        }
        
        @Test @GUITest
        void testUpdateShiftSuccess() {
            window.tabbedPane().selectTab("Shifts");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.checkBox("editShift").click();
            window.textBox("selectedDoctorIdTextBox").setText("").enterText(DOCTOR_FIXTURE_1_ID);
            window.button("updateButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents())
                .anySatisfy(e -> assertThat(e).contains(
                    DOCTOR_FIXTURE_1_ID, DEPARTMENT_FIXTURE_1_ID,
                    DATE_24_07_2026.toString(), TIME_08_00.toString(), TIME_09_00.toString())));
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ErrorCases {
        
        @Test @GUITest
        void testAddShiftError() {
            window.tabbedPane().selectTab("Shifts");
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.textBox("doctorIdTextBox").enterText(DOCTOR_FIXTURE_1_ID);
            window.textBox("departmentIdTextBox").enterText(DEPARTMENT_FIXTURE_NON_EXISTENT_ID);
            window.textBox("dateTextBox").enterText("24/07/2026");
            window.textBox("startTimeTextBox").enterText("08:00");
            window.textBox("endTimeTextBox").enterText("09:00");
            
            window.button("addButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorLabel").text().contains(DEPARTMENT_FIXTURE_NON_EXISTENT_ID)));
        }
        
        @Test @GUITest
        void testRemoveShiftError() {
            window.tabbedPane().selectTab("Shifts");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            removeTestShiftsFromDatabase(DOCTOR_FIXTURE_2_ID);
            
            window.button("deleteButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorLabel").text().contains(DOCTOR_FIXTURE_2_ID)));
        }
        
        @Test @GUITest
        void testUpdateShiftError() {
            window.tabbedPane().selectTab("Shifts");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.textBox("dateTextBox").requireEnabled());
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.checkBox("editShift").click();
            window.textBox("selectedDepartmentIdTextBox").setText("").enterText(DEPARTMENT_FIXTURE_NON_EXISTENT_ID);
            window.button("updateButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorLabel").text().contains(DEPARTMENT_FIXTURE_NON_EXISTENT_ID)));
        }
    }
    
    private void addTestDepartmentToDatabase(String id, String name) {
        client
            .getDatabase(DB_NAME)
            .getCollection(DEPARTMENT_COLLECTION)
            .insertOne(new Document()
                .append("_id", id)
                .append("name", name));
    }
    
    private void addTestDoctorToDatabase(String id, String firstName, String lastName) {
        client
        .getDatabase(DB_NAME)
        .getCollection(DOCTOR_COLLECTION)
        .insertOne(new Document()
            .append("_id", id)
            .append("firstName", firstName)
            .append("lastName", lastName));
    }
    
    private void addTestShiftToDatabase(String doctorId, String departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String documentId = String.format("%s-%s-%s-%s-%s",
            doctorId,
            departmentId,
            date.toString(),
            startTime.toString(),
            endTime.toString());
        
        Document toInsert = new Document()
            .append("_id", documentId)
            .append("doctorId", doctorId)
            .append("departmentId", departmentId)
            .append("date", date.toString())
            .append("startTime", startTime.toString())
            .append("endTime", endTime.toString());
        
        client
            .getDatabase(DB_NAME)
            .getCollection(SHIFT_COLLECTION)
            .insertOne(toInsert);
    }
    
    private void removeTestShiftsFromDatabase(String doctorId) {
        client
            .getDatabase(DB_NAME)
            .getCollection(SHIFT_COLLECTION)
            .deleteMany(Filters.eq("doctorId", doctorId));
    }
}
