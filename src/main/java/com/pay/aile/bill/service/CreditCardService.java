package com.pay.aile.bill.service;

import java.util.List;

import com.pay.aile.bill.entity.CreditCard;

public interface CreditCardService {
    /**
     *
     * @Title: findCreditCard
     * @Description:查询信用卡
     * @param card
     * @return CreditCard 返回类型 @throws
     */
    public CreditCard findCreditCard(CreditCard card);

    /**
     *
     * @Title: saveOrUpateCreditCard
     * @Description: 保存
     * @param card
     * @return Long 返回类型 @throws
     */
    public Long saveOrUpateCreditCard(CreditCard card);

    public void saveOrUpateCreditCard(List<CreditCard> cardList);
}
