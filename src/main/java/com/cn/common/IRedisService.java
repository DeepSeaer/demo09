package com.cn.common;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRedisService {

    void delete(String... key);

    boolean set(String key, String value);
    boolean set(String key, String value,long expireTime);

    /**
     *
     * @param key
     * @param value any object ,not need Serializable
     * @return
     */
    boolean set(String key, Object value);
    boolean set(String key, Object value,long expireTime);

    boolean hSet(String key, String field, Object value);

    String get(String key);

    /**
     * 模糊查询KEY,支持全模糊(*),左模糊(*str),右模糊(str*),中间模糊(str*str)
     * @param keyLike
     * @return
     */
    Set<String> getKeys(String keyLike);

    <T> T get(String key,Class<T> clz);

    <T> T hGet(String key,String field,Class<T> clz);

    <T> boolean setList(String key ,List<T> list);

    <T> List<T> getList(String key,Class<T> clz);

    /**
     * 确保map的value是一个对象
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    <T> Map<String,T> getMap(String key, Class<T> clz);

    Map<String,String> getMap(String key);

    void hDelete(String key,String field);

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    boolean exist(String key);

    /**
     * 返回列表的长度
     * @param key
     * @return
     */
    long llen(String key);

    /**
     * 从头部往列表里添加值
     * @param key
     * @param value
     * @return
     */
    long lpush(String key, Object value);

    /**
     * 从头部往列表里添加值
     * @param key
     * @param value
     * @param expireTime 过期时间,单位秒
     * @return
     */
    long lpush(String key, Object value, long expireTime);

    /**
     * 从列表头部拿值
     * @param key
     * @return
     */
    String lpop(String key);

    /**
     * 从列表头部拿值
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    <T> T lpop(String key, Class<T> clz);

    /**
     * 从尾部往列表里添加值
     * @param key
     * @param value
     * @return
     */
    long rpush(String key, Object value);

    /**
     * 从尾部往列表里添加值
     * @param key
     * @param value
     * @param expireTime 过期时间,单位秒
     * @return
     */
    long rpush(String key, Object value, long expireTime);

    /**
     * 从列表尾部拿值
     * @param key
     * @return
     */
    String rpop(String key);

    /**
     * 从列表尾部拿值
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    <T> T rpop(String key, Class<T> clz);

    /**
     * 返回列表
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<String> lrange(String key, long start, long end);

    /**
     * 返回列表
     * @param key
     * @param start
     * @param end
     * @param clz
     * @param <T>
     * @return
     */
    <T> List<T> lrange(String key, long start, long end, Class<T> clz);

    /**
     * 按步长删除列表的元素
     * @param key
     * @param pace 步长,必须大于0
     * @return
     */
    void ldel(String key, int pace);

    /**
     * 获取key的过期时间
     * @param key
     * @return
     */
    long getExpireTime(String key);

}
