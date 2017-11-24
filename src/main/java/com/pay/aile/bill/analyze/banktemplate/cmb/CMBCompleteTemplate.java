package com.pay.aile.bill.analyze.banktemplate.cmb;

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
 * @description 招商银行信用卡详细完整版账单解析模板
 */
@Service
public class CMBCompleteTemplate extends AbstractCMBTemplate {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardholder("尊敬的[\\u4e00-\\u9fa5]+，");
            rules.setYearMonth("以下是您的招商银行信用卡\\d{2}月账单");
            rules.setCycle("\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2}");
            rules.setCredits("\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2} \\d+\\.?\\d*");
            rules.setCurrentAmount("\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2} \\d+\\.?\\d* \\d+\\.?\\d*");
            rules.setMinimum("\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2} \\d+\\.?\\d* \\d+\\.?\\d* \\d+\\.?\\d*");
            rules.setDueDate(
                    "\\d{4}/\\d{2}/\\d{2}-\\d{4}/\\d{2}/\\d{2} \\d+\\.?\\d* \\d+\\.?\\d* \\d+\\.?\\d* \\d{4}/\\d{2}/\\d{2}");
            rules.setIntegral("您的信用额度一般可于缴款后立即恢复。 \\d+");
            rules.setDetails("\\d{4} \\d{4} \\S+ -?\\d+\\.?\\d* \\d{4} [a-zA-Z]+ -?\\d+\\.?\\d*");
            rules.setTransactionDescription("2");
            rules.setAccountableAmount("3");
            rules.setTransactionAmount("6");
            rules.setTransactionCurrency("5");
            rules.setCardNumbers("4");
        }
    }

    @Override
    protected void analyzeCardholder(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCardholder())) {
            String cardholder = getValueByPattern("cardholder", content, rules.getCardholder(), apm, "");
            final String finalCardholder = cardholder.replaceAll("尊敬的", "").replaceAll("先生", "").replaceAll("女士", "")
                    .replaceAll("，", "");
            cardList.forEach(card -> {
                card.setCardholder(finalCardholder);
            });
        }
    }

    @Override
    protected void analyzeYearMonth(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getYearMonth())) {
            String month = getValueByPattern("yearMonth", content, rules.getYearMonth(), apm, "");
            month = month.replaceAll("年|月|-|/", "");
            final String finalMonth = PatternMatcherUtil.getMatcherString("\\d{2}", month);
            if (StringUtils.hasText(finalMonth)) {
                String year = DateUtil.getBillYearByMonth(finalMonth);
                billList.forEach(bill -> {
                    bill.setYear(year);
                    bill.setMonth(finalMonth);
                });
            }
        }
    }

    @Override
    protected void initContext(AnalyzeParamsModel apm) {
        String content = extractor.extract(apm.getOriginContent(), "font");
        apm.setContent(content);
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.CMB_COMPLETE;
    }

    @Override
    protected void setField(CreditBillDetail cbd, int index, String value) {
        if (index == 0 || index == 1) {
            String month = value.substring(0, 2);
            String year = DateUtil.getBillYearByMonth(month);
            value = year + value;
            if (index == 0) {
                cbd.setTransactionDate(DateUtil.parseDate(value));
            } else if (index == 1) {
                cbd.setBillingDate(DateUtil.parseDate(value));
            }

        }
    }
}
