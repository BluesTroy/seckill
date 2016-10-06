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
    private JedisPool jedisPool;    //redis连接池

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //获取Seckill对象的sechema，sechema保存了类的属性等信息
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    /**
     * 从缓存中读取seckill对象
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId){
        //redis逻辑操作
        try{
            //使用了redis的客户端Jedis来操作缓存
            Jedis jedis = jedisPool.getResource();
            try {
                String key="seckill:"+seckillId;
                //redis并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //采用自定义序列化:速度快、空间小
                //protostuff:pojo
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes !=null){
                    Seckill seckill = schema.newMessage(); //创建一个空对象
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);//反序列化
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

    /**
     * 将数据放入redis缓存
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill){
        //set Object(Seckill) ->序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key ="seckill:"+seckill.getSeckillId();
                //序列化，第三个参数是缓冲器
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60*60; //1个小时
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result; //"OK"
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return  null;
    }
}
