package com.pay.aile.bill.enums;

/**
 *
 * @author Charlie
 * @description 信用卡类别
 */
public enum CardTypeEnum {
    PAB_DEFAULT("PAB_DEFAULT", BankCodeEnum.PAB, "平安银行信用卡"),
    CZB_DEFAULT("CZB_DEFAULT", BankCodeEnum.CZB, "浙商银行信用卡"),
    BOB_DEFAULT("BOB_DEFAULT", BankCodeEnum.BOB, "北京银行信用卡"),
    GDB_DEFAULT("GDB_DEFAULT", BankCodeEnum.GDB, "广发银行信用卡"),
    BSB_DEFAULT("BSB_DEFAULT", BankCodeEnum.BSB, "包商银行信用卡"),
    ABC_DEFAULT("ABC_DEFAULT", BankCodeEnum.ABC, "农业银行信用卡"),
    BCM_DEFAULT("BCM_DEFAULT", BankCodeEnum.BCM, "交通银行信用卡"),
    CIB_DEFAULT("CIB_DEFAULT", BankCodeEnum.CIB, "兴业银行信用卡"),
    HXB_DEFAULT("HXB_DEFAULT", BankCodeEnum.HXB, "华夏银行信用卡"),
    BOC_DEFAULT("BOC_DEFAULT", BankCodeEnum.BOC, "中国银行信用卡"),
    CEB_DEFAULT("CEB_DEFAULT", BankCodeEnum.CEB, "光大信用卡PDF"),
    CEB_GOLD("CEB_GOLD", BankCodeEnum.CEB, "光大信用卡金卡"),
    CMBC_DEFAULT("CMBC_DEFAULT", BankCodeEnum.CMBC, "民生银行信用卡"),
    PSBC_DEFAULT("PSBC_DEFAULT", BankCodeEnum.PSBC, "邮政储蓄银行信用卡"),
    CCB_LK("CCB_LK", BankCodeEnum.CCB, "建行龙卡"),
    ICBC_MDC("ICBC_MDC", BankCodeEnum.ICBC, "工商银行牡丹卡"),
    SPDB("SPDB", BankCodeEnum.SPDB, "浦发银行信用卡"),
    CMB_YOUNG("CMB_YOUNG", BankCodeEnum.CMB, "招商银行YOUNG卡"),
    CMB_UNIONPAY("CMB_UNIONPAY", BankCodeEnum.CMB, "招商银行银联单币卡"),
    CMB_COMPLETE("CMB_COMPLETE", BankCodeEnum.CMB, "招商银行详细完整版账单"),
    CITIC_STANDARD("CITIC_STANDARD", BankCodeEnum.CITIC, "中信银行标准卡IC信用卡");

    private BankCodeEnum bankCode;
    private String cardCode;
    private String cardName;

    private CardTypeEnum(String cardCode, BankCodeEnum bankCode, String cardName) {
        this.cardCode = cardCode;
        this.bankCode = bankCode;
        this.cardName = cardName;
    }

    public BankCodeEnum getBankCode() {
        return bankCode;
    }

    public CardTypeEnum getByCardCode(String cardCode) {
        for (CardTypeEnum c : values()) {
            if (cardCode.equals(c.cardCode)) {
                return c;
            }
        }
        return null;
    }

    public String getCardCode() {
        return cardCode;
    }

    public String getCardName() {
        return cardName;
    }
}
