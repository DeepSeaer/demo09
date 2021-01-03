package com.cn.controller;

import com.cn.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(value = "公共类服务")
@RestController
@RequestMapping(value = "/common")
public class CommonController {

    @Resource
    private CommonService commonService;

    @ApiModelProperty(value = "阿里云短信验证码发送")
    @RequestMapping(value = "/sms-send", method = RequestMethod.POST)
    public void sendAliyunSms(String phone){
        commonService.sendSmsVerifyCode(phone);
    }

}
