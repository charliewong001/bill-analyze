package com.pay.aile.bill.contant;

/**
 *
 * @author Charlie
 * @description 通用常量
 */
public class Constant {

    /**
     * 指定银行下用户邮箱对应信用卡类型的模板缓存 bank email:template
     */
    public static final String redisTemplateCache = "bill_bank_email_template";

    /**
     * 正则-匹配金额
     */
    public static final String pattern_amount = "-?\\d+\\.?\\d*";

    /**
     * 发送邮件
     */
    public static final String redisSendMail = "bill_bank_send_email";

    /**
     * Hash
     *
     * hkey:credit_card_login_status
     *
     * key:email value:loginStatus
     *
     * 存放用户邮箱登录状态
     */
    public static String REDIS_LOGGIN_STATUS = "credit_card_login_status";

    /**
     * Set
     *
     * key:credit_card_cards_${email}
     *
     * value:{{"cardNo":"1234","bankCode":"ABC"},
     * {"cardNo":"5678","bankCode":"CEB"}}
     *
     * 存放已解析的信用卡
     */
    public static String REDIS_CARDS = "credit_card_cards_";

    /**
     * key:credit_card_analysis_status_${email}
     *
     * value:5(初始值为已下载的账单数量,解析完一个则减一,若解析完成则为0)
     */
    public static String REDIS_ANALYSIS_STATUS = "credit_card_analysis_status_";

}
