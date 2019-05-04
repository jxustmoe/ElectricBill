package cn.jxust.electricBill.controller;

import cn.jxust.electricBill.pojo.ElectricRoom;
import cn.jxust.electricBill.pojo.Message;
import cn.jxust.electricBill.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BillController {

    @Autowired
    private QueryService queryService;

    private Logger logger = LoggerFactory.getLogger(BillController.class);

    /**
     * 查询单间寝室的电费情况
     *
     * @param areaId    校区 1=本部 2=黄金 3=西区
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    @RequestMapping("/queryBill")
    public Message queryBill(Integer areaId, Integer buildId, Integer roomId) {
        if (areaId == null || buildId == null || roomId == null) {
            return new Message(500, "缺少参数!");
        }
        try {
            double balance = queryService.queryBill(areaId, buildId, roomId);
            return new Message(0, balance);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return new Message(500, "服务器内部错误!");
        }
    }

    /**
     * 查询对应校区楼栋信息
     *
     * @param areaId 校区 1=本部 2=黄金 3=西区
     */
    @RequestMapping("/queryBuilds")
    public Message queryBuild(Integer areaId) {
        if (areaId == null) {
            return new Message(500, "缺少参数!");
        }
        try {
            Map<String, List<Map<String, String>>> builds = queryService.queryBuildInfo(areaId);
            return new Message(0, builds);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return new Message(500, "服务器内部错误!");
        }
    }

    /**
     * 查询对应楼栋的楼层信息
     *
     * @param areaId    校区 1=本部 2=黄金 3=西区
     * @param buildId 楼栋id
     */
    @RequestMapping("/queryFloors")
    public Message queryFloor(Integer areaId, Integer buildId) {
        if (areaId == null || buildId == null) {
            return new Message(500, "缺少参数!");
        }
        try {
            List<Map<String, String>> floors = queryService.queryFloorInfo(areaId, buildId);
            return new Message(0, floors);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return new Message(500, "服务器内部错误!");
        }
    }

    /**
     * 查询对应楼层的房间信息
     *
     * @param areaId    校区 1=本部 2=黄金 3=西区
     * @param buildId 楼栋id
     * @param floorId 楼层id
     */
    @RequestMapping("/queryRooms")
    public Message queryRoom(Integer areaId, Integer buildId, Integer floorId) {
        if (areaId == null || buildId == null || floorId == null) {
            return new Message(500, "缺少参数!");
        }
        try {
            List<Map<String, String>> rooms = queryService.queryRoomInfo(areaId, buildId, floorId);
            return new Message(0, rooms);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return new Message(500, "服务器内部错误!");
        }
    }

    /**
     * 添加监控记录
     *
     * @param room 具体参数有:areaId、buildId、floorId、roomId、areaName、buildName、floorName、roomName、threshold、expireTime、phone、email
     */
    @RequestMapping("/addRecord")
    public Message addRecord(ElectricRoom room) {
        if (room.isEmpty()) {
            return new Message(500, "缺少参数!");
        }
        if (queryService.addRecord(room)) {
            return new Message(0, "记录添加成功!");
        } else {
            return new Message(500, "记录添加失败!");
        }
    }

    /**
     * 删除监控记录
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    @RequestMapping("/deleteRecord")
    public Message deleteRecord(Integer areaId, Integer buildId, Integer roomId) {
        if (areaId == null || buildId == null || roomId == null) {
            return new Message(500, "缺少参数!");
        }
        if (queryService.deleteRecord(areaId, buildId, roomId)) {
            return new Message(0, "删除记录成功!");
        } else {
            return new Message(500, "删除记录失败!");
        }
    }

    /**
     * 修改监控记录
     *
     * @param room 具体参数有:areaId、buildId、roomId、threshold、expireTime、phone、email
     */
    @RequestMapping("/updateRecord")
    public Message updateRecord(ElectricRoom room) {
        if (room.getAreaId() == 0 || room.getBuildId() == 0 || room.getRoomId() == 0) {
            return new Message(500, "缺少参数!");
        }
        if (queryService.updateRecord(room)) {
            return new Message(0, "修改记录成功");
        } else {
            return new Message(500, "修改记录失败");
        }
    }

    /**
     * 查看监控记录,用于绑定信息界面
     *
     * @param areaId  校区id
     * @param buildId 楼栋id
     * @param roomId  房间id
     */
    @RequestMapping("/getRecord")
    public Message getRecord(Integer areaId, Integer buildId, Integer roomId) {
        if (areaId == null || buildId == null || roomId == null) {
            return new Message(500, "缺少参数!");
        }
        ElectricRoom record = queryService.fetchOneRecord(areaId, buildId, roomId);
        return new Message(0, record);
    }

    /**
     * 异常处理器
     */
    @ExceptionHandler(Exception.class)
    public Message exceptionHandler(Exception e) {
        e.printStackTrace();
        logger.error(e.toString());
        return new Message(500, "服务器内部错误!");
    }
}
