package com.cn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    private DateUtil(){}

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat dateTimeMillisecondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static SimpleDateFormat dateMinuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static SimpleDateFormat compactDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private static SimpleDateFormat datesplitbydotTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static SimpleDateFormat obliqueTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static synchronized String date2String(Date date) {
        return date!=null?dateFormat.format(date):"";
    }

    public static synchronized String dateTime2String(Date date) {
        return date != null ? dateTimeFormat.format(date) : "";
    }
}
