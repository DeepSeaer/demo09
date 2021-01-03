package com.cn.service.impl;

import com.cn.entity.bean.AbstractEntity;
import com.cn.dao.JpaBaseDao;
import com.cn.util.BeanUtil;
import com.cn.util.DaoUtil;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class JpaBaseDaoImpl implements JpaBaseDao {

    private static Logger logger = LoggerFactory.getLogger(JpaBaseDaoImpl.class);

    @PersistenceContext
    protected EntityManager manager;

    @Override
    public <T extends AbstractEntity> T save(T t) {
        t.setUpdateTime(new Date());
        t = manager.merge(t);
        return t;
    }

    @Override
    public <T extends AbstractEntity> void saveSimple(T t) {
        t.setUpdateTime(new Date());
        manager.merge(t);
    }

    @Override
    public <T extends AbstractEntity> void save(Collection<T> lst) {
        for (T t : lst) {
            t.setUpdateTime(new Date());
            manager.merge(t);
        }
    }

    @Override
    public <T extends AbstractEntity> void saveNew(T t) {
        manager.persist(t);
    }

    @Override
    public <T> List<T> getResultsBySQL(Class<T> resultBean, String sql, Map<String, Object> parameter, Integer currentPage, Integer pageSize) {
        return this.getResultsBySQL(resultBean,sql,parameter, currentPage, pageSize,null,null);
    }

    @Override
    public <T> List<T> getResultsBySQL(Class<T> resultBean, String sql, Map<String, Object> parameter, Integer currentPage, Integer pageSize, String orderField, Boolean isDesc) {
        if(orderField!=null && isDesc!=null){
            sql +=" order by "+ orderField + (isDesc?" desc":"");
        }

        Query query = manager.createNativeQuery(sql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        if(parameter!=null){
            parameter.keySet().forEach(param -> query.setParameter(param,parameter.get(param)));
        }

        if (currentPage != null && currentPage > 0 && pageSize!=null && pageSize > 0) {
            query.setFirstResult((currentPage - 1) * pageSize);
            query.setMaxResults(pageSize);
        }

        List<HashMap<String, Object>> lst = query.getResultList();

        return this.buildResultList(resultBean,lst);
    }

    private <T> List<T> buildResultList(Class<T> resultClazz,List<HashMap<String, Object>> queryResults){
        List<T> resultList = new ArrayList<T>();
        try {
            for (HashMap<String, Object> map : queryResults) {
                T resultObj = resultClazz.newInstance();
                if (resultObj instanceof AbstractEntity) {
                    ((AbstractEntity) resultObj).setCreateTime(null);
                    ((AbstractEntity) resultObj).setUpdateTime(null);//初始化该值，避免出现错误数据
                }
                this.transMap2Bean(map,resultObj);
                resultList.add(resultObj);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(),e);
        }
        return resultList;
    }

    @Override
    public List getResultsByJPQL(String jpql, Map<String, Object> parameter) {
        Query query = manager.createQuery(jpql);
        if(parameter!=null){
            for (Iterator<String> iter = parameter.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                Object value = parameter.get(key);

                query.setParameter(key,value);
            }
        }

        List lst = query.getResultList();
        return lst;
    }

    @Override
    public <T> List<T> getResultsBySQL(Class<T> resultBean, String sql,Map<String, Object> parameter) {
        return this.getResultsBySQL(resultBean,sql,parameter, null,null,null,null);
    }

    private void transMap2Bean(Map<String, Object> map, Object obj) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            logger.error("transMap2Bean Error ", e);
            return;
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (map.containsKey(key)) {
                try {
                    Object value = map.get(key);
                    // 得到property对应的setter方法
                    Method setter = property.getWriteMethod();
                    Field field = this.getDeclaredField(obj,property.getName());
                    if(field != null){
                        value = BeanUtil.convertType(value, field.getType());
                        setter.invoke(obj, value);
                    }
                }catch (Exception e) {
                    logger.error("transMap2Bean Error "+e.getMessage(), e);
                }
            } else {
                try {
                    Field field = this.getDeclaredField(obj,property.getName());
                    // 得到property对应的setter方法
                    Method setter = property.getWriteMethod();
                    Object resultVV = null;
                    if(field != null){
                        Class destType = field.getType();
                        if (AbstractEntity.class.isAssignableFrom(destType)) {
                            JoinColumn anno = field.getAnnotation(JoinColumn.class);
                            if (anno != null) {
                                String sql = "select " + DaoUtil.buildSelectColumn(destType, "a") + " from " + ((Table) destType.getAnnotation(Table.class)).name() + " a where a.id=" + map.get("id");
                                List lst = this.getResultsBySQL(destType, sql, null);
                                if (CollectionUtils.isNotEmpty(lst)) {
                                    resultVV = lst.get(0);
                                    setter.invoke(obj, resultVV);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("transMap2Bean Error "+e.getMessage(), e);
                }
            }
        }
    }

    private Field getDeclaredField(Object object, String fieldName){
        Field field = null ;
        for(Class<?> clazz = object.getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            List<Field> fields = BeanUtil.getFields(clazz);
            for (Field f : fields) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
            if(null != field){
                break;
            }
        }
        return field;
    }
}
