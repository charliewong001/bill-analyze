package com.pay.aile.bill.analyze.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.AbstractBankMailAnalyzer;
import com.pay.aile.bill.enums.BankCodeEnum;
import com.pay.aile.bill.analyze.banktemplate.psbc.AbstractPSBCTemplate;

/**
 *
 * @author zhibin.cui
 * @description 邮储银行解析模版
 */
@Service("PSBCAnalyzer")
public class PSBCAnalyzer extends AbstractBankMailAnalyzer<AbstractPSBCTemplate> {
    public static final BankCodeEnum bankCode = BankCodeEnum.PSBC;

    @Override
    public boolean support(String name) {
        return StringUtils.hasText(name) && (name.equalsIgnoreCase(BankCodeEnum.PSBC.getBankCode())
                || name.contains(BankCodeEnum.PSBC.getBankName()));
    }

}
