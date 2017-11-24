package com.pay.aile.bill.service.mail.analyze.impl;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.pay.aile.bill.BillAnalyzeApplication;
import com.pay.aile.bill.analyze.BankMailAnalyzer;
import com.pay.aile.bill.exception.AnalyzeBillException;

import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.MongoDownloadUtil;
import com.pay.aile.bill.utils.TextExtractUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BillAnalyzeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CEBAnalyzerTest {

    @Resource
    private BankMailAnalyzer CEBAnalyzer;
    @Autowired
    private MongoDownloadUtil downloadUtil;

    @Test
    public void test() throws AnalyzeBillException {

        String content = "";
        try {
            content = downloadUtil.getFile("5a9dcbbd-7926-4167-b973-771439acf52b");
        } catch (AnalyzeBillException e) {
            e.printStackTrace();
        }
        content = TextExtractUtil.parseHtml(content, "td");
        System.out.println(content);
        AnalyzeParamsModel amp = new AnalyzeParamsModel();
        amp.setOriginContent(content);
        amp.setBankCode("CEB");
        amp.setBankId("1");
        amp.setCardtypeId(2l);
        amp.setEmail("123@QQ.com");
        amp.setEmailId(6L);

        CEBAnalyzer.analyze(amp);
    }

}
