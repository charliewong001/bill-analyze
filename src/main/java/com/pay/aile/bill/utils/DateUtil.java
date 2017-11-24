package com.pay.aile.bill.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Charlie
 * @description
 */
public class DateUtil {

    public static Date parseDate(String datestr) {
        if (null == datestr || "".equals(datestr)) {
            return null;
        }
        try {
            String fmtstr = null;
            if (datestr.indexOf('-') > 0) {
                fmtstr = "yyyy-MM-dd";
            } else if (datestr.indexOf('年') > 0) {
                fmtstr = "yyyy年MM月dd日";
            } else if (datestr.indexOf('/') > 0) {
                fmtstr = "yyyy/MM/dd";
            } else {
                fmtstr = "yyyyMMdd";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(fmtstr);
            return sdf.parse(datestr);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getBillYearByMonth(String month) {
        int billMonth = Integer.valueOf(month);
        Calendar c = Calendar.getInstance();
        int nowMonth = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        if (nowMonth < billMonth) {
            year--;
        }
        return String.valueOf(year);
    }
}
