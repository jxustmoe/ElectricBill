package cn.jxust.electricBill.dao;

import cn.jxust.electricBill.pojo.ElectricRoom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 监控相关dao
 */
public interface MonitorDao {

    /**
     * 删除提醒期已过的记录
     */
    @Delete("DELETE FROM electric_room WHERE CURRENT_DATE>expire_time")
    void delExpireRecord();

    /**
     * 查询需要通知的房间
     * 筛选条间为:余额低于提醒值、余额有过变动、距离上次通知10天以上、未超过通知有效期
     */
    @Select("SELECT * " +
            "FROM electric_room " +
            "WHERE balance < threshold " +
            "AND (balance != last_notify_balance OR last_notify_balance IS NULL) " +
            "AND (DATE_ADD(last_notify_time, INTERVAL 10 DAY) < CURRENT_DATE OR last_notify_time IS NULL) " +
            "AND CURRENT_DATE < expire_time " +
            "LIMIT 100")
    List<ElectricRoom> selNotifyRecords();

    /**
     * 获取所有记录,用于更新余额
     */
    @Select("SELECT * FROM electric_room")
    List<ElectricRoom> selAllRecords();
}
