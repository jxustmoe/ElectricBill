package cn.jxust.electricBill.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 房间信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElectricRoom {

    @JsonIgnore
    //校区id
    private int areaId;

    @JsonIgnore
    //楼栋id
    private int buildId;

    @JsonIgnore
    //楼层id
    private int floorId;

    @JsonIgnore
    //房间id
    private int roomId;

    @JsonIgnore
    //校区字
    private String areaName;

    @JsonIgnore
    //楼栋名
    private String buildName;

    @JsonIgnore
    //楼层名
    private String floorName;

    @JsonIgnore
    //房间号
    private String roomName;

    @JsonIgnore
    //电费余额
    private Double balance;

    //触发通知的电费值
    private Integer threshold;

    //通知有效期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireTime;

    @JsonIgnore
    //最近一次通知的时间
    private Date lastNotifyTime;

    @JsonIgnore
    //最近一次通知时的电费余额
    private Double lastNotifyBalance;

    //联系电话
    private String phone;

    //邮箱
    private String email;

    @JsonIgnore
    public boolean isEmpty() {
        return areaId == 0 || buildId == 0 || floorId == 0 || roomId == 0 || areaName == null || buildName == null || floorName == null || roomName == null || threshold == null || expireTime == null || (phone == null && email == null);
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public int getBuildId() {
        return buildId;
    }

    public void setBuildId(int buildId) {
        this.buildId = buildId;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Date lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public Double getLastNotifyBalance() {
        return lastNotifyBalance;
    }

    public void setLastNotifyBalance(Double lastNotifyBalance) {
        this.lastNotifyBalance = lastNotifyBalance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
