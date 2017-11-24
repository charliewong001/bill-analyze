package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.citic.AbstractCITICTemplate;

/**
 *
 * @author Charlie
 * @description 中信银行解析模板
 */
@Service
public class CITICAnalyzer extends AbstractBankMailAnalyzer<AbstractCITICTemplate> {
    public static final BankCodeEnum bankCode = BankCodeEnum.CITIC;

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.CITIC.getBankCode())
                || name.contains(BankCodeEnum.CITIC.getBankName()));
    }

}
