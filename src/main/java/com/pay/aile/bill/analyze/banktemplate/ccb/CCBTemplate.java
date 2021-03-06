package com.pay.aile.bill.analyze.banktemplate.ccb;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.mapper.CreditTemplateMapper;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.DateUtil;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 *
 * @author Charlie
 * @description 建设银行解析模板
 */
@Service
public class CCBTemplate extends AbstractCCBTemplate {

    @Autowired
    CreditTemplateMapper creditTemplateMapper;

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = new CreditTemplate();
            rules.setCardtypeId(13L);
            rules.setCardholder("尊敬的[\\u4e00-\\u9fa5]+");
            rules.setCycle("账单周期：\\d{4}年\\d{2}月\\d{2}日-\\d{4}年\\d{2}月\\d{2}日");
            rules.setBillDay("StatementDate \\d{4}-\\d{2}-\\d{2}");
            rules.setDueDate("DueDate \\d{4}-\\d{2}-\\d{2}");
            rules.setCredits("CreditLimit [a-zA-Z]+\\d+\\.?\\d*");
            rules.setCash("CashAdvanceLimit [a-zA-Z]+\\d+\\.?\\d*");
            rules.setCurrentAmount("争议款/笔数DisputeAmt/Nbr [\\u4e00-\\u9fa5]+（[a-zA-Z]+） \\d+\\.?\\d*");
            rules.setMinimum("争议款/笔数DisputeAmt/Nbr [\\u4e00-\\u9fa5]+（[a-zA-Z]+） \\d+\\.?\\d* \\d+\\.?\\d*");
            rules.setDetails(
                    "\\d{4}-\\d{2}-\\d{2} \\d{4}-\\d{2}-\\d{2} \\d{4}/?(\\d{4})? \\S+ [A-Za-z]+ -?\\d+\\.?\\d* [A-Za-z]+ -?\\d+\\.?\\d*");
            rules.setTransactionDate("0");
            rules.setBillingDate("1");
            rules.setCardNumbers("2");
            rules.setTransactionDescription("3");
            rules.setTransactionCurrency("4");
            rules.setTransactionAmount("5");
            rules.setAccountableAmount("7");
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
    protected void initContext(AnalyzeParamsModel apm) {
        String content = extractor.extract(apm.getOriginContent(), "font");
        apm.setContent(content);

    }

    @Override
    protected void setCardNumbers(List<CreditCard> cardList, List<CreditBill> billList, String number) {
        if (StringUtils.hasText(rules.getCardNumbers())) {
            try {
                int n = Integer.parseInt(rules.getCardNumbers());
                String[] detailArray = number.split(" ");
                String numbers = detailArray[n];
                String no = PatternMatcherUtil.getMatcherString("\\d{4}", numbers);
                CreditCard card = new CreditCard();
                card.setNumbers(no);
                if (!cardList.contains(card)) {
                    CreditBill bill = new CreditBill();
                    if (cardList.size() == billList.size()) {
                        BeanUtils.copyProperties(billList.get(0), bill, CreditBill.class);
                        billList.add(bill);
                    }
                    cardList.add(card);
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.CCB_LK;
    }
}
