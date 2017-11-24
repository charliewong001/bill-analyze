package com.pay.aile.bill.analyze.banktemplate.bob;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pay.aile.bill.contant.Constant;
import com.pay.aile.bill.entity.CreditBill;
import com.pay.aile.bill.entity.CreditTemplate;
import com.pay.aile.bill.enums.CardTypeEnum;
import com.pay.aile.bill.model.AnalyzeParamsModel;
import com.pay.aile.bill.utils.PatternMatcherUtil;

/**
 * @author Charlie
 * @description 北京银行解析模板
 */
@Service
public class BOBTemplate extends AbstractBOBTemplate {
	private static final Logger logger = LoggerFactory.getLogger(BOBTemplate.class);

	@Override
	public void initRules() {
		super.initRules();
		if (rules == null) {
			rules = new CreditTemplate();
			rules.setCardtypeId(7L);
			rules.setCurrentAmount("本期应还款额：\\d+\\.?\\d*");
			rules.setMinimum("最低还款额：\\d+\\.?\\d*");
			rules.setCardNumbers("信用卡号\\d+");
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
		cardType = CardTypeEnum.BOB_DEFAULT;
	}

	@Override
	protected void initContext(AnalyzeParamsModel apm) {
		apm.setContent(parseHtml(apm.getOriginContent()));
	}

	private String getHtml(String cardUrl) {
		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(cardUrl);
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: " + getMethod.getStatusLine());
			}
			// 读取内容
			byte[] responseBody = getMethod.getResponseBody();
			// 处理内容
			html = new String(responseBody);
		} catch (Exception e) {
			logger.error("北京银行账单详细页无法访问:{}", e);
		} finally {
			getMethod.releaseConnection();
		}
		return html;
	}

	private String parseHtml(String html) {
		String cardUrl = "";
		html = html.replaceAll("&nbsp;", ""); // remove &nbsp;
		Elements links = Jsoup.parse(html).select("a");
		for (Iterator<Element> it = links.iterator(); it.hasNext();) {
			Element e = it.next();
			if ("点击查看账单详情>".equals(e.text())) {
				cardUrl = e.attr("href");
			}
			try {
				String cardHtml = getHtml(cardUrl);
				Document documentCard = Jsoup.parse(cardHtml);
				// Document documentCard = Jsoup.connect(cardUrl).get();
				Element elementCard = documentCard.getElementById("cardNum");
				String cardNo = elementCard.attr("value");

				Document document = Jsoup.parse(html);
				Elements elements = document.getElementsByTag("td");

				for (int j = 0; j < elements.size(); j++) {
					Element element = elements.get(j);
					// td需要特殊处理
					if ("td".equals("td")) {

						Elements childElements = element.getElementsByTag("td");

						if (childElements != null && childElements.size() > 1) {
							continue;
						}
					}
					String text = element.text();
					text = text.replaceAll("\\s+", "");
					element.text(text);
				}

				html = document.toString();
				html = html.replaceAll("(?is)<!DOCTYPE.*?>", ""); // remove html
																	// top
																	// infomation
				html = html.replaceAll("(?is)<!--.*?-->", ""); // remove html
																// comment
				html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove
																			// javascript
				html = html.replaceAll("(?is)<style.*?>.*?</style>", ""); // remove
																			// css
				html = html.replaceAll("(?is)<.*?>", "");

				html = html.replaceAll("\n", "");// remove \n
				html = html.replaceAll("$", "");// 去掉美元符号
				html = html.replaceAll("＄", "");
				html = html.replaceAll("￥", "");// 去掉人民币符号
				html = html.replace(",", "");// 去掉金额分隔符
				html = html.replaceAll(" {2,}", " ");// 去掉多余空格，只留一个
				// logger.info(html);
				html = html + "信用卡号" + cardNo;

				return html;
			} catch (Exception e1) {
				logger.error("北京银行抓取网页正文异常:{}", e);
			}
		}
		return html;

	}

}
