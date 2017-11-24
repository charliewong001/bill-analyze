package com.pay.aile.bill.analyze.banktemplate.icbc;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.contant.Constant;
import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditBillDetail;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.DateUtil;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 * @author ji
 * @description 中国工商银行-牡丹贷记卡
 */
@Service
public class ICBCMDCreditTemplate extends AbstractICBCTemplate {

    @Override
    protected void analyzeBillDate(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getBillDay())) {
            String billDay = getValueByPattern("billDay", content, rules.getBillDay(), apm, "");
            billDay = PatternMatcherUtil.getMatcherString("\\d{4}年\\d{2}月\\d{2}日", billDay);
            if (StringUtils.hasText(billDay)) {
                billDay = billDay.replaceAll("年", "").replaceAll("月", "").replaceAll("日", "").replaceAll("\\s+", "");
                final String finalBillDay = billDay.substring(billDay.length() - 2);
                cardList.forEach(card -> {
                    card.setBillDay(finalBillDay);
                });
            }
        }
    }

    @Override
    protected void analyzeCurrentAmount(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCurrentAmount())) {
            List<String> currentAmountList = getValueListByPattern("currentAmount", content, rules.getCurrentAmount(),
                    defaultSplitSign);
            currentAmountList = PatternMatcherUtil.getMatcher(Constant.pattern_amount, currentAmountList);
            if (!currentAmountList.isEmpty()) {
                currentAmountList.stream().map(item -> {
                    if (item.startsWith("-")) {
                        item = item.replaceAll("-", "");
                    }
                    item = item.replaceAll("\\RMB", "");
                    return item;
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

            String cycle = getValueByPattern("cycle", content, rules.getCycle(), apm, defaultSplitSign);
            List<String> list = PatternMatcherUtil.getMatcher("\\d{4}年\\d{2}月\\d{2}日", cycle);
            billList.forEach(bill -> {
                bill.setBeginDate(DateUtil.parseDate(list.get(0)));
                bill.setEndDate(DateUtil.parseDate(list.get(1)));
            });
        }
    }

    private List<String> getValueListByPattern(String content, String ruleValue) {

        if (StringUtils.hasText(ruleValue)) {

            List<String> list = PatternMatcherUtil.getMatcher(ruleValue, content);
            return list;
        }
        return null;
    }

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardtypeId(14L);
            rules.setBillDay("对账单生成日\\d{4}年\\d{2}月\\d{2}日");
            rules.setCardholder("尊敬的[\\u4e00-\\u9fa5]+");
            rules.setCycle("账单周期\\d{4}年\\d{2}月\\d{2}日—\\d{4}年\\d{2}月\\d{2}日");
            rules.setDueDate("贷记卡到期还款日 \\d{4}年\\d{1,2}月\\d{1,2}日");
            rules.setCurrentAmount(
                    "合计 -?\\d+\\.?\\d*/[a-z-A-Z]+ -?\\d+\\.?\\d*/[a-z-A-Z]+ -?\\d+\\.?\\d*/[a-z-A-Z]+ -?\\d+\\.?\\d*/[a-z-A-Z]+");
            rules.setMinimum("合计 [\\u4e00-\\u9fa5]+ -?\\d+\\.?\\d*/[a-z-A-Z]+ -?\\d+\\.?\\d*/[a-z-A-Z]+");
            rules.setCredits(
                    "信用额度 \\d{4}\\(\\S+\\) [\\u4e00-\\u9fa5]+ (\\d+\\.?\\d*/[a-z-A-Z]+ ){2} \\d+\\.?\\d*/[a-z-A-Z]+");
            rules.setDetails(
                    "\\d{4} \\d{4}-\\d{2}-\\d{2} \\d{4}-\\d{2}-\\d{2} \\S+ \\S+ \\d+\\.?\\d*/[a-zA-Z]+ \\d+\\.?\\d*/[a-zA-Z]+\\([\\u4e00-\\u9fa5]+\\)");
            rules.setIntegral("个人综合积分 余额 \\d+");
            rules.setCardNumbers("0");
            rules.setTransactionDate("1");
            rules.setBillingDate("2");
            rules.setTransactionDescription("4");
        }
    }

    /**
     * 设置信用卡类型
     */
    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.ICBC_MDC;
    }

    @Override
    protected void setField(CreditBillDetail cbd, int index, String value) {
        if (index == 5) {
            String transamount = PatternMatcherUtil.getMatcherString("\\d+\\.?\\d*", value);
            if (value.indexOf("存入") != -1) {
                transamount = "-" + transamount;
            }
            cbd.setTransactionAmount(transamount);
            String currency = PatternMatcherUtil.getMatcherString("[a-zA-Z]+", value);
            cbd.setTransactionCurrency(currency);
        } else if (index == 6) {
            String accountamount = PatternMatcherUtil.getMatcherString("\\d+\\.?\\d*", value);
            if (value.indexOf("存入") != -1) {
                accountamount = "-" + accountamount;
            }
            cbd.setAccountableAmount(accountamount);
            String currency = PatternMatcherUtil.getMatcherString("[a-zA-Z]+", value);
            cbd.setAccountType(currency);
        }
    }
}
