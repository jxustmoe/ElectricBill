package cn.jxust.electricBill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    //查询相关url
    @Value("${codeUrl}")
    private String codeUrl;

    @Value("${loginUrl}")
    private String loginUrl;

    @Value("${queryBuildUrl}")
    private String queryBuildUrl;

    @Value("${queryFloorUrl}")
    private String queryFloorUrl;

    @Value("${queryRoomUrl}")
    private String queryRoomUrl;

    @Value("${queryBillUrl}")
    private String queryBillUrl;

    @Value("${ecard.withCode}")
    private boolean withCode;

    //一卡通账号密码
    @Value("${ecard.username}")
    private String username;

    @Value("${ecard.password}")
    private String password;

    //短信模板id
    @Value("${sms.templateId}")
    private int templateId;

    //发件人邮箱
    @Value("${mail.username}")
    private String mailSender;

    //发件人昵称
    @Value("${mail.nickname}")
    private String mailNickname;

    //邮件主题
    @Value("${mail.subject}")
    private String mailSubject;

    public String getCodeUrl() {
        return codeUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public String getQueryBuildUrl() {
        return queryBuildUrl;
    }

    public String getQueryFloorUrl() {
        return queryFloorUrl;
    }

    public String getQueryRoomUrl() {
        return queryRoomUrl;
    }

    public String getQueryBillUrl() {
        return queryBillUrl;
    }

    public boolean isWithCode() {
        return withCode;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTemplateId() {
        return templateId;
    }

    public String getMailSender() {
        return mailSender;
    }

    public String getMailNickname() {
        return mailNickname;
    }

    public String getMailSubject() {
        return mailSubject;
    }
}
