package com.pay.aile.bill.analyze.banktemplate.bsb;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.contant.Constant;
import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 *
 * @author Charlie
 * @description 包商银行解析模板
 */
@Service
public class BSBTemplate extends AbstractBSBTemplate {

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardtypeId(9L);
            rules.setCurrentAmount("本期应还款额：\\d+\\.?\\d*");
            rules.setMinimum("最低还款额：\\d+\\.?\\d*");
        }
    }

    @Override
    protected void analyzeCurrentAmount(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCurrentAmount())) {
            List<String> currentAmountList = getValueListByPattern("currentAmount", content, rules.getCurrentAmount(),
                    "：");
            currentAmountList = PatternMatcherUtil.getMatcher(Constant.pattern_amount, currentAmountList);
            if (!currentAmountList.isEmpty()) {
                currentAmountList.stream().map(item -> {
                    if (item.startsWith("-")) {
                        return item.replaceAll("-", "");
                    } else {
                        return item;
                    }
                }).forEach(currentAmount -> {
                    CreditBill bill = new CreditBill();
                    bill.setCurrentAmount(new BigDecimal(currentAmount));
                    billList.add(bill);
                });
            }
        }
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.BSB_DEFAULT;
    }

}
