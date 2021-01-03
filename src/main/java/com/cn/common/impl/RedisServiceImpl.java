package com.cn.common.impl;

import com.cn.common.IRedisService;
import com.cn.util.GSONFloatAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisServiceImpl implements IRedisService {

    private Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(float.class, new GSONFloatAdapter())
            .registerTypeAdapter(Float.class, new GSONFloatAdapter())
            .create();

    @Resource
    private RedisTemplate<String, ?> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void delete(String... keys) {
        if(keys!=null && keys.length>0){
            redisTemplate.execute((RedisConnection connection) -> {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();

                List<byte[]> lst = new ArrayList<>();
                for (String key : keys) {
                    lst.add(serializer.serialize(key));
                }
                byte[][] bkeys = new byte[lst.size()][];
                lst.toArray(bkeys);

                connection.del(bkeys);
                return null;
            });
        }
    }

    @Override
    public boolean set(String key, String value) {
        boolean result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.set(serializer.serialize(key), serializer.serialize(value));
            return true;
        });
        return result;
    }

    @Override
    public boolean set(String key, String value,long expireTime) {
        boolean result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.set(serializer.serialize(key), serializer.serialize(value));
            connection.expire(serializer.serialize(key),expireTime);
            return true;
        });
        return result;
    }

    @Override
    public boolean set(String key, Object value) {
        if(value!=null){
            String str = gson.toJson(value);
            return this.set(key, str);
        }
        return false;
    }

    @Override
    public boolean set(String key, Object value,long expireTime) {
        if(value!=null){
            String str = gson.toJson(value);
            return this.set(key, str,expireTime);
        }
        return false;
    }


    @Override
    public boolean hSet(String key, String field, Object value) {
        boolean result = redisTemplate.execute((RedisConnection connection) -> {
            if(value==null){
                return false;
            }
            String str;
            if(value.getClass() == String.class){
                str = (String) value;
            } else {
                str = gson.toJson(value);
            }
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.hSet(serializer.serialize(key),serializer.serialize(field), serializer.serialize(str));
            return true;
        });
        return result;
    }

    @Override
    public String get(String key) {
        String result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value =  connection.get(serializer.serialize(key));
            return serializer.deserialize(value);
        });
        return result;
    }

    @Override
    public Set<String> getKeys(String keyLike) {
        return stringRedisTemplate.keys(keyLike);
    }

    @Override
    public <T> T get(String key, Class<T> clz) {
        String json = this.get(key);
        if(StringUtils.isNotBlank(json)){
            return gson.fromJson(json,clz);
        }
        return null;
    }

    @Override
    public <T> T hGet(String key, String field, Class<T> clz) {
        String result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value =  connection.hGet(serializer.serialize(key),serializer.serialize(field));
            return serializer.deserialize(value);
        });
        if(StringUtils.isNotBlank(result)){
            if(clz == String.class ){
                return (T) result;
            } else {
                return gson.fromJson(result,clz);
            }
        }
        return null;
    }

    @Override
    public <T> boolean setList(String key, List<T> list) {
        return this.set(key, list);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clz) {
        String json = this.get(key);
        if(StringUtils.isNotBlank(json)){
            List datalist = gson.fromJson(json,List.class);
            List<T> list = new ArrayList<>();
            for (Object data:datalist){
                String str = gson.toJson(data);
                T t = gson.fromJson(str,clz);
                list.add(t);
            }
            return list;
        }
        return null;

    }

    @Override
    public <T> Map<String, T> getMap(String key, Class<T> clz) {
        Map<String,T> resultMap = this.hGetAll(key,clz);
        return resultMap == null ? null : resultMap;
    }

    @Override
    public Map<String, String> getMap(String key) {
        Map<String,String> resultMap = this.hGetAll(key,null);
        return resultMap == null ? null : resultMap;
    }

    private <T> Map hGetAll(String key , Class<T> clz){
        Map<byte[], byte[]> map = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            Map<byte[], byte[]> mapByte = connection.hGetAll(serializer.serialize(key));
            return mapByte;
        });
        if (null == map){
            return null;
        }
        Map resultMap = new HashMap<>(map.size());
        Iterator<Map.Entry<byte[], byte[]>> iterator = map.entrySet().iterator();
        Map.Entry<byte[], byte[]> entry;
        String mapKey = "";
        String mapValue = "";
        if (null != clz){
            while (iterator.hasNext()){
                entry = iterator.next();
                mapKey = new String(entry.getKey());
                mapValue = new String(entry.getValue());
                resultMap.put(mapKey,gson.fromJson(mapValue,clz));
            }
        }else {
            while (iterator.hasNext()){
                entry = iterator.next();
                mapKey = new String(entry.getKey());
                mapValue = new String(entry.getValue());
                resultMap.put(mapKey,mapValue);
            }
        }

        return resultMap;
    }

    @Override
    public void hDelete(String key, String field) {
        redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.hDel(serializer.serialize(key),serializer.serialize(field));
            return null;
        });
    }

    @Override
    public boolean exist(String key) {
        Assert.notNull(key, "key can not be null!");
        boolean result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.exists(serializer.serialize(key));
        });
        return result;
    }

    @Override
    public long llen(String key) {
        Assert.notNull(key, "key can not be null!");
        long result = redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.lLen(serializer.serialize(key));
        });
        return result;
    }

    @Override
    public long lpush(String key, Object value) {
        Assert.notNull(key, "key can not be null!");
        Assert.notNull(value, "value can not be null!");
        String str = gson.toJson(value);
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.lPush(serializer.serialize(key), serializer.serialize(str));
        });
    }

    @Override
    public long lpush(String key, Object value, long expireTime) {
        Assert.notNull(key, "key can not be null!");
        Assert.notNull(value, "value can not be null!");
        String str = gson.toJson(value);
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            long push = connection.lPush(serializer.serialize(key), serializer.serialize(str));
            connection.expire(serializer.serialize(key),expireTime);
            return push;
        });
    }

    @Override
    public String lpop(String key) {
        Assert.notNull(key, "key can not be null!");
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value =  connection.lPop(serializer.serialize(key));
            return serializer.deserialize(value);
        });
    }

    @Override
    public <T> T lpop(String key, Class<T> clz) {
        String json = this.lpop(key);
        if(StringUtils.isNotBlank(json)){
            return gson.fromJson(json,clz);
        }
        return null;
    }

    @Override
    public long rpush(String key, Object value) {
        Assert.notNull(key, "key can not be null!");
        Assert.notNull(value, "value can not be null!");
        String str = gson.toJson(value);
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.rPush(serializer.serialize(key), serializer.serialize(str));
        });
    }

    @Override
    public long rpush(String key, Object value, long expireTime) {
        Assert.notNull(key, "key can not be null!");
        Assert.notNull(value, "value can not be null!");
        String str = gson.toJson(value);
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            long push = connection.rPush(serializer.serialize(key), serializer.serialize(str));
            connection.expire(serializer.serialize(key),expireTime);
            return push;
        });
    }

    @Override
    public String rpop(String key) {
        Assert.notNull(key, "key can not be null!");
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] value =  connection.rPop(serializer.serialize(key));
            return serializer.deserialize(value);
        });
    }

    @Override
    public <T> T rpop(String key, Class<T> clz) {
        String json = this.rpop(key);
        if(StringUtils.isNotBlank(json)){
            return gson.fromJson(json,clz);
        }
        return null;
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        Assert.notNull(key, "key can not be null!");
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            List<byte[]> bytes = connection.lRange(serializer.serialize(key), start, end);
            List<String> resultList = new ArrayList<>(bytes.size());
            bytes.forEach(b -> resultList.add(new String(b)));
            return resultList;
        });
    }

    @Override
    public <T> List<T> lrange(String key, long start, long end, Class<T> clz) {
        Assert.notNull(key, "key can not be null!");
        Assert.notNull(clz, "clz can not be null!");
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            List<byte[]> bytes = connection.lRange(serializer.serialize(key), start, end);
            List<T> resultList = new ArrayList<>(bytes.size());
            bytes.forEach(b -> resultList.add(gson.fromJson(new String(b), clz)));
            return resultList;
        });
    }

    @Override
    public void ldel(String key, int pace) {
        Assert.notNull(key, "key can not be null!");
        Assert.isTrue(pace>0, "pace must greater than zero!");
        redisTemplate.executePipelined((RedisConnection connection) -> {

            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            long size = llen(key);
            if (size <= 0) {
                return null;
            }
            byte[] deleteFlag = serializer.serialize("delete");
            //从第二个开始删除,保留列表里的第一个值
            for (int i = 1; i < size; i = i+(pace+1)) {
                connection.lSet(serializer.serialize(key), i, deleteFlag);
            }
            //执行删除
            connection.lRem(serializer.serialize(key), size, deleteFlag);
            return null;
        });
    }

    @Override
    public long getExpireTime(String key) {
        return redisTemplate.execute((RedisConnection connection) -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.pTtl(serializer.serialize(key), TimeUnit.SECONDS);
        });
    }
}
