package org.duckdns.whocaresleft.repository.mongodb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

class MongoDoctorRepositoryTest {

    @Test
    void testFindAllWhenDatabaseIsEmpty() {
        MongoServer server = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = server.bind();
        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
        MongoDoctorRepository repository = new MongoDoctorRepository(client);
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        
        assertThat(repository.findAll()).isEmpty();
        
        client.close();
        server.shutdown();
    }
    
    @Test
    void testFindAllWhenDatabaseIsNotEmpty() {
        MongoServer server = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = server.bind();
        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
        MongoDoctorRepository repository = new MongoDoctorRepository(client);
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        MongoCollection<Document> doctorCollection = database.getCollection("doctor");
        doctorCollection.insertOne(
            new Document()
               .append("id", "doctor_1")
               .append("firstName", "doc")
               .append("lastName", "tor"));
        
        doctorCollection.insertOne(
                new Document()
                   .append("id", "doctor_2")
                   .append("firstName", "doc")
                   .append("lastName", "ter"));
        
        assertThat(repository.findAll())
            .containsExactly(
                Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                Doctor.createDoctor(Id.createId("doctor_2"), "doc", "ter"));
        
        client.close();
        server.shutdown();
    }
}
