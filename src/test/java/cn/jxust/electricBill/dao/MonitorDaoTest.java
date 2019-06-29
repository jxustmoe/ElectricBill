package cn.jxust.electricBill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:spring-core.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MonitorDaoTest {
    @Autowired
    private MonitorDao monitorDao;

    @Test
    public void testDelayNotifyTime(){
        monitorDao.delayNotifyTime(1,15,192);
    }
}
