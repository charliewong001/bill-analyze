package com.pay.aile.bill.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.pay.aile.bill.entity.CreditCard;
import com.pay.aile.bill.mapper.CreditCardMapper;
import com.pay.aile.bill.service.CreditCardService;

@Service
public class CreditCardServiceImpl implements CreditCardService {
    @Autowired
    private CreditCardMapper creditCardMapper;

    /**
     *
     * @Title: findCreditCard
     * @Description:查询信用卡
     * @param card
     * @return CreditCard 返回类型 @throws
     */
    @Override
    public CreditCard findCreditCard(CreditCard card) {
        return creditCardMapper.selectOne(card);
    }

    /**
     *
     * @Title: saveOrUpateCreditCard
     * @Description: 保存
     * @param card
     * @return Long 返回类型 @throws
     */
    @Override
    public Long saveOrUpateCreditCard(CreditCard card) {
        // 根据卡号查询
        CreditCard oldCard = new CreditCard();
        oldCard.setNumbers(card.getNumbers());
        oldCard = creditCardMapper.selectOne(card);
        if (oldCard != null) {
            card.setId(oldCard.getId());
        }
        try {
            if (card.getId() != null) {
                creditCardMapper.updateById(card);
            } else {
                creditCardMapper.insert(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return card.getId();
    }

    @Transactional
    @Override
    public void saveOrUpateCreditCard(List<CreditCard> cardList) {
        List<CreditCard> existsList = creditCardMapper.selectList(new EntityWrapper<CreditCard>().in("numbers",
                cardList.stream().map(item -> item.getNumbers()).collect(Collectors.toList())));
        List<CreditCard> insertList = Collections.emptyList();
        if (existsList != null && !existsList.isEmpty()) {
            cardList.stream().filter(item -> existsList.contains(item)).forEach(updateCreditCard -> {
                creditCardMapper.update(updateCreditCard,
                        new EntityWrapper<CreditCard>().eq("numbers", updateCreditCard.getNumbers()));
            });
            insertList = cardList.stream().filter(item -> !existsList.contains(item)).collect(Collectors.toList());
        } else {
            insertList = cardList;
        }
        if (!insertList.isEmpty()) {
            creditCardMapper.batchInsert(insertList);
        }
        final List<CreditCard> finalInsertList = insertList;
        cardList = cardList.stream().map(item -> {
            int index = existsList.indexOf(item);
            if (index != -1) {
                item.setId(existsList.get(index).getId());
            } else {
                index = finalInsertList.indexOf(item);
                if (index != -1) {
                    item.setId(finalInsertList.get(index).getId());
                }
            }
            return item;
        }).collect(Collectors.toList());
    }

}
