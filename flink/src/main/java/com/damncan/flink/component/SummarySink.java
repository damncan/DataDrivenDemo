package com.damncan.flink.component;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This component is used to store the analysing result into MongoDB.
 *
 * @author Ian Zhong (damncan)
 * @since 14 January 2023
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class SummarySink extends RichSinkFunction<Tuple5<String, Double, Double, Double, ConcurrentHashMap<String, HashMap<String, Double>>>> {
    @Value(value = "${spring.data.mongodb.host}")
    private String mongoHost;
    @Value(value = "${spring.data.mongodb.port}")
    private String mongoPort;
    @Value(value = "${spring.data.mongodb.database}")
    private String mongoDatabaseName;
    @Value(value = "${spring.data.mongodb.collection}")
    private String mongoCollection;

    static MongoClient mongoClient = null;
    static MongoDatabase mongoDatabase = null;

    public SummarySink() {
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
        mongoDatabase = mongoClient.getDatabase(mongoDatabaseName);
        if (mongoDatabase.getCollection(mongoCollection) == null) {
            mongoDatabase.createCollection(mongoCollection);
        } else {
            mongoDatabase.getCollection(mongoCollection);
        }
    }

    @Override
    public void invoke(Tuple5<String, Double, Double, Double, ConcurrentHashMap<String, HashMap<String, Double>>> value, SinkFunction.Context context) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(mongoCollection);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = collection.updateOne(
                Filters.eq("baseCurrency", value.f0),
                Updates.combine(
                        Updates.set("turnover", value.f1),
                        Updates.set("volume", value.f2),
                        Updates.set("count", value.f3),
                        Updates.set("currencyMap", new BasicDBObject(value.f4))),
                options);

        System.out.println("incoming data: " + value);
        System.out.println("updated result: " + updateResult);
    }

    @Override
    public void close() {
        mongoClient.close();
    }
}
