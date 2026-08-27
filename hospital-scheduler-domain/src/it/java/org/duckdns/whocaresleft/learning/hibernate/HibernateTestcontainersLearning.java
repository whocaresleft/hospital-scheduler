package org.duckdns.whocaresleft.learning.hibernate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Learning tests for Hibernate API")
class HibernateTestcontainersLearning {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager em;
    
    @BeforeAll
    static void setupEntityManagerFactory() {
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("for_learning_tests", properties);
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
    
    @Test @DisplayName("Persist method without transaction, cache will contain the entity")
    void testPersistWithoutTransactionCacheWillStoreTheEntity() {
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        
        LearningEntity cachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(cachedEntity)
            .isSameAs(le);
    }
    
    @Test @DisplayName("Persist without transaction and then clearing cache. The db did not store the entity")
    void testPersistClearingCacheWithoutTransactionTheDatabaseWontHaveStoredTheEntity() {
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        
        em.clear();
        
        LearningEntity notCachedAnymore = em.find(LearningEntity.class, "le_id");
        assertThat(notCachedAnymore)
            .isNull();
    }
    
    @Test @DisplayName("Persist with transaction committed, the cache will have the entity stored, which is the same reference")
    void testPersistAlsoWithTransactionStoresInCacheAndWillStoreTheSameObject() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        em.getTransaction().commit();

        LearningEntity cachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(cachedEntity)
            .isSameAs(le);
    }
    
    @Test @DisplayName("Persist with transaction rollbacked, the cache will NOT have the value stored")
    void testPersistWithTransactionRollbackedWontStoreNotEvenInCache() {
        em.getTransaction().begin();
        LearningEntity le = new LearningEntity("le_id", "Entity 1");
        em.persist(le);
        em.getTransaction().rollback();

        LearningEntity notCachedEntity = em.find(LearningEntity.class, "le_id");
        assertThat(notCachedEntity)
            .isNull();
    }
    
    @Test @DisplayName("Persist with transaction committed, after clearing cache the object is retrieved from database, it's not the same object but it's equal")
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
    
    @Test @DisplayName("Managed entity (retrieved during transaction, during the same transaction) can be modified using setter, and the update is registered after commit")
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
    
    @Test @DisplayName("Detached entity (retrieved during a transaction, then transaction terminates) cannot be modified using setters, even in transaction")
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
    
    @Test @DisplayName("Detached entity can be updated by using merge in a new transaction")
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
    
    @Test @DisplayName("Adding an entity and removing it, in different transactions, without clearing cache, will delete removed entity from cache as well")
    void testRemoveAndEvenIfCacheIsNotClearedEntityWontBeFoundInCache() {
        addTestLearningEntity("le_id", "Entity 1");
        
        em.getTransaction().begin();
        LearningEntity toRemove = em.find(LearningEntity.class, "le_id");
        em.remove(toRemove);
        em.getTransaction().commit();
        
        LearningEntity cached = em.find(LearningEntity.class, "le_id");
        assertThat(cached)
            .isNull();
    }
    
    @Test @DisplayName("Removing entity that does not exist")
    void testRemoveEntityThatDoesNotExistPassingTheEntityDoesNotThrow() {
        LearningEntity le = new LearningEntity("id", "name");
        
        assertThatCode(() -> {
            em.getTransaction().begin();
            em.remove(le);
            em.getTransaction().commit();
        }).doesNotThrowAnyException();
    }
    
    @Test @DisplayName("Removing entity that does not exist using find throws at REMOVE because it's null")
    void testRemoveEntityThatDoesNotExistUsingFindThrowsAtRemove() {
        em.getTransaction().begin();
        LearningEntity toBeRemoved = em.find(LearningEntity.class, "le_id");
        
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> em.remove(toBeRemoved));
        em.getTransaction().rollback();
    }
    
    @Test @DisplayName("Finding a single entity does not require a transaction")
    void testFindCanBeUsedAlsoWithoutTransactions() {
        addTestLearningEntity("le_id", "Entity 1");
        em.clear();
        
        LearningEntity foundButNotCached = em.find(LearningEntity.class, "le_id");
        assertThat(foundButNotCached)
            .extracting(LearningEntity::getName)
            .isEqualTo("Entity 1");
    }
    
    @Test @DisplayName("Retrieving all entities does not require a transaction to be performed")
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
    @Test @DisplayName("Retrieving all entities with database empty will return empty list")
    void testFindAllIfNoEntitiesInDBIsJustEmptyList() {
        List<LearningEntity> list = em.createQuery(
                "SELECT e FROM LearningEntity e",
                LearningEntity.class).getResultList();
            
            assertThat(list)
                .isEmpty();
    }
    
    @Test @DisplayName("Calling merge with a non existing entity adds it")
    void callingMergeWithANonExistingEntityAddsItNorThrows() {
        em.getTransaction().begin();
        em.merge(new LearningEntity("id", "name"));
        em.getTransaction().commit();
        em.clear();
        
        assertThat(em.find(LearningEntity.class, "id"))
            .isEqualTo(new LearningEntity("id", "name"));
    }
    
    @Test @DisplayName("Adding an entity with duplicated id will have transaction rollbacked, the wrong entity is NOT cached")
    void testPersistWithDuplicateIdInTrasactionGetsRollbackedWithExceptionOnCommitAndWrongEntityIsNotCached() {
        addTestLearningEntity("le_id", "Original");
        em.clear();
        em.getTransaction().begin();
        em.persist(new LearningEntity("le_id", "Other entity"));
        
        assertThatExceptionOfType(jakarta.persistence.RollbackException.class)
            .isThrownBy(() -> {
                em.getTransaction().commit();
            });
        
        LearningEntity notCached = em.find(LearningEntity.class, "le_id");
        assertThat(notCached)
            .extracting(LearningEntity::getName)
            .isNotEqualTo("Other entity")
            .isEqualTo("Original");
    }
    
    @Test @DisplayName("1:1 primary-related relation insertion and retrieval works as expected")
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
    
    @Test @DisplayName("1:1 primary-related relation. Storing the related entity only will also insert the primary in the database")
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
    
    @Test @DisplayName("1:1 primary-related relation. Deleting only the primary entity in transaction will have the transaction rollbacked. Nothing is deleted as expected")
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
    
    @Test @DisplayName("1:1 primary-related relation. Deleting only the related entity in transaction will keep the primary only as expected")
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
    
    @Test @DisplayName("Wrong order of removal in 1:1 primary-related relation in trasaction. Will be rollbacked, nothing is deleted")
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
    
    @Test @DisplayName("Correct order of removal in 1:1 primary-related relation in trasaction. Will be committed with no problems")
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
    
    @Test
    void testMultipleBegins() {
        em.getTransaction().begin();
        {
            em.getTransaction().begin();
            em.persist(new LearningEntity("id", "name"));
            em.getTransaction().commit();
        }
        em.getTransaction().rollback();
    }
    
    private void addTestLearningEntity(String id, String name) {
        em.getTransaction().begin();
        em.persist(new LearningEntity(id, name));
        em.getTransaction().commit();
    }
}
