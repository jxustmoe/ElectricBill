package cn.jxust.electricBill.service;

import cn.jxust.electricBill.config.Config;
import cn.jxust.electricBill.dao.MonitorDao;
import cn.jxust.electricBill.pojo.ElectricRoom;
import com.github.pagehelper.PageHelper;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 定时查询电费并发送提醒
 */
@Service
public class MonitorService {

    @Autowired
    private MonitorDao monitorDao;

    @Autowired
    private QueryService queryService;

    @Autowired
    private SmsSingleSender smsSender;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Config config;

    private Logger logger = LoggerFactory.getLogger(MonitorService.class);

    private Logger notifyLogger = LoggerFactory.getLogger("NotifyLogger");

    /**
     * 启动监控服务
     */
    @Scheduled(cron = "0 0 6,8,10,12,14,16,18,20,22 * * ?")
    public void monitor() {
        try {
//          System.out.println("开始");
//          long start = System.currentTimeMillis();
            monitorDao.delExpireRecord();
            periodUpdate();
            periodCheck();
//          System.out.println("OK");
//          System.out.println(System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 定期更新电费
     */
    void periodUpdate() throws IOException, TesseractException {
        int pageNum = 1;
        while (true) {
            PageHelper.startPage(pageNum++, 100);
            List<ElectricRoom> rooms = monitorDao.selAllRecords();
            //记录不为0时,则表示没有遍历完,继续遍历
            if (!rooms.isEmpty()) {
                for (ElectricRoom room : rooms) {
                    //查询电费余额
                    double balance = queryService.queryBill(room.getAreaId(), room.getBuildId(), room.getRoomId());
                    //更新电费余额
                    room.setBalance(balance);
                    queryService.updateRecord(room);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //清除多余的分页请求
                PageHelper.clearPage();
                break;
            }
        }
    }

    /**
     * 定时查询电费并发送提醒
     */
    void periodCheck() {
        while (true) {
            List<ElectricRoom> rooms = monitorDao.selNotifyRecords();
            if (!rooms.isEmpty()) {
                for (ElectricRoom room : rooms) {
                    //如果发送消息成功,更新对应字段
                    if (notify(room)) {
                        //更新最后通知时的余额
                        room.setLastNotifyBalance(room.getBalance());
                        //更新最后通知时间
                        room.setLastNotifyTime(new Date());
                        queryService.updateRecord(room);
                    }
                }
            } else {
                break;
            }
        }
    }

    /**
     * 发送消息提醒
     *
     * @param room 房间信息
     */
    boolean notify(ElectricRoom room) {
        if (room.getPhone() != null && !room.getPhone().isEmpty()) {
            return sendSms(room);
        } else if (room.getEmail() != null && !room.getEmail().isEmpty()) {
            return sendEmail(room);
        } else {
            //如果手机和邮箱都为空,则该记录无效,删除该条记录
            queryService.deleteRecord(room.getAreaId(), room.getBuildId(), room.getRoomId());
            return false;
        }
    }

    /**
     * 发送短信通知
     *
     * @param room 房间信息
     * @return 是否成功
     */
    boolean sendSms(ElectricRoom room) {
        //验证手机是否合法,不合法就删除,避免疯狂打印错误日志
        boolean isValid = Pattern.matches("^1[34578]\\d{9}$", room.getPhone());
        if (!isValid) {
            queryService.deleteRecord(room.getAreaId(), room.getBuildId(), room.getRoomId());
            return false;
        }

        try {
            SmsSingleSenderResult result = smsSender.sendWithParam("86", room.getPhone(), config.getTemplateId(), new String[]{room.getBuildName() + room.getRoomName(), String.valueOf(room.getThreshold())}, "", null, null);
            if (result.result == 0) {
                notifyLogger.info(room.getAreaName() + room.getBuildName() + room.getRoomName() + "短信通知成功!");
                return true;
            } else {
                logger.error(room.getAreaName() + room.getBuildName() + room.getRoomName() + "短信通知失败,错误码:" + result.result + ",错误信息:" + result.errMsg);
                return false;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送邮件通知
     *
     * @param room 房间信息
     * @return 是否成功
     */
    boolean sendEmail(ElectricRoom room) {
        //验证邮箱是否合法,不合法就删除,避免疯狂打印错误日志
        boolean isValid = Pattern.matches("^[\\w-]+@[\\w-]+\\.(com|cn)$", room.getEmail());
        if (!isValid) {
            queryService.deleteRecord(room.getAreaId(), room.getBuildId(), room.getRoomId());
            return false;
        }

        MimeMessage       message = mailSender.createMimeMessage();
        MimeMessageHelper helper  = new MimeMessageHelper(message, "UTF-8");

        try {
            //收件人
            helper.setTo(room.getEmail());
            //发送人
            helper.setFrom(config.getMailNickname() + "<" + config.getMailSender() + ">");
            //邮件主题
            helper.setSubject(config.getMailSubject());

            //读取邮件模板
            InputStream is       = Resources.getResourceAsStream("MailTemplate.html");
            String      mailText = IOUtils.toString(is, "UTF-8");
            is.close();

            //替换变量
            mailText = mailText.replace("${room}", room.getBuildName() + room.getRoomName());
            mailText = mailText.replace("${threshold}", String.valueOf(room.getThreshold()));
            message.setContent(mailText, "text/html;charset=utf8");

            mailSender.send(message);
            notifyLogger.info(room.getAreaName() + room.getBuildName() + room.getRoomName() + "邮件通知成功!");

            return true;
        } catch (MessagingException e) {
            logger.error(e.toString());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            logger.error("邮件模板读取错误!\r\n" + e.toString());
            e.printStackTrace();
            return false;
        }
    }
}