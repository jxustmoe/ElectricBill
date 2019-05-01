package cn.jxust.electricBill.dao;

import cn.jxust.electricBill.pojo.ElectricRoom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 查询相关dao
 */
public interface QueryDao {

    /**
     * 添加一条监控记录
     *
     * @param room 房间信息
     */
    @Insert("INSERT INTO electric_room VALUES (#{areaId},#{buildId},#{floorId},#{roomId},#{areaName},#{buildName},#{floorName},#{roomName},#{balance},#{threshold},#{expireTime},#{lastNotifyTime},#{lastNotifyBalance},#{phone},#{email})")
    int addRecord(ElectricRoom room);

    /**
     * 删除一条记录
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    @Delete("DELETE FROM electric_room WHERE area_id=#{arg0} AND build_id=#{arg1} AND room_id=#{arg2}")
    int delRecord(int areaId, int buildId, int roomId);

    /**
     * 更新一条记录
     *
     * @param room 房间信息
     */
    @UpdateProvider(type = cn.jxust.electricBill.dao.DynamicSQL.class, method = "updRecord")
    int updRecord(ElectricRoom room);

    /**
     * 获取一条记录
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    @Select("SELECT * FROM electric_room WHERE area_id=#{arg0} AND build_id=#{arg1} AND room_id=#{arg2}")
    ElectricRoom selOneRecord(int areaId, int buildId, int roomId);

}
