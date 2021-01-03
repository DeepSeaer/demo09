package com.cn.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.cn.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("smsServiceImpl")
public class SmsServiceImpl implements SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Resource
    private Environment env;

    @Override
    public String sendAuthCode(String phoneNumber, String code) {

        //短信签名
        String signName = env.getProperty("sms.aliyun.signName");
        //短信模板ID
        String templateCode = env.getProperty("sms.aliyun.templateCode");
        //短信API产品名称（短信产品名固定，无需修改）
        String product = env.getProperty("sms.aliyun.product");
        //短信API产品域名（接口地址固定，无需修改）
        String doMain= env.getProperty("sms.aliyun.doMain");
        //AccessKey
        String accessKeyId= env.getProperty("sms.aliyun.accessKeyId");
        //AccessKeySecret
        String accessKeySecret= env.getProperty("sms.aliyun.accessKeySecret");
        //区域Id(固定值)
        String regionId= env.getProperty("sms.aliyun.regionId");
        //端点名称(固定值)
        String endPointName= env.getProperty("sms.aliyun.endpointName");

        //初始化ascClient需要的几个参数
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        try {
            DefaultProfile.addEndpoint(endPointName, regionId, product, doMain);
        } catch (ClientException e) {
            logger.error("调用阿里云短信网关失败, {}", e.getMessage());
            return e.getMessage();
        }
        IAcsClient acsClient = new DefaultAcsClient(profile);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        request.setMethod(MethodType.POST);
        //接收短信验证码的手机号
        request.setPhoneNumbers(phoneNumber);
        //短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
        request.setTemplateCode(templateCode);
        //模板中的变量替换JSON串,此处的值为code,如模板内容为"亲爱的${name},您的验证码为${code}"时
        request.setTemplateParam("{ \"code\":\""+code+"\"}");
        //request.setOutId("");
        try {
            SendSmsResponse result = acsClient.getAcsResponse(request);
            if(result == null){
                logger.error("调用阿里云短信网关失败, 结果为空!");
                return "result is blank";
            }
            if(!"OK".equals(result.getCode())){
                logger.error("errorCode:{} errorMessage:{}",result.getCode(), result.getMessage());
                return result.getCode();
            }
        }catch (Exception e) {
            logger.error("调用阿里云短信网关失败, {}", e.getMessage());
            return e.getMessage();
        }

        return "success";
    }
}
