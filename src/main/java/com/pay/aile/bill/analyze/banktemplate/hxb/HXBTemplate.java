package com.pay.aile.bill.analyze.banktemplate.hxb;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.mapper.CreditTemplateMapper;
import com.pay.aile.bill.model.AnalyzeParamsModel;

/**
 *
 * @author Charlie
 * @description 交通银行信用卡账单内容解析模板
 */
@Service
public class HXBTemplate extends AbstractHXBTemplate {

    @Autowired
    CreditTemplateMapper creditTemplateMapper;

    @Override
    public void initRules() {
        super.initRules();
        if (rules == null) {
            rules = creditTemplateMapper.selectById(4);
        }
        super.initDetail();

    }

    /**
     *
     * @Title: analyzeDueDate
     * @Description: 解析参数
     * @param card
     * @param content
     * @param apm
     * @return void 返回类型 @throws
     */
    @Override
    protected void analyzeCardholder(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCardholder())) {

            String cardholder = getValueByPattern("cardholder", content, rules.getCardholder(), apm, "");
            final String finalCardholder = cardholder.substring(cardholder.indexOf("的") + 1, cardholder.length() - 3);
            cardList.forEach(card -> {
                card.setCardholder(finalCardholder);
            });
        }
    }

    // /**
    // *
    // * @Title: analyzeDueDate
    // * @Description: 解析参数
    // * @param card
    // * @param content
    // * @param apm
    // * @return void 返回类型 @throws
    // */
    // @Override
    // protected void analyzeBillDate(CreditCard card, String content,
    // AnalyzeParamsModel apm) {
    // if (StringUtils.hasText(rules.getBillDay())) {
    //
    // String billDay = getValueByPattern("billDay", content,
    // rules.getBillDay(), apm, " ");
    // card.setBillDay(billDay);
    // }
    // }

    @Override
    protected void analyzeYearMonth(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getYearMonth())) {

            String yearMonth = getValueByPattern("yearMonth", content, rules.getYearMonth(), apm, "");
            String year = yearMonth.substring(0, 4);
            String month = yearMonth.substring(5, 7);
            billList.forEach(bill -> {
                bill.setYear(year);
                bill.setMonth(month);
            });
        }
    }

    @Override
    protected void setCardNumbers(List<CreditCard> cardList, List<CreditBill> billList, String number) {
        String[] detailArray = number.split(" ");
        String cardNo = detailArray[4];
        CreditCard card = new CreditCard();
        card.setNumbers(cardNo);
        if (!cardList.contains(card)) {
            cardList.add(card);
            CreditBill bill = new CreditBill();
            if (!billList.isEmpty()) {
                BeanUtils.copyProperties(billList.get(0), bill, CreditBill.class);
            }
            billList.add(bill);
        }
    }

    @Override
    protected void setCardType() {
        cardType = CardTypeEnum.HXB_DEFAULT;
    }
}
