package com.cn.service.impl;

import com.cn.common.IRedisService;
import com.cn.service.CommonService;
import com.cn.service.SmsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Random;

@Repository
public class CommonServiceImpl implements CommonService {
    private static final Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);

    @Resource
    private SmsService smsService;

    @Resource
    private IRedisService redisService;

    /** 发送间隔60s */
    private static final long SEND_PERIOD = 60 * 1000L;

    @Override
    public void sendSmsVerifyCode(String phone) {

        String randomNum = randomNum(6);
        String result = null;
        if (StringUtils.isNotBlank(randomNum) && StringUtils.isNotBlank(phone)) {
            result = smsService.sendAuthCode(phone, randomNum);
        }

        if ("success".equals(result)) {
            logger.info("验证码发送成功：phone:{},authCode:{}",phone,randomNum);
            redisService.set(phone, randomNum, SEND_PERIOD);
        }else {
            logger.error("验证码发送失败：phone:{},result:{}",phone,result);
        }
        logger.info("短信验证码：{}",redisService.get(phone));
    }

    private static String random(StringBuffer buffer, int len) {
        StringBuffer sb = new StringBuffer();
        Random r = new Random();
        int range = buffer.length();

        for(int i = 0; i < len; ++i) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }

        return sb.toString();
    }

    public static String randomNum(int len) {
        return random(new StringBuffer("0123456789"), len);
    }
}
