package cn.jxust.electricBill.service;

import cn.jxust.electricBill.pojo.ElectricRoom;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;

@ContextConfiguration(locations = {"classpath:spring-core.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class QueryServiceTest {

    @Autowired
    private QueryService service;

    @Test
    public void testAddRecord() {
        ElectricRoom room = new ElectricRoom();
        room.setAreaId(1);
        room.setBuildId(15);
        room.setFloorId(6);
        room.setRoomId(192);
        room.setAreaName("校本部");
        room.setBuildName("3栋");
        room.setFloorName("6楼");
        room.setRoomName("626");
        room.setBalance(188.0);
        room.setThreshold(200);
        room.setExpireTime(new Date());
        room.setLastNotifyTime(new Date());
        room.setLastNotifyBalance(150.0);
        room.setPhone("18812345678");
        Assert.assertEquals(true, service.addRecord(room));
    }

    @Test
    public void testUpdateRecord() {
        ElectricRoom room = new ElectricRoom();
        room.setAreaId(1);
        room.setBuildId(15);
        room.setRoomId(192);
        room.setBalance(100.0);
        room.setThreshold(100);
        room.setExpireTime(new Date());
        room.setLastNotifyTime(new Date());
        room.setLastNotifyBalance(10.0);
        room.setPhone("13488888888");
        Assert.assertEquals(true, service.updateRecord(room));
    }

    @Test
    public void testDeleteRecord() {
        Assert.assertEquals(true, service.deleteRecord(1, 15, 192));
    }

    @Test
    public void testFetchOneRecord() {
        ElectricRoom room = service.fetchOneRecord(1, 15, 192);
    }

    @Test
    public void testQuery() {
        int areaId = 1;
        int buildId = 15;
        int floorId = 6;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            try {
                service.queryBuildInfo(areaId);
//                Thread.sleep(50);
//                service.queryFloorInfo(areaId, buildId);
//                Thread.sleep(50);
//                service.queryRoomInfo(areaId, buildId, floorId);
//                Thread.sleep(50);
//                Thread.sleep(50);
//                service.queryBill(areaId,buildId,192);
                Thread.sleep(50);
                System.out.println("ok");
            } catch (IOException | TesseractException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
