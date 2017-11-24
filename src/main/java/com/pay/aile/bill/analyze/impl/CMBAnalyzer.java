package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.cmb.AbstractCMBTemplate;

/**
 *
 * @author Charlie
 * @description 招商银行解析模板
 */
@Service
public class CMBAnalyzer extends AbstractBankMailAnalyzer<AbstractCMBTemplate> {
    public static final BankCodeEnum bankCode = BankCodeEnum.CMB;

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.CMB.getBankCode())
                || name.contains(BankCodeEnum.CMB.getBankName()));
    }

}
