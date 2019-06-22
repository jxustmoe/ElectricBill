package cn.jxust.electricBill.service;

import cn.jxust.electricBill.config.Config;
import cn.jxust.electricBill.dao.QueryDao;
import cn.jxust.electricBill.pojo.ElectricRoom;
import cn.jxust.electricBill.tools.Ocr;
import cn.jxust.electricBill.tools.PostSender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * 查询电费相关信息
 */
@Service
public class QueryService {

    //配置文件
    @Autowired
    private Config config;

    @Autowired
    private QueryDao queryDao;

    //正确的验证码
    private String code;

    //验证码session
    private String codeSession;

    //登陆session
    private String loginSession;

    //登陆session过期时间
    private Date expireTime;

    //Json解析器
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 检查登陆是否有效,应在需要查询的方法执行前先执行此方法
     *
     * @throws IOException        验证码下载异常
     * @throws TesseractException 验证码解析异常
     */
    private void checkLogin() throws IOException, TesseractException {
        //先判断是否登陆过,如果登陆过,则检查session是否过期
        if (expireTime != null && expireTime.after(new Date())) {
            expireTime = new Date(System.currentTimeMillis() + 1000 * 60 * 30);
        } else {
            for (int i = 0; i <= 5; i++) {
                //需要验证码登陆时才获取验证码
                if (config.isWithCode()) {
                    getCode();
                } else {
                    //基于一卡通系统奇怪的验证机制,先发一次空请求
                    login();
                }
                if (login()) {
                    //如果登陆成功,更新session过期时间(默认30分钟)
                    expireTime = new Date(System.currentTimeMillis() + 1000 * 60 * 30);
                    return;
                } else {
                    //如果登陆失败,则等待一段时间后重试
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //5次尝试后仍然失败,则放弃尝试
            throw new IOException("login fail!");
        }
    }

    /**
     * 获取验证码及验证码session
     *
     * @throws IOException        验证码下载异常
     * @throws TesseractException 验证码解析异常
     */
    private void getCode() throws IOException, TesseractException {

        URLConnection con = new URL(config.getCodeUrl()).openConnection();
        InputStream   is  = con.getInputStream();

        //获取验证码
        code = Ocr.parse(is);
        is.close();

        //获取验证码session
        String cookie = con.getHeaderField("Set-Cookie");
        codeSession = cookie.substring(cookie.indexOf("JSESSIONID"), cookie.indexOf("JSESSIONID") + 43);

    }

    /**
     * 获取登录态及登陆session
     *
     * @throws IOException 登陆异常
     */
    private boolean login() throws IOException {

        PostSender sender = new PostSender(config.getLoginUrl());

        //禁止重定向,否则会出现重定向到主页且没有携带正确session导致重新获取到错误的session
        sender.getConnection().setInstanceFollowRedirects(false);

        //只有在启用验证码登陆时才该请求携带验证码session
        if (config.isWithCode()) {
            sender.setCookie(codeSession);
        }

        //POST请求参数
        Map<String, String> parms = new HashMap<>();
        parms.put("j_username", config.getUsername());
        parms.put("j_password", config.getPassword());
        //只有在启用验证码登陆时才带验证码信息
        if (config.isWithCode()) {
            parms.put("imageCodeName", code);
        }
        sender.setParms(parms);

        //发送POST请求
        sender.send();

        //获取登陆Session
        String cookie = sender.getHeaderField("Set-Cookie");

        //验证登陆是否成功
        if (cookie != null) {
            loginSession = cookie.substring(cookie.indexOf("JSESSIONID"), cookie.indexOf("JSESSIONID") + 43);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询单个房间的电费情况
     *
     * @param schoolArea 校区 1=本部 2=黄金 3=西区
     * @param buildId    楼栋id
     * @param roomId     房间id
     * @return 电费信息
     * @throws IOException
     * @throws TesseractException
     */
    public double queryBill(int schoolArea, int buildId, int roomId) throws IOException, TesseractException {

        //验证是否登陆
        checkLogin();

        PostSender sender = new PostSender(config.getQueryBillUrl());

        //携带登陆cookie
        sender.setCookie(loginSession);

        Map<String, String> parms = new HashMap<>();
        parms.put("sysid", String.valueOf(schoolArea));
        parms.put("roomNo", String.valueOf(roomId));
        parms.put("elcbuis", String.valueOf(buildId));
        //下面这项是固定的
        parms.put("elcarea", "1");
        sender.setParms(parms);

        sender.send();

        //获得响应输入流
        InputStream is = sender.getInputStream();

        //获取json字符串
        String json = IOUtils.toString(is, "utf-8");

        //关流
        is.close();

        //解析JSON
        Map<String, String> bill = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
        });

        if (!bill.get("retcode").equals("0")) {
            //调用一卡通api查询失败时
            throw new IOException("ecard system error! query fail!");
        }

        //结果保留两位小数
        double balance = Double.parseDouble(bill.get("restElecDegree"));
        balance = new BigDecimal(balance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        return balance;
    }

    /**
     * 获取所有楼栋、楼层、房间信息,暂时没用
     */
    @Deprecated
    public List<Map<String, String>> queryAllInfo() throws IOException, TesseractException, InterruptedException {
        int    currentArea;
        String currentAreaName;
        int    currentBuild;
        String currentBuildName;
        int    currentFloor;
        String currentFloorName;

        List<Map<String, String>> roomList = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            currentArea = i;
            switch (currentArea) {
                case 1:
                    currentAreaName = "校本部";
                    break;
                case 2:
                    currentAreaName = "黄金校区";
                    break;
                default:
                    currentAreaName = "西校区";
            }

            //获取楼栋信息
            Map<String, List<Map<String, String>>> buildInfo = queryBuildInfo(currentArea);
            Thread.sleep(300);
            for (Map<String, String> build : buildInfo.get("buils")) {
                currentBuild = Integer.parseInt(build.get("buiId"));
                currentBuildName = build.get("buiName");

                //获取楼层信息
                List<Map<String, String>> floorInfo = queryFloorInfo(currentArea, currentBuild);
                Thread.sleep(300);
                for (Map<String, String> floor : floorInfo) {
                    currentFloor = Integer.parseInt(floor.get("floorId"));
                    currentFloorName = floor.get("floorName");

                    //获取房间信息
                    List<Map<String, String>> rooms = queryRoomInfo(currentArea, currentBuild, currentFloor);
                    Thread.sleep(300);
                    for (Map<String, String> roomTmp : rooms) {
                        int    currentRoom = Integer.parseInt(roomTmp.get("roomId"));
                        double balance     = queryBill(currentArea, currentBuild, currentRoom);
                        Thread.sleep(50);

                        ElectricRoom room = new ElectricRoom();
                        room.setAreaId(currentArea);
                        room.setBuildId(currentBuild);
                        room.setFloorId(currentFloor);
                        room.setRoomId(currentRoom);
                        room.setAreaName(currentAreaName);
                        room.setBuildName(currentBuildName);
                        room.setFloorName(currentFloorName);
                        room.setRoomName(roomTmp.get("roomName").trim());
                        room.setBalance(balance);
                    }
                    switch (currentArea) {
                        case 1:
                            System.out.println("本部:" + currentBuildName + "->" + currentFloor + "楼完成");
                            break;
                        case 2:
                            System.out.println("黄金:" + currentBuildName + "->" + currentFloor + "楼完成");
                            break;
                        case 3:
                            System.out.println("西区:" + currentBuildName + "->" + currentFloor + "楼完成");
                    }
                }
            }
        }

        return roomList;

    }

    /**
     * 获取楼栋信息
     *
     * @param schoolArea 校区 1=本部 2=黄金 3=西区
     * @return 楼栋信息
     * @throws IOException
     * @throws TesseractException
     */
    public Map<String, List<Map<String, String>>> queryBuildInfo(int schoolArea) throws IOException, TesseractException {

        //验证是否登陆
        checkLogin();

        PostSender sender = new PostSender(config.getQueryBuildUrl());

        //携带登陆cookie
        sender.setCookie(loginSession);

        //POST请求参数
        Map<String, String> parms = new HashMap<>();
        parms.put("sysid", String.valueOf(schoolArea));
        sender.setParms(parms);

        sender.send();

        //获得响应输入流
        InputStream is = sender.getInputStream();

        //解析JSON
        Map<String, List<Map<String, String>>> map = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {
        });
        is.close();

        //去掉多余的字段
        map.remove("areas");
        map.remove("districts");
        //去除房间号后的空格
        for (Map<String, String> room : map.get("rooms")) {
            room.put("roomName", room.get("roomName").trim());
        }

        return map;
    }

    /**
     * 获取楼层信息
     *
     * @param schoolArea 校区 1=本部 2=黄金 3=西区
     * @param buildId    楼栋id
     * @return 楼层信息
     * @throws IOException
     * @throws TesseractException
     */
    public List<Map<String, String>> queryFloorInfo(int schoolArea, int buildId) throws IOException, TesseractException {

        //验证是否登陆
        checkLogin();

        PostSender sender = new PostSender(config.getQueryFloorUrl());

        //携带登陆cookie
        sender.setCookie(loginSession);

        //POST请求参数
        Map<String, String> parms = new HashMap<>();
        parms.put("sysid", String.valueOf(schoolArea));
        parms.put("build", String.valueOf(buildId));
        //下面2项基本是固定的
        parms.put("area", "1");
        parms.put("district", "1");
        sender.setParms(parms);

        sender.send();

        //获得响应输入流
        InputStream is = sender.getInputStream();

        //解析JSON
        Map<String, List<Map<String, String>>> map = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {
        });
        is.close();

        return map.get("floors");
    }

