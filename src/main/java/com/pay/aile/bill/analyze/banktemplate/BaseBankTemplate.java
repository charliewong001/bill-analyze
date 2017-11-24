package com.pay.aile.bill.analyze.banktemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.analyze.BankMailAnalyzerTemplate;
import com.pay.aile.bill.analyze.MailContentExtractor;
import com.pay.aile.bill.config.TemplateCache;
import com.pay.aile.bill.contant.Constant;
import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditBillDetail;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.exception.AnalyzeBillException;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.model.AnalyzeResult;
import com.pay.aile.bill.service.CreditBillDetailService;
import com.pay.aile.bill.service.CreditBillService;
import com.pay.aile.bill.service.CreditCardService;
import com.pay.aile.bill.utils.DateUtil;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 *
 * @author Charlie
 * @description 卡种解析基础模板
 */
public abstract class BaseBankTemplate
        implements BankMailAnalyzerTemplate, Comparable<BaseBankTemplate>, InitializingBean {
    @Resource(name = "textExtractor")
    protected MailContentExtractor extractor;
    /**
     * 统计每一种模板的调用次数 用于不同卡种之间的排序,调用次数高的排位靠前
     */
    private volatile int count;
    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 信用卡类型 由子类去初始化自己是什么信用卡类型
     */
    protected CardTypeEnum cardType;

    @Resource
    protected CreditBillDetailService creditBillDetailService;

    @Resource
    protected CreditBillService creditBillService;

    @Resource
    protected CreditCardService creditCardService;

    /**
     * 存放明细规则的map
     */
    protected Map<Integer, String> detailMap = new HashMap<Integer, String>();
    /**
     * 模板解析邮件时需要的关键字及对应的规则 key:到期还款日/应还款金额.eg value:规则 根据银行和信用卡类型,从缓存中初始化
     */
    protected CreditTemplate rules;

    /**
     * 默认的分隔符
     */
    protected String defaultSplitSign = " ";

    @Override
    public void afterPropertiesSet() throws Exception {
        setCardType();
    }

    @Override
    public void analyze(AnalyzeParamsModel apm) throws AnalyzeBillException {
        count++;
        initRules();
        initDetail();
        if (rules != null) {
            apm.setCardtypeId(rules.getCardtypeId());
        }

        beforeAnalyze(apm);
        analyzeInternal(apm);
        afterAnalyze(apm);
    }

    /**
     * 用于不同卡种之间的排序,调用次数高的排位靠前
     */
    @Override
    public int compareTo(BaseBankTemplate o) {
        if (o == null) {
            return 1;
        }
        return count > o.count ? 1 : -1;
    }

    @Override
    public void handleResult(AnalyzeParamsModel apm) {
        handleResultInternal(apm);
    }

    /**
     *
     * @param apm
     * @throws AnalyzeBillException
     */
    protected void afterAnalyze(AnalyzeParamsModel apm) throws AnalyzeBillException {
        checkCardAndBill(apm);
    }

    protected void analyzeBillDate(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getBillDay())) {
            String billDay = getValueByPattern("billDay", content, rules.getBillDay(), apm, " ");
            if (StringUtils.hasText(billDay)) {
                billDay = billDay.replaceAll("年", "").replaceAll("月", "").replaceAll("日", "").replaceAll("\\s+", "");
                final String finalBillDay = billDay.substring(billDay.length() - 2);
                cardList.forEach(card -> {
                    card.setBillDay(finalBillDay);
                });
            }
        }
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
    protected void analyzeCardholder(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCardholder())) {
            String cardholder = getValueByPattern("cardholder", content, rules.getCardholder(), apm, "");
            final String finalCardholder = cardholder.replaceAll("尊敬的", "").replaceAll("先生", "").replaceAll("女士", "")
                    .replaceAll("您好", "");
            cardList.forEach(card -> {
                card.setCardholder(finalCardholder);
            });
        }
    }

    protected void analyzeCardNo(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCardNumbers())) {
            Exception error = null;
            try {
                Integer.valueOf(rules.getCardNumbers());
            } catch (Exception e) {
                error = e;
            }
            if (error != null) {
                List<String> cardNos = getValueListByPattern("cardNumbers", content, rules.getCardNumbers(),
                        defaultSplitSign);
                cardNos = PatternMatcherUtil.getMatcher("\\d{4}", cardNos);
                if (!cardNos.isEmpty()) {
                    for (int i = 0; i < cardNos.size(); i++) {
                        String cardNo = cardNos.get(i);
                        CreditCard card = new CreditCard();
                        card.setNumbers(cardNo);
                        if (!cardList.contains(card)) {
                            cardList.add(card);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @Title: analyzeCash
     * @Description:预借现金
     * @param bill
     * @param content
     * @param apm
     * @return void 返回类型 @throws
     */
    protected void analyzeCash(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCash())) {
            List<String> cash = getValueListByPattern("cash", content, rules.getCash(), " ");
            cash = PatternMatcherUtil.getMatcher(Constant.pattern_amount, cash);
            if (!cash.isEmpty()) {
                for (int i = 0; i < cash.size(); i++) {
                    CreditBill bill = billList.get(i);
                    bill.setCash(new BigDecimal(cash.get(i)));
                }
            }
        }
    }

    /**
     *
     * @Title: analyzeCredits
     * @Description: 信用额度
     * @param bill
     * @param content
     * @param apm
     * @return void 返回类型 @throws
     */
    protected void analyzeCredits(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCredits())) {
            List<String> credits = getValueListByPattern("credits", content, rules.getCredits(), " ");
            credits = PatternMatcherUtil.getMatcher(Constant.pattern_amount, credits);
            if (!credits.isEmpty()) {
                for (int i = 0; i < credits.size(); i++) {
                    CreditBill bill = billList.get(i);
                    bill.setCredits(new BigDecimal(credits.get(i)));
                }
            }
        }
    }

    /**
     *
     * @Title: analyzeCurrentAmount
     * @Description: 应还款额
     * @param bill
     * @param content
     * @param apm
     * @return void 返回类型 @throws
     */
    protected void analyzeCurrentAmount(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCurrentAmount())) {
            List<String> currentAmountList = getValueListByPattern("currentAmount", content, rules.getCurrentAmount(),
                    defaultSplitSign);
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

    protected void analyzeCycle(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getCycle())) {

            String cycle = getValueByPattern("cycle", content, rules.getCycle(), apm, defaultSplitSign);
            String[] sa = cycle.split("-");
            billList.forEach(bill -> {
                bill.setBeginDate(DateUtil.parseDate(sa[0]));
                bill.setEndDate(DateUtil.parseDate(sa[1]));
            });
        }
    }

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

    /**
     *
     * @Title: analyzeDueDate
     * @Description: 还款日
     * @param bill
     * @param content
     * @param apm
     * @return void 返回类型 @throws
     */
    protected void analyzeDueDate(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getDueDate())) {
            String date = getValueByPattern("dueDate", content, rules.getDueDate(), apm, defaultSplitSign);
            billList.forEach(bill -> {
                bill.setDueDate(DateUtil.parseDate(date));
            });

        }
    }

    /**
     * @Description: 积分余额
     * @param card
     * @param content
     * @param apm
     */
    protected void analyzeIntegral(List<CreditCard> cardList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getIntegral())) {
            String integral = getValueByPattern("integral", content, rules.getIntegral(), apm, " ");
            final String finalIntegral = PatternMatcherUtil.getMatcherString("\\d+\\.?\\d*", integral);
            if (StringUtils.hasText(finalIntegral)) {
                cardList.forEach(card -> {
                    card.setIntegral(new BigDecimal(finalIntegral));
                });
            }
        }
    }

    protected void analyzeInternal(AnalyzeParamsModel apm) throws AnalyzeBillException {
        logger.info("账单内容：{}", apm.toString());
        String content = apm.getContent();
        AnalyzeResult ar = new AnalyzeResult();
        // ka
        List<CreditCard> cardList = ar.getCardList();
        // 账单
        List<CreditBill> billList = ar.getBillList();

        List<CreditBillDetail> detailList = ar.getDetailList();
        if (rules == null) {
            throw new AnalyzeBillException("账单模板规则未初始化");
        }

        // 本期账单金额
        analyzeCurrentAmount(billList, content, apm);
        // 最低还款额
        analyzeMinimum(billList, content, apm);
        // 卡号
        analyzeCardNo(cardList, content, apm);
        // 年月
        analyzeYearMonth(billList, content, apm);
        // 账单周期
        analyzeCycle(billList, content, apm);
        // 还款日
        analyzeDueDate(billList, content, apm);
        // 信用额度
        analyzeCredits(billList, content, apm);
        // 取取现金额
        analyzeCash(billList, content, apm);
        // 消费明细
        analyzeDetails(detailList, billList, content, apm, cardList);
        // 持卡人
        analyzeCardholder(cardList, content, apm);
        // 账单日
        analyzeBillDate(cardList, content, apm);
        // 积分余额
        analyzeIntegral(cardList, content, apm);
        // 设置卡片
        setCard(cardList, billList, apm);
        apm.setResult(ar);
    }

    protected void analyzeMinimum(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getMinimum())) {
            List<String> minimumList = getValueListByPattern("minimum", content, rules.getMinimum(), defaultSplitSign);
            minimumList = PatternMatcherUtil.getMatcher(Constant.pattern_amount, minimumList);
            if (!minimumList.isEmpty()) {
                minimumList = minimumList.stream().map(item -> {
                    if (item.startsWith("-")) {
                        return item.replaceAll("-", "");
                    } else {
                        return item;
                    }
                }).collect(Collectors.toList());
                for (int i = 0; i < minimumList.size(); i++) {
                    CreditBill bill = null;
                    if (!billList.isEmpty()) {
                        bill = billList.get(i);
                    } else {
                        bill = new CreditBill();
                        billList.add(bill);
                    }
                    bill.setMinimum(new BigDecimal(minimumList.get(i)));
                }
            }
        }
    }

    protected void analyzeYearMonth(List<CreditBill> billList, String content, AnalyzeParamsModel apm) {
        if (StringUtils.hasText(rules.getYearMonth())) {
            String yearMonth = getValueByPattern("yearMonth", content, rules.getYearMonth(), apm, "");
            yearMonth = yearMonth.replaceAll("年|月|-|/", "");
            yearMonth = PatternMatcherUtil.getMatcherString("\\d{6}", yearMonth);
            if (StringUtils.hasText(yearMonth)) {
                String year = yearMonth.substring(0, 4);
                String month = yearMonth.substring(4);
                billList.forEach(bill -> {
                    bill.setYear(year);
                    bill.setMonth(month);
                });
            }
        }
    }

    /**
     *
     * @param apm
     */
    protected void beforeAnalyze(AnalyzeParamsModel apm) {
        initContext(apm);
    }

    /**
     *
     * @throws AnalyzeBillException
     * @Title: checkCardAndBill @Description: 检查数据合法性 @param apm @return void
     *         返回类型 @throws
     */
    protected void checkCardAndBill(AnalyzeParamsModel apm) throws AnalyzeBillException {
        if (apm.getResult().getCardList().isEmpty()) {
            apm.setResult(null);
            throw new AnalyzeBillException("未抓取到卡号");
        }
        if (apm.getResult().getBillList().isEmpty()) {
            apm.setResult(null);
            throw new AnalyzeBillException("未抓取到账单");
        }
        // 检查是否包含卡号和持卡人
        for (CreditCard card : apm.getResult().getCardList()) {
            if (!StringUtils.hasText(card.getNumbers())) {
                apm.setResult(null);
                throw new AnalyzeBillException("无法获取卡号");
            }
        }
        for (CreditBill bill : apm.getResult().getBillList()) {
            if (bill.getDueDate() == null && bill.getCurrentAmount() == null) {
                apm.setResult(null);
                throw new AnalyzeBillException("应还款日期和应还款额都为空!");
            }
        }
    }

    protected String getValueByPattern(String key, String content, String ruleValue, AnalyzeParamsModel apm,
            String splitSign) {

        if (StringUtils.hasText(ruleValue)) {

            List<String> list = PatternMatcherUtil.getMatcher(ruleValue, content);
            if (list.isEmpty()) {
                // handleNotMatch(key, rules.getDueDate(), apm);
                return "";
            }
            String result = list.get(0);
            if ("".equals(splitSign)) {
                return result;
            } else {
                String[] sa = result.split(splitSign);
                String value = sa[sa.length - 1];
                return value;
            }

        }
        return "";
    }

    protected List<String> getValueListByPattern(String key, String content, String ruleValue, String splitSign) {
        if (StringUtils.hasText(ruleValue)) {
            List<String> list = PatternMatcherUtil.getMatcher(ruleValue, content);
            if (list.isEmpty()) {
                return list;
            }
            if ("".equals(splitSign)) {
                return list;
            } else {
                return list.stream().map((item) -> {
                    String[] sa = item.split(splitSign);
                    return sa[sa.length - 1];
                }).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    protected void handleNotMatch(String key, String reg, AnalyzeParamsModel apm) {
        apm.setResult(null);
        throw new RuntimeException(String.format("未找到匹配值,bank=%s,cardType=%s,key=%s,reg=%s",
                cardType.getBankCode().getBankCode(), cardType.getCardCode(), key, reg));
    }

    @Transactional
    protected void handleResultInternal(AnalyzeParamsModel apm) {
        Long emailId = apm.getEmailId();
        // 保存或更新卡信息
        List<CreditCard> cardList = apm.getResult().getCardList();
        creditCardService.saveOrUpateCreditCard(cardList);
        // 保存账单
        List<CreditBill> billList = apm.getResult().getBillList();
        for (int i = 0; i < billList.size(); i++) {
            CreditBill bill = billList.get(i);
            bill.setCardId(cardList.get(i).getId());
            bill.setEmailId(emailId);
            bill.setSentDate(apm.getSentDate());
        }
        if (!billList.isEmpty()) {
            creditBillService.saveCreditBill(billList);
        }
        List<CreditBillDetail> detailList = apm.getResult().getDetailList();
        List<CreditBillDetail> saveDetailList = new ArrayList<CreditBillDetail>();
        if (!detailList.isEmpty()) {
            billList.forEach(bill -> {
                detailList.forEach(detail -> {
                    CreditBillDetail saveDetail = new CreditBillDetail();
                    BeanUtils.copyProperties(detail, saveDetail, CreditBillDetail.class);
                    saveDetail.setBillId(bill.getId());
                    saveDetailList.add(saveDetail);
                });
            });
            creditBillDetailService.batchSaveBillDetail(saveDetailList);
        }

    }

    /**
     *
     * @Title: initContext @Description: 初始化需要解析的内容 @param @param apm 参数 @return
     *         void 返回类型 @throws
     */
    protected void initContext(AnalyzeParamsModel apm) {
        String content = extractor.extract(apm.getOriginContent(), "td");
        apm.setContent(content);
    }

    protected void initDetail() {
        if (rules != null && StringUtils.hasText(rules.getDetails())) {
            if (StringUtils.hasText(rules.getTransactionDate())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getTransactionDate()), "transactionDate");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            if (StringUtils.hasText(rules.getBillingDate())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getBillingDate()), "billingDate");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            if (StringUtils.hasText(rules.getTransactionDescription())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getTransactionDescription()), "transactionDescription");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            if (StringUtils.hasText(rules.getTransactionCurrency())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getTransactionCurrency()), "transactionCurrency");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            if (StringUtils.hasText(rules.getTransactionAmount())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getTransactionAmount()), "transactionAmount");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            if (StringUtils.hasText(rules.getAccountableAmount())) {
                try {
                    detailMap.put(Integer.parseInt(rules.getAccountableAmount()), "accountableAmount");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

        }
    }

    /**
     * 获取模板对应的关键字
     */
    protected void initRules() {
        // 根据cardCode从缓存中获取对应的规则
        String cardCode = cardType.getCardCode();
        // 从缓存中找模板
        // rules = JedisClusterUtils.getBean(Constant.redisTemplateRuleCache +
        // cardCode,
        // CreditTemplate.class);
        rules = TemplateCache.templateCache.get(cardCode);

    }

    /**
     *
     * @Title: setCard
     * @Description: 设置行用卡
     * @param card
     * @param bill
     * @param apm
     * @return void 返回类型 @throws
     */
    protected void setCard(List<CreditCard> cardList, List<CreditBill> billList, AnalyzeParamsModel apm) {
        for (int i = 0; i < cardList.size(); i++) {
            CreditCard card = cardList.get(i);
            CreditBill bill = billList.get(i);
            card.setBankId(new Long(apm.getBankId()));
            card.setCash(bill.getCash());
            card.setCredits(bill.getCredits());
        }

    }

    /**
     *
     * @Title: setCardNumbers @Description: 卡号
     * @param card
     * @param number
     * @return void 返回类型 @throws
     */
    protected void setCardNumbers(List<CreditCard> cardList, List<CreditBill> billList, String number) {
        if (StringUtils.hasText(rules.getCardNumbers())) {
            try {
                int n = Integer.parseInt(rules.getCardNumbers());
                String[] detailArray = number.split(" ");
                String cardNo = detailArray[n];
                CreditCard card = new CreditCard();
                card.setNumbers(cardNo);
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

    /**
     * 设置信用卡类型
     */
    protected void setCardType() {

    }

    protected CreditBillDetail setCreditBillDetail(String detail) {
        CreditBillDetail cbd = new CreditBillDetail();
        String[] detailArray = detail.split(" ");
        for (Integer i = 0; i < detailArray.length; i++) {
            if (detailMap.containsKey(i)) {
                Field field;
                try {
                    field = CreditBillDetail.class.getDeclaredField(detailMap.get(i));
                    if (field.getType() == Date.class) {
                        ReflectionUtils.setField(field, cbd, DateUtil.parseDate(detailArray[i]));
                    } else {
                        ReflectionUtils.setField(field, cbd, detailArray[i]);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }

            } else {
                ///
                setField(cbd, i, detailArray[i]);
            }

        }
        return cbd;

    }

    protected void setField(CreditBillDetail cbd, int index, String value) {

    }

    public CardTypeEnum getCardType() {
        return cardType;
    }
}
