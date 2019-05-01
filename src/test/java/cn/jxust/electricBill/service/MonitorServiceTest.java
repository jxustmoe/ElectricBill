package cn.jxust.electricBill.service;

import cn.jxust.electricBill.pojo.ElectricRoom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:spring-core.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MonitorServiceTest {

    @Autowired
    private MonitorService service;

    @Test
    public void testPeriodCheck(){
        service.periodCheck();
    }

    @Test
    public void testMonitor(){
        service.monitor();
    }

    @Test
    public void testNotify(){
        ElectricRoom room = new ElectricRoom();
        room.setBuildName("3栋");
        room.setRoomName("626");
        room.setThreshold(10);
        room.setPhone("17746648477");
        room.setEmail("543000463@qq.com");
        service.notify(room);
    }
}
