package com.pay.aile.bill.service;

import java.util.List;

import com.pay.aile.bill.entity.CreditBillDetail;

/**
 *
 * @author Charlie
 * @description
 */
public interface CreditBillDetailService {
    public Long saveCreditBillDetail(CreditBillDetail billDetail);

    public void batchSaveBillDetail(List<CreditBillDetail> detailList);

}
