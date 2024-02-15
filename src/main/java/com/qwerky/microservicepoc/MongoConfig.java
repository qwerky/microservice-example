package com.qwerky.microservicepoc;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;


@Configuration
public class MongoConfig {

    private String mongoConnection;
    private String mongoDb;

    @Value("${learn.mongo.connection}")
    public void setMongoConnection(String mongoConnection) {
        this.mongoConnection = mongoConnection;
    }

    @Value("${learn.mongo.db}")
    public void setMongoDb(String mongoDb) {
        this.mongoDb = mongoDb;
    }

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString(mongoConnection);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();

        return MongoClients.create(settings);
    }


    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongo(), mongoDb);
    }

}
