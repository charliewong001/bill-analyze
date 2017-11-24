package com.pay.aile.bill.analyze.banktemplate.bcm;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.contant.Constant;
import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditBillDetail;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.DateUtil;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 *
 * @author Charlie
 * @description 交通银行信用卡账单内容解析模板
 */
@Service
public class BCMTemplate extends AbstractBCMTemplate {

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardtypeId(10L);
            rules.setCardholder("尊敬的 [\\u4e00-\\u9fa5]+(女士|先生)");
            rules.setCardNumbers("卡号末四位\\d{4}");
            rules.setCycle(" 账单周期：\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2}");
            rules.setDueDate("到期还款日 \\d{4}/\\d{2}/\\d{2}");
            rules.setCurrentAmount("本期应还款额 -?\\d+\\.?\\d*");
            rules.setMinimum("最低还款额 \\d+\\.?\\d*");
            rules.setCredits("信用额度 \\d+.?\\d*");
            rules.setCash("取现额度 \\d+\\.?\\d*");
            rules.setIntegral("人民币账户 \\d+");
            rules.setDetails(
                    "\\d{4}/\\d{2}/\\d{2} \\d{4}/\\d{2}/\\d{2} \\S+ [a-zA-Z]{3}\\d+\\.?\\d* [a-zA-Z]{3}\\d+\\.?\\d*");
            rules.setTransactionDate("0");
            rules.setBillingDate("1");
            rules.setTransactionDescription("2");
        }
    }

    @Override
    protected void analyzeCurrentAmount(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCurrentAmount())) {
            List<String> currentAmountList = getValueListByPattern("currentAmount", content, rules.getCurrentAmount(),
                    "");
            currentAmountList = PatternMatcherUtil.getMatcher(Constant.pattern_amount, currentAmountList);
            if (!currentAmountList.isEmpty()) {
                currentAmountList.stream().map(item -> {
                    if (item.startsWith("-")) {
                        item = "0";
                        return item;
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
    protected void analyzeCycle(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCycle())) {
            String cycle = getValueByPattern("cycle", content, rules.getCycle(), apm, "：");
            String[] sa = cycle.split("-");
            billList.forEach(bill -> {
                bill.setBeginDate(DateUtil.parseDate(sa[0]));
                bill.setEndDate(DateUtil.parseDate(sa[1]));
            });
        }
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.BCM_DEFAULT;
    }

    @Override
    protected void setField(CreditBillDetail cbd, int index, String value) {
        value = value.replaceAll("([A-Za-z]{3})(\\d+\\.?\\d*)", "$1@$2");
        String[] s = value.split("@");
        if (index == 3) {
            cbd.setTransactionCurrency(s[0]);
            cbd.setTransactionAmount(s[1]);
        }
        if (index == 4) {
            cbd.setAccountType(s[0]);
            cbd.setAccountableAmount(s[1]);
        }
    }

}
