package org.duckdns.whocaresleft.learning.hibernate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
import jakarta.persistence.Persistence;;

@Testcontainers @DisplayName("Learning tests for Hibernate API")
class HibernateTestcontainersLearningTestsIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager em;
    
    @BeforeAll
    static void setupHibernatePropertiesAndEntityManagerFactory() {
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("learning", properties);
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
        em.createQuery("DELETE FROM LearningEntityBirthday").executeUpdate();
        em.createQuery("DELETE FROM LearningEntity").executeUpdate();
        em.getTransaction().commit();
    }
    
    @AfterEach
    void teardown() {
        if (em.isOpen()) {
            if(em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }       
    }
    
    @Test
    void testPersistWithoutTransactionCacheWillStoreTheEntity() {
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        
        LearningEntity cachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(cachedEntity)
            .isSameAs(le);
    }
    
    @Test
    void testPersistClearingCacheWithoutTransactionTheDatabaseWontHaveStoredTheEntity() {
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        
        em.clear();
        
        LearningEntity notCachedAnymore = em.find(LearningEntity.class, "le_id");
        assertThat(notCachedAnymore)
            .isNull();
    }
    
    @Test
    void testPersistAlsoWithTransactionStoresInCacheAndWillStoreTheSameObject() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        em.getTransaction().commit();

        LearningEntity cachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(cachedEntity)
            .isSameAs(le);
    }
    
    @Test
    void testPersistWithTransactionRollbackedWontStoreNotEvenInCache() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        em.getTransaction().rollback();

        LearningEntity notCachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(notCachedEntity)
            .isNull();
    }
    
    @Test
    void testPersistWithTransactionCommittedAndCacheClearedWillStoreAnObjectThatIsJustEqualButNotSame() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        em.getTransaction().commit();
        
        em.clear();

        LearningEntity foundButNotFromCache = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotFromCache)
            .isEqualTo(le)
            .isNotSameAs(le);
    }
    
    @Test
    void testManagedEntityAnEntityRetrievedFromDBAndModifiedInSameTransactionWillBeAutomaticallyUpdatedIfCommitted() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        em.getTransaction().begin();
        LearningEntity le = em.find(LearningEntity.class, "le_id");
        le.setName("Entity 2");
        em.getTransaction().commit();
        em.clear();

        LearningEntity foundButNotFromCache = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotFromCache)
            .extracting(LearningEntity::getName)
            .isEqualTo("Entity 2");
    }
    
    @Test
    void testDetachedEntityAnEntityRetrievedFromDBDuringTransactionAndModifiedAfterSaidTransactionEndedWillNotBeUpdated() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        LearningEntity le = em.find(LearningEntity.class, "le_id");
        em.clear();
        
        em.getTransaction().begin();
        le.setName("Entity 2");
        em.getTransaction().commit();
        em.clear();

        LearningEntity foundButNotFromCache = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotFromCache)
            .extracting(LearningEntity::getName)
            .isEqualTo("Entity 1");
    }
    
    @Test
    void testDetachedEntityCanBeUpdatedByUsingMergeInNewTransaction() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        LearningEntity le = em.find(LearningEntity.class, "le_id");
        em.clear();
        le.setName("Entity 2");
        
        em.getTransaction().begin();
        em.merge(le);
        em.getTransaction().commit();
        em.clear();

        LearningEntity foundButNotFromCache = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotFromCache)
            .extracting(LearningEntity::getName)
            .isEqualTo("Entity 2");
    }
    
    @Test
    void testRemoveAndEvenIfCacheIsNotClearedEntityWontBeFoundInCache() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        em.getTransaction().begin();
        LearningEntity toRemove = em.find(LearningEntity.class, "le_id");
        em.remove(toRemove);
        em.getTransaction().commit();
        
        LearningEntity cached = em.find(LearningEntity.class, "le_id");
        assertThat(cached)
            .isNull();
    }
    
    @Test
    void testFindCanBeUsedAlsoWithoutTransactions() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        LearningEntity foundButNotCached = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotCached)
            .extracting(LearningEntity::getName)
            .isEqualTo("Entity 1");
    }
    
    @Test
    void testFindAllEntitiesDoesNotRequireTransaction() {
        addTestLearningEntity("le_id1", "Entity 1");
        addTestLearningEntity("le_id2", "Entity 2");
        addTestLearningEntity("le_id3", "Entity 3");
        em.clear();
        
        List<LearningEntity> list = em.createQuery(
            "SELECT e FROM LearningEntity e",
            LearningEntity.class).getResultList();
        
        assertThat(list)
            .containsExactly(
                new LearningEntity("le_id1", "Entity 1"),
                new LearningEntity("le_id2", "Entity 2"),
                new LearningEntity("le_id3", "Entity 3"));
    }
    @Test
    void testFindAllIfNoEntitiesInDBIsJustEmptyList() {
        List<LearningEntity> list = em.createQuery(
                "SELECT e FROM LearningEntity e",
                LearningEntity.class).getResultList();
            
            assertThat(list)
                .isEmpty();
    }
    
    @Test
    void testPersistenceWithDuplicateIdInTrasactionGetsRollbackedWithExceptionOnCommitAndWrongEntityIsNotCached() {
        addTestLearningEntity("le_id", "Original");
        em.clear();
        em.getTransaction().begin();
        em.persist(new LearningEntity("le_id", "Other entity"));
        
        assertThatExceptionOfType(jakarta.persistence.RollbackException.class)
            .isThrownBy(() -> {
                em.getTransaction().commit();
            });
        
        LearningEntity cached = em.find(LearningEntity.class, "le_id");
        assertThat(cached)
            .extracting(LearningEntity::getName)
            .isEqualTo("Original");
    }
    
    @Test
    void testOneToOneSharedPrimaryKey() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        em.persist(le);
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        LearningEntityBirthday found = em.find(LearningEntityBirthday.class, "le_id");
        assertThat(found).isNotNull();
        assertThat(found.getLearningEntityId()).isEqualTo("le_id");
        assertThat(found.getLearningEntity().getName()).isEqualTo("Entity");
        assertThat(found.getDateOfBirth()).isEqualTo(LocalDate.of(2026, 8, 25));
    }
    
    @Test
    void testOneToOneSharedPrimaryKeyWhenPrimaryEntityIsNotPresentInDBStoresIt() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        assertThat(em.find(LearningEntityBirthday.class, "le_id"))
            .isEqualTo(birthday);
        
        assertThat(em.find(LearningEntity.class, "le_id"))
            .isEqualTo(le);
    }
    
    @Test
    void testOneToOneSharedPrimaryKeyRemovingPrimaryEntityFirstThrowsExceptionAndDoesNotRemoveAnything() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        em.persist(le);
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        em.remove(em.find(LearningEntity.class, "le_id"));
        
        assertThatExceptionOfType(jakarta.persistence.RollbackException.class)
            .isThrownBy(() -> em.getTransaction().commit());
        
        assertThat(em.find(LearningEntityBirthday.class, "le_id"))
            .isEqualTo(birthday);
        
        assertThat(em.find(LearningEntity.class, "le_id"))
            .isEqualTo(le);
    }
    
    @Test
    void testOneToOneSharedPrimaryKeyRemovingRelatedEntityFirstRemovesIt() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        em.persist(le);
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        em.remove(em.find(LearningEntityBirthday.class, "le_id"));
        em.getTransaction().commit();
        
        assertThat(em.find(LearningEntityBirthday.class, "le_id"))
            .isNull();
        
        assertThat(em.find(LearningEntity.class, "le_id"))
            .isEqualTo(le);
    }
    
    @Test
    void testOneToOneSharedPrimaryKeyRemovingBothInTransactionInWrongOrderStillThrowsException() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        em.persist(le);
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        em.remove(em.find(LearningEntity.class, "le_id"));
        em.remove(em.find(LearningEntityBirthday.class, "le_id"));
        
        assertThatExceptionOfType(jakarta.persistence.RollbackException.class)
            .isThrownBy(() -> em.getTransaction().commit());
        
        assertThat(em.find(LearningEntityBirthday.class, "le_id"))
            .isEqualTo(birthday);
    
        assertThat(em.find(LearningEntity.class, "le_id"))
            .isEqualTo(le);
    }
    
    @Test
    void testOneToOneSharedPrimaryKeyRemovingBothInTransactionInCorrectOrderDoesNotRollback() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity");
        em.persist(le);
        LearningEntityBirthday birthday = new LearningEntityBirthday(le, LocalDate.of(2026, 8, 25));
        em.persist(birthday);
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        em.remove(em.find(LearningEntityBirthday.class, "le_id"));
        em.remove(em.find(LearningEntity.class, "le_id"));
        em.getTransaction().commit();
        
        assertThat(em.find(LearningEntityBirthday.class, "le_id"))
            .isNull();
    
        assertThat(em.find(LearningEntity.class, "le_id"))
            .isNull();
    }
    
    private void addTestLearningEntity(String id, String name) {
        em.getTransaction().begin();
        em.persist(new LearningEntity(id, name));
        em.getTransaction().commit();
    }
}
