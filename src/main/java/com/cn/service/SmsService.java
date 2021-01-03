package com.cn.service;

public interface SmsService {

    /**
     * 发送短信验证码
     * @param phoneNumber 电话号码
     * @param code        验证码
     * @return "success":成功;其他字符串:失败原因
     */
    String sendAuthCode(String phoneNumber, String code);
}
