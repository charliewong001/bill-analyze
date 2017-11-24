package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.abc.AbstractABCTemplate;

/**
 *
 * @author Charlie
 * @description 交通银行
 */
@Service
public class ABCAnalyzer extends AbstractBankMailAnalyzer<AbstractABCTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.ABC.getBankCode())
                || name.contains(BankCodeEnum.ABC.getBankName()));
    }

}
