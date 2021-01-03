package com.cn.dao;

import com.cn.entity.bean.AbstractEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JpaBaseDao {

    /**
     * 保存或者更新
     * @param t
     * @param <T>
     * @return
     */
    <T extends AbstractEntity> T save(T t);

    /**
     * 保存或者更新
     * @param t
     * @param <T>
     */
    <T extends AbstractEntity> void saveSimple(T t);

    /**
     * 批量保存或者更新
     * @param lst
     * @param <T>
     */
    <T extends AbstractEntity> void save(Collection<T> lst);

    <T extends AbstractEntity> void saveNew(T t);

    <T> List<T> getResultsBySQL(Class<T> resultBean, String sql, Map<String, Object> parameter, Integer currentPage, Integer pageSize);

    <T> List<T> getResultsBySQL(Class<T> resultBean,String sql,Map<String, Object> parameter, Integer currentPage, Integer pageSize, String orderField, Boolean isDesc);

    List getResultsByJPQL(String jpql, Map<String, Object> parameter);

    <T> List<T> getResultsBySQL(Class<T> resultBean, String sql,Map<String, Object> parameter);

}
