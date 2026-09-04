package org.duckdns.whocaresleft.app.swing;

import java.awt.EventQueue;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mariadb.MariaTransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.duckdns.whocaresleft.view.swing.SwingHospitalFrame;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Command(mixinStandardHelpOptions = true)
public class SwingHospitalApp implements Callable<Void>{
    
    private static final Logger LOGGER = LogManager.getLogger(SwingHospitalApp.class);
    
    @Option(names = {"-d", "--db-backend"}, description = "Database backend (mongodb and mariadb)")
    private String databaseBackend = "mongodb";
    
    @Option(names = {"--mongo-connection-string"}, description = "MongoDB connection string")
    private String mongoConnectionString = "mongodb://localhost:27017/?replicaSet=rs0";
    
    @Option(names = {"--maria-jdbc-url"}, description = "JDBC connection URL for MariaDB")
    private String mariaJdbcUrl = DEFAULT_MARIA_JDBC_URL;
    private static final String DEFAULT_MARIA_JDBC_URL = "jdbc:mariadb://localhost:3306/";
    
    @Option(names = {"--db-mongo-name"}, description = "MongoDB database name")
    private String databaseName = "hospital";
    
    @Option(names = {"--db-mongo-doctor-collection"}, description = "Doctor collectio name")
    private String doctorCollection = "doctor";
    
    @Option(names = {"--db-mongo-department-collection"}, description = "Department collectio name")
    private String departmentCollection = "department";
    
    @Option(names = {"--db-mongo-shift-collection"}, description = "Shift collectio name")
    private String shiftCollection = "shift";
    
    @Option(names = {"--maria-user"}, description = "MariaDB username")
    private String mariaUser = "root";
    
    @Option(names = {"--maria-password"}, description = "MariaDB password")
    private String mariaPassword = "password";
    
    @Option(names = {"--maria-ddl"}, description = "Hibernate schema generation strategy")
    private String mariaDdl = "update";
    
    @Option(names = {"--connection-timeout"}, description = "Amount (ms) of time before considering the connection timed out")
    private int timeout = 15000;
    
    public static void main(String[] args) {
        new CommandLine(new SwingHospitalApp()).execute(args);
    }
    
    @Override
    public Void call() throws Exception {
        
        EventQueue.invokeLater(() -> {
            try {
                TransactionManager transactionManager = getTransactionManager();
                
                SwingHospitalFrame frame = new SwingHospitalFrame();
                
                DoctorPresenter doctorPresenter = new DoctorPresenter(transactionManager, frame.getDoctorView());
                DepartmentPresenter departmentPresenter = new DepartmentPresenter(transactionManager, frame.getDepartmentView());
                ShiftPresenter shiftPresenter = new ShiftPresenter(transactionManager, frame.getShiftView());
                
                frame.setDoctorPresenter(doctorPresenter);
                frame.setDepartmentPresenter(departmentPresenter);
                frame.setShiftPresenter(shiftPresenter);
                
                frame.setVisible(true);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        });
        return null;
    }
    
    private TransactionManager getTransactionManager() {
        TransactionManager transactionManager;
        
        switch (databaseBackend.toLowerCase()) {
        
        case "mongodb":
            MongoClient client = MongoClients.create(mongoConnectionString + "&serverSelectionTimeoutMS=" + timeout);
            MongoDatabase database = client.getDatabase(databaseName);
            
            try {
                database.runCommand(new Document("ping", 1));
            } catch (Exception e) {
                client.close();
                throw new IllegalStateException("Cannot connect within " + timeout + "ms to MongoDB @" + mongoConnectionString);
            }
            
            transactionManager = new MongoTransactionManager(client, database, doctorCollection, departmentCollection, shiftCollection);
            break;
            
        case "mariadb":
            
            if (mariaJdbcUrl.equals(DEFAULT_MARIA_JDBC_URL)) {
                mariaJdbcUrl = mariaJdbcUrl + databaseName;
            }
            
            String additionalArgs = "createDatabaseIfNotExist=true&connectTimeout=" + timeout;
            String finalUrl = mariaJdbcUrl.contains("?")
                ? mariaJdbcUrl + "&" + additionalArgs
                : mariaJdbcUrl + "?" + additionalArgs;
            
            Map<String, String> properties = Map.of(
                "jakarta.persistence.jdbc.url", finalUrl,
                "jakarta.persistence.jdbc.user", mariaUser,
                "jakarta.persistence.jdbc.password", mariaPassword,
                "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
                "hibernate.hbm2ddl.auto", mariaDdl);
            EntityManagerFactory  emf;
            try {
                emf = Persistence.createEntityManagerFactory("maria_repository_it", properties);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot connect within " + timeout + "ms to MariaDB @" + mariaJdbcUrl);
            }
                
            transactionManager = new MariaTransactionManager(emf);
            break;
            
        default:
            throw new IllegalArgumentException("Unknown database backend: " + databaseBackend + ", only \"mongodb\" and \"mariadb\" supported");
        }
        
        return transactionManager;
    }
}