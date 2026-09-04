package org.duckdns.whocaresleft.swing.mariadb;

import static org.awaitility.Awaitility.await;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
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
import org.duckdns.whocaresleft.repository.mariadb.entity.DepartmentEntity;
import org.duckdns.whocaresleft.repository.mariadb.entity.DoctorEntity;
import org.duckdns.whocaresleft.repository.mariadb.entity.ShiftEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("End-to-End tests for the SwingHospitalApp, using MariaDB as database backend")
class SwingHospitalAppMariaE2E {
    
    private static final int TIMEOUT = 15;
    
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
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.11");
    
    private EntityManager em;
    private static EntityManagerFactory emf;
    private FrameFixture window;
    
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
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ShiftEntity").executeUpdate();
        em.createQuery("DELETE FROM DepartmentEntity").executeUpdate();
        em.createQuery("DELETE FROM DoctorEntity").executeUpdate();
        em.getTransaction().commit();
        
        addTestDoctorToDatabase(DOCTOR_FIXTURE_1_ID, DOCTOR_FIXTURE_1_FIRST_NAME, DOCTOR_FIXTURE_1_LAST_NAME);
        addTestDoctorToDatabase(DOCTOR_FIXTURE_2_ID, DOCTOR_FIXTURE_2_FIRST_NAME, DOCTOR_FIXTURE_2_LAST_NAME);
        
        addTestDepartmentToDatabase(DEPARTMENT_FIXTURE_1_ID,DEPARTMENT_FIXTURE_1_NAME);
        
        addTestShiftToDatabase(DOCTOR_FIXTURE_2_ID, DEPARTMENT_FIXTURE_1_ID, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        application("org.duckdns.whocaresleft.app.swing.SwingHospitalApp")
            .withArgs(
                "--db-backend=mariadb",
                "--maria-jdbc-url=" + maria.getJdbcUrl(),
                "--maria-user=" + maria.getUsername(),
                "--maria-password=" + maria.getPassword(),
                "--maria-ddl=update"
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
        em.close();
        if (window != null)
            window.cleanUp();
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
            waitForTabToLoad("shift");
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.button("deleteButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents())
                    .noneMatch(e -> e.contains(DOCTOR_FIXTURE_2_ID)));
        }
        
        @Test @GUITest
        void testUpdateShiftSuccess() {
            window.tabbedPane().selectTab("Shifts");
            waitForTabToLoad("shift");
            
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
        
        @Test @GUITest
        void testDeleteDoctorAlsoDeletesItsShifts() {
            window.tabbedPane().selectTab("Shifts");
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents())
                    .anySatisfy(e -> assertThat(e).contains(
                        DOCTOR_FIXTURE_2_ID, DEPARTMENT_FIXTURE_1_ID,
                        DATE_24_07_2026.toString(), TIME_08_00.toString(), TIME_09_00.toString())));
            
            window.tabbedPane().selectTab("Doctors");
            waitForTabToLoad("doctor");
            window.list("doctorList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.button("deleteButton").click();
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("doctorList").contents())
                    .noneMatch(e -> e.contains(DOCTOR_FIXTURE_2_ID)));
            
            window.tabbedPane().selectTab("Shifts");
            waitForTabToLoad("shift");
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list("shiftList").contents()).isEmpty());
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ErrorCases {
        
        @Test @GUITest
        void testAddShiftError() {
            window.tabbedPane().selectTab("Shifts");
            waitForTabToLoad("shift");
            
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
            waitForTabToLoad("shift");
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            removeTestShiftsFromDatabase(DOCTOR_FIXTURE_2_ID);
            
            window.button("deleteButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorLabel").text().contains(DOCTOR_FIXTURE_2_ID)));
        }
        
        @Test @GUITest
        void testUpdateShiftError() {
            window.tabbedPane().selectTab("Shifts");
            waitForTabToLoad("shift");
            
            window.list("shiftList").selectItem(Pattern.compile(".*" + DOCTOR_FIXTURE_2_ID + ".*"));
            
            window.checkBox("editShift").click();
            window.textBox("selectedDepartmentIdTextBox").setText("").enterText(DEPARTMENT_FIXTURE_NON_EXISTENT_ID);
            window.button("updateButton").click();
            
            await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorLabel").text().contains(DEPARTMENT_FIXTURE_NON_EXISTENT_ID)));
        }
    }
    
    private void waitForTabToLoad(String tab) {
        await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> window.list(tab + "List").requireEnabled());
    }
    
    private void addTestDepartmentToDatabase(String id, String name) {
        em.getTransaction().begin();
        em.persist(new DepartmentEntity(id, name));
        em.getTransaction().commit();
        em.clear();
    }
    
    private void addTestDoctorToDatabase(String id, String firstName, String lastName) {
        em.getTransaction().begin();
        em.persist(new DoctorEntity(id, firstName, lastName));
        em.getTransaction().commit();
        em.clear();
    }
    
    private void addTestShiftToDatabase(String doctorId, String departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String shiftId = String.format("%s-%s-%s-%s-%s",
            doctorId,
            departmentId,
            date.toString(),
            startTime.toString(),
            endTime.toString());
        
        em.getTransaction().begin();
        em.persist(new ShiftEntity(
            shiftId,
            doctorId,
            departmentId,
            date,
            startTime,
            endTime));
        em.getTransaction().commit();
        em.clear();
    }
    
    private void removeTestShiftsFromDatabase(String doctorId) {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ShiftEntity e WHERE e.doctorId = :doctorId")
            .setParameter("doctorId", doctorId)
            .executeUpdate();
        em.getTransaction().commit();
        em.clear();
    }
}
