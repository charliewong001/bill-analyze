package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.hxb.AbstractHXBTemplate;

/**
 *
 * @author Charlie
 * @description 建设银行
 */
@Service("HBXAnalyzer")
public class HXBAnalyzer extends AbstractBankMailAnalyzer<AbstractHXBTemplate> {

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.HXB.getBankCode())
                || name.contains(BankCodeEnum.HXB.getBankName()));
    }

}
