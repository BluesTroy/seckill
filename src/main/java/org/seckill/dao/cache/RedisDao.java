package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by Troy on 2016/10/4.
 */
public class RedisDao {
    private JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //redis逻辑操作
        try{
            Jedis jedis = jedisPool.getResource();
            try {
                String key="seckill:"+seckillId;
                //redis并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //采用自定义序列化:速度快、空间小
                //protostuff:pojo
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes !=null){
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    return seckill;
                }
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        //set Object(Seckill) ->序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key ="seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60*60; //1个小时
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        return  null;
    }
}
