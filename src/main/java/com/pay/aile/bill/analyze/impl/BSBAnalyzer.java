package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.bsb.AbstractBSBTemplate;

/**
 *
 * @author Charlie
 * @description 包商银行
 */
@Service
public class BSBAnalyzer extends AbstractBankMailAnalyzer<AbstractBSBTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.BSB.getBankCode())
                || name.contains(BankCodeEnum.BSB.getBankName()));
    }

}
