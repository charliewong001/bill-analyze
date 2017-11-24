package com.pay.aile.bill.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.pay.aile.bill.entity.EmailFile;
import com.pay.aile.bill.exception.AnalyzeBillException;
import com.pay.aile.bill.task.FileQueueRedisHandle;

/***
 * DownloadUtil.java
 *
 * @author shinelon
 *
 * @date 2017年10月30日
 *
 */
@Component
public class MongoDownloadUtil {
    private static final Logger logger = LoggerFactory.getLogger(MongoDownloadUtil.class);
    private static final String DOC_KEY_FILE_NAME = "fileName";
    private static final String DOC_KEY_FILE_EMAIL = "email";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FileQueueRedisHandle fileQueueRedisHandle;

    public String getFile(String fileName) throws AnalyzeBillException {

        try {

            Criteria criteria = new Criteria(DOC_KEY_FILE_NAME);
            criteria.is(fileName);
            Query query = new Query(criteria);
            EmailFile ef = mongoTemplate.findOne(query, EmailFile.class);
            return ef.getContent();
        } catch (Exception e) {

            logger.error(e.getMessage());
            throw new AnalyzeBillException(e.getMessage());
        }

    }

    @SuppressWarnings("static-access")
    public EmailFile getFile(String fileName, String email) throws AnalyzeBillException {

        try {

            EmailFile ef = mongoTemplate.findOne(
                    new Query(Criteria.where(DOC_KEY_FILE_NAME).is(fileName).and(DOC_KEY_FILE_EMAIL).is(email)),
                    EmailFile.class);
            return ef;
        } catch (Exception e) {

            logger.error(e.getMessage());
            throw new AnalyzeBillException(e.getMessage());
        }

    }

    public void saveEmailFiles(List<EmailFile> emailFileList) {
        List<String> fileNames = emailFileList.stream().map(e -> e.getFileName()).collect(Collectors.toList());
        Criteria criteria = new Criteria(DOC_KEY_FILE_NAME);
        criteria.in(fileNames);
        Query query = new Query(criteria);
        List<EmailFile> exitsEmailFileList = mongoTemplate.find(query, EmailFile.class);
        List<String> exitsfileNames = exitsEmailFileList.stream().map(e -> e.getFileName())
                .collect(Collectors.toList());
        List<EmailFile> insertList = emailFileList.stream().filter(e -> !exitsfileNames.contains(e.getFileName()))
                .collect(Collectors.toList());
        if (insertList.size() > 0) {
            mongoTemplate.insert(insertList, EmailFile.class);
        }

    }

}
