package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExcution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Troy on 2016/10/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() throws Exception {
        List<Seckill> list =  seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void testGetById() throws Exception {
        long id =1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    //1.和2.的集成测试
    //测试代码完整逻辑。注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception{
        long id=1001;
        Exposer exposer= seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()){
            logger.info("exposer={}",exposer);
            long phone = 13526846545L;
            String md5 = exposer.getMd5();
            //捕获正常发生的异常，这些异常发生时单元测试应该通过
            try {
                SeckillExcution seckillExcution = seckillService.executeSeckill(id,phone,md5);
                logger.info("seckillExcution={}",seckillExcution);
            }catch (RepeatKillException e){
                logger.error(e.getMessage());
            }catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }else {
            //秒杀未开启或已结束
            logger.warn("export={}",exposer);
        }
    }


    //1.单元测试
    @Test
    public void testExportSeckillUrl() throws Exception {
        long id=1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);
    }

    //2.单元测试
    @Test
    public void testExecuteSeckill() throws Exception {
        long id=1000;
        long phone = 13526846545L;
        String md5 = "c5697d5582f5e09d6ba5870001b3761c";
        //捕获正常发生的异常，这些异常发生时单元测试应该通过
        try {
            SeckillExcution seckillExcution = seckillService.executeSeckill(id,phone,md5);
            logger.info("seckillExcution={}",seckillExcution);
        }catch (RepeatKillException e){
            logger.error(e.getMessage());
        }catch (SeckillCloseException e){
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testExecuteSeckillProcedure(){
        long seckillId=1000;
        long phone = 13813699999L;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExcution excution = seckillService.executeSeckillProcedure(seckillId,phone,exposer.getMd5());
            logger.info(excution.getStateInfo());
        }
    }


}