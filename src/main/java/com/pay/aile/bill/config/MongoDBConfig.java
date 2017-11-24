package com.pay.aile.bill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.pay.dsmclient.mongodb.MongoPoolClient;

@Configuration
@ConditionalOnProperty(prefix = "environment", value = "production")
public class MongoDBConfig extends AbstractMongoConfiguration {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${mongodb.databaseName}")
    private String databaseName;
    @Value("${mongodb.dataSourceName}")
    private String dataSourceName;

    @Bean
    @Override
    public Mongo mongo() throws Exception {
        MongoPoolClient mongoPoolClient = new MongoPoolClient();
        mongoPoolClient.setDataSourceType("mongoDb");
        mongoPoolClient.setDataSourceName(dataSourceName);
        mongoPoolClient.getDataSource();
        logger.debug("######################## MongodbPool create success!");
        return mongoPoolClient.getMongoClient();
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

}