    /**
     * 获取房间信息
     *
     * @param schoolArea 校区 1=本部 2=黄金 3=西区
     * @param buildId    楼栋id
     * @param floorId    楼层id
     * @return 楼层信息
     * @throws IOException
     * @throws TesseractException
     */
    public List<Map<String, String>> queryRoomInfo(int schoolArea, int buildId, int floorId) throws IOException, TesseractException {

        //验证是否登陆
        checkLogin();

        PostSender sender = new PostSender(config.getQueryRoomUrl());

        //携带登陆cookie
        sender.setCookie(loginSession);

        //POST请求参数
        Map<String, String> parms = new HashMap<>();
        parms.put("sysid", String.valueOf(schoolArea));
        parms.put("build", String.valueOf(buildId));
        parms.put("floor", String.valueOf(floorId));
        //下面2项基本是固定的
        parms.put("area", "1");
        parms.put("district", "1");
        sender.setParms(parms);

        sender.send();

        //获得响应输入流
        InputStream is = sender.getInputStream();

        //解析JSON
        Map<String, List<Map<String, String>>> map = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {
        });
        is.close();
        List<Map<String, String>> rooms = map.get("rooms");

        //去除房号后的空格
        for (Map<String, String> room : rooms) {
            room.put("roomName", room.get("roomName").trim());
        }

        return rooms;
    }

    /**
     * 添加一条监控记录
     *
     * @param room 房间信息
     */
    public boolean addRecord(ElectricRoom room) {
        return queryDao.addRecord(room) == 1;
    }

    /**
     * 删除一条记录
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    public boolean deleteRecord(int areaId, int buildId, int roomId) {
        return queryDao.delRecord(areaId, buildId, roomId) == 1;
    }

    /**
     * 更新一条记录
     *
     * @param room 房间信息
     */
    public boolean updateRecord(ElectricRoom room) {
        return queryDao.updRecord(room) == 1;
    }

    /**
     * 获取一条记录
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    public ElectricRoom fetchOneRecord(int areaId, int buildId, int roomId) {
        ElectricRoom record = queryDao.selOneRecord(areaId, buildId, roomId);
        if (record != null && record.getPhone() != null) {
            //将手机号码隐藏
            String phone = record.getPhone().replaceAll("(?<=\\d{3})\\d{4}(?=\\d{4})", "****");
            record.setPhone(phone);
        }
        return record;
    }
}