package com.pay.aile.bill.analyze.banktemplate.cmb;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditBillDetail;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.DateUtil;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 *
 * @author Charlie
 * @description 招商银行银联单币卡解析模板
 */
@Service
public class CMBUnionPayTemplate extends AbstractCMBTemplate {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardtypeId(3L);
            rules.setCardholder("尊敬的 [\\u4e00-\\u9fa5]+");
            rules.setYearMonth("\\d{4} 年 \\d{2} 月");
            rules.setDueDate("\\d{2} 月 \\d{2} 日");
            rules.setCurrentAmount("本期应还金额NewBalance \\d+\\.?\\d*");
            rules.setDetails("\\d{4} \\d{8} \\d{2}:\\d{2}:\\d{2} \\S+ \\S+ \\d+\\.?\\d*");
            rules.setTransactionDate("1");
            rules.setTransactionCurrency("3");
            rules.setTransactionAmount("5");
        }
    }

    @Override
    protected void analyzeDetails(List<CreditBillDetail> detailList, List<CreditBill> billList, String content,
            AnalyzeParamsModel apm, List<CreditCard> cardList) {
        List<String> list = null;
        if (StringUtils.hasText(rules.getDetails())) {
            // 交易明细
            list = PatternMatcherUtil.getMatcher(rules.getDetails(), content);
            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    String s = list.get(i);
                    detailList.add(setCreditBillDetail(s));
                    setCardNumbers(cardList, billList, s);
                }
            }
        }
    }

    @Override
    protected void analyzeDueDate(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getDueDate())) {
            try {
                String ruleValue = rules.getDueDate();
                List<String> list = PatternMatcherUtil.getMatcher(ruleValue, content);
                if (!list.isEmpty()) {

                    String date = list.get(0);
                    date = date.replaceAll("\\s+", "").replaceAll("月", "-").replaceAll("日", "");
                    String monthStr = date.split("-")[0];
                    int billMonth = Integer.valueOf(monthStr);
                    Calendar c = Calendar.getInstance();
                    int nowMonth = c.get(Calendar.MONTH) + 1;
                    int year = c.get(Calendar.YEAR);
                    if (nowMonth == 12 && billMonth == 1) {
                        year++;
                    }
                    final String finalDate = String.valueOf(year) + "-" + date;
                    billList.forEach(bill -> {
                        bill.setDueDate(DateUtil.parseDate(finalDate));
                    });
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void analyzeYearMonth(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getYearMonth())) {
            String yearMonth = super.getValueByPattern("yearMonth", content, rules.getYearMonth(), apm, "");
            yearMonth = yearMonth.replaceAll("\\s+", "");
            if (StringUtils.hasText(yearMonth)) {
                String year = yearMonth.substring(0, 4);
                String month = yearMonth.substring(5, 7);
                billList.forEach(bill -> {
                    bill.setYear(year);
                    bill.setMonth(month);
                });
            }
        }
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.CMB_UNIONPAY;
    }

}
