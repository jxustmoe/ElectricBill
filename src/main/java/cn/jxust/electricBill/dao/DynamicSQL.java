package cn.jxust.electricBill.dao;

import cn.jxust.electricBill.pojo.ElectricRoom;
import org.apache.ibatis.jdbc.SQL;

public class DynamicSQL {
    public String updRecord(ElectricRoom room) {
        return new SQL() {
            {
                UPDATE("electric_room");
                if (room.getBalance() != null) {
                    SET("balance=#{balance}");
                }
                if (room.getThreshold() != null) {
                    SET("threshold=#{threshold}");
                }
                if (room.getExpireTime() != null) {
                    SET("expire_time=#{expireTime}");
                }
                if (room.getLastNotifyTime() != null) {
                    SET("last_notify_time=#{lastNotifyTime}");
                }
                if (room.getLastNotifyBalance() != null) {
                    SET("last_notify_balance=#{lastNotifyBalance}");
                }
                if (room.getPhone() != null) {
                    SET("phone=#{phone}");
                }
                if (room.getEmail() != null) {
                    SET("email=#{email}");
                }
                WHERE("area_id=#{areaId} AND build_id=#{buildId} AND room_id=#{roomId}");
            }
        }.toString();
    }
}
