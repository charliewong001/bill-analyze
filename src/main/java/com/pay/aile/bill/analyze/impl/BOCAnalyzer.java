package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.boc.AbstractBOCTemplate;

/**
 *
 * @author zhibin.cui
 * @description 中国银行解析模版
 */
@Service("BOCAnalyzer")
public class BOCAnalyzer extends AbstractBankMailAnalyzer<AbstractBOCTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.BOC.getBankCode())
                || name.contains(BankCodeEnum.BOC.getBankName()));
    }

}
