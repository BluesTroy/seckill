USE seckill;
--  秒杀执行存储过程
DELIMITER $$ -- console ; 换行符
-- 定义存储过程
--  参数：in 输入参数；out 输出参数
#  row_count(): 返回上一条修改类型sql(delete,insert,update)的影响行数
#  row_count: 0: 未修改数据；>0 :表示修饰的行数；<0: sql错误/未执行修改sql

DROP PROCEDURE IF EXISTS `seckill`.`execute_seckill`;
CREATE PROCEDURE `seckill`.`execute_seckill`
  (IN v_seckill_id BIGINT, IN v_phone bigint,
  IN v_kill_time TIMESTAMP , OUT r_result INT )

  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION ;
    INSERT IGNORE INTO success_killed(seckill_id,user_phone,create_time,state)
      VALUES (v_seckill_id,v_phone,v_kill_time,0);
    SELECT row_count() INTO insert_count;
    IF (insert_count=0) THEN
      ROLLBACK ;
      SET r_result=-1; #重复秒杀
    ELSEIF (insert_count<0) THEN
      ROLLBACK ;
      set r_result = -2; #异常出错
    ELSE
      UPDATE seckill SET number = number -1
      WHERE seckill_id = v_seckill_id
        AND end_time > v_kill_time
        AND start_time < v_kill_time AND number>0;
      SELECT row_count() into insert_count;
      IF (insert_count=0) THEN
        ROLLBACK ;
        set r_result =0; #秒杀结束
      ELSEIF (insert_count<0) THEN
        ROLLBACK ;
        set r_result = -2; #异常出错
      ELSE
        COMMIT ;
        SET r_result=1; #秒杀成功
      END IF;
    END IF ;
  END;
$$

# 存储过程定义结束

DELIMITER ;

set @r_result=-3;
SELECT @r_result;

call execute_seckill(1000,1352620000,now(),@r_result);
SELECT @r_result;

SELECT * FROM success_killed;
