package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.bcm.AbstractBCMTemplate;

/**
 *
 * @author Charlie
 * @description 交通银行
 */
@Service
public class BCMAnalyzer extends AbstractBankMailAnalyzer<AbstractBCMTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.BCM.getBankCode())
                || name.contains(BankCodeEnum.BCM.getBankName()));
    }

}
