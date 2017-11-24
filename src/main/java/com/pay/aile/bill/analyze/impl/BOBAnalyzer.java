package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.bob.AbstractBOBTemplate;

/**
 *
 * @author Charlie
 * @description 北京银行
 */
@Service
public class BOBAnalyzer extends AbstractBankMailAnalyzer<AbstractBOBTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.BOB.getBankCode())
                || name.contains(BankCodeEnum.BOB.getBankName()));
    }

}
