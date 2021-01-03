package com.cn.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 *  对象辅助类
 */
public class BeanUtil {

    private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

    private static Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(float.class, new GSONFloatAdapter())
            .registerTypeAdapter(Float.class, new GSONFloatAdapter())
            .create();

    public static <T, V> T toObject(Map<String, V> map, Class<T> destCls) {
        String jsonStr = gson.toJson(map);
        return gson.fromJson(jsonStr,destCls);
    }

    /**
     * 将Object对象里面的属性和值转化成Map对象
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, String> toMap(Object obj) {
        try {
            Map<String, String> map = new HashMap<>();
            Class<?> clazz = obj.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(obj);
                if(null != value){
                    map.put(fieldName, convertObjValue(value));
                }
            }
            return map;
        } catch (IllegalAccessException e) {
            logger.warn(e.getMessage());
        }
        return null;
    }

    /**
     * 根据属性名,获取对象的属性值
     *
     * @param bean
     * @param propertyName
     * @return
     */
    public static String getPropertyValue(Object bean, String propertyName) {
        return getPropertyValue(bean, propertyName, false);
    }

    public static String getPropertyValue(Object bean, String propertyName, boolean useDateTimeFormat) {
        String returnValue = "";
        try {
            PropertyDescriptor prop = new PropertyDescriptor(propertyName, bean.getClass());
            Method method = prop.getReadMethod();
            Object objValue = method.invoke(bean);
            returnValue = convertObjValue(objValue, useDateTimeFormat);
        } catch (IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            logger.warn(e.getMessage());
        }
        return returnValue;
    }

    /**
     * 根据属性名,获取对象的属性值(不转换返回值)
     *
     * @param bean
     * @param propertyName
     * @return
     */
    public static Object getPropertyObjectValue(Object bean, String propertyName) {
        Object objValue = null;
        try {
            PropertyDescriptor prop = new PropertyDescriptor(propertyName, bean.getClass());
            Method method = prop.getReadMethod();
            objValue = method.invoke(bean);
        } catch (IntrospectionException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            logger.warn(e.getMessage());
        }
        return objValue;
    }

    /**
     * 获取对象的属性类型
     *
     * @param beanClass
     * @param propertyName
     * @return
     */
    public static Class getPropertyType(Class beanClass, String propertyName) {
        if(StringUtils.isNotBlank(propertyName)) {
            List<Field> fields = getFields(beanClass);
            for (Field field : fields) {
                if (field.getName().equals(propertyName)) {
                    return field.getType();
                }
            }
        }
        return null;
    }

    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue) {
        try {
            if (propertyValue != null && StringUtils.isNotBlank(propertyName)) {
                List<Field> fields = getFields(bean.getClass());
                for (Field field:fields){
                    if(field.getName().equals(propertyName)){
                        field.setAccessible(true);
                        field.set(bean,propertyValue);
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException  e) {
            logger.warn(propertyName+" "+e.getMessage());
        }
    }

    /**
     * 根据传入对象获取所有属性名
     *
     * @param cls
     * @return
     */
    public static List<String> getProperties(Class cls,String...excludeField) {
        List<String> list = new ArrayList<>();
        try {
            if (cls != null) {
                BeanInfo info = Introspector.getBeanInfo(cls, Object.class);
                PropertyDescriptor[] props = info.getPropertyDescriptors();
                for (int i = 0; i < props.length; i++) {
                    String fieldName = props[i].getName();
                    if(!ArrayUtils.contains(excludeField,fieldName)){
                        list.add(fieldName);
                    }
                }
            }
        } catch (IntrospectionException e) {
            logger.warn(e.getMessage());
        }
        return list;
    }

    /**
     * 获取对象的字段列表
     * @param cls
     * @param excludeField
     * @return
     */
    public static List<Field> getFields(Class cls, String...excludeField) {
        List<Field> list = new ArrayList<>();
        if (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field:fields) {
                String fieldName = field.getName();
                if(!ArrayUtils.contains(excludeField,fieldName)){
                    list.add(field);
                }
            }
        }
        return list;
    }

    /**
     * 将值类型统一处理成String
     * @param objValue
     * @param useDateTimeFormat
     * @return
     */
    public static String convertObjValue(Object objValue, boolean useDateTimeFormat) {
        if(null == objValue){
            return null;
        }
        String value = "";
        if (objValue instanceof String) {
            value = objValue.toString();
        }else if (objValue instanceof Long) {
            value = objValue.toString();
        }else if (objValue instanceof Integer) {
            value =  objValue.toString();
        }else if (objValue instanceof Double) {
            value = objValue.toString();
        }else if (objValue instanceof Float) {
            value = objValue.toString();
        }else if (objValue instanceof Boolean) {
            value = objValue.toString();
        }else if(objValue instanceof Enum){
            value = ((Enum) objValue).name();
        }else if (objValue instanceof Date) {
            if (useDateTimeFormat) {
                value = DateUtil.dateTime2String((Date) objValue);
            } else {
                value = DateUtil.date2String((Date) objValue);
            }
        }
        return value;
    }

    public static String convertObjValue(Object objValue) {
        String value = convertObjValue(objValue, false);
        return value;
    }

    /**
     * 将当前值按照目标类型进行映射转换，无法转换则返回null
     * @param sourceValue
     * @param destType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <R> R convertType(Object sourceValue,Class<R> destType){
        Object result = null;
        if(sourceValue==null){ return null; }

        Class srcType = sourceValue.getClass();
        if( destType.isAssignableFrom(srcType)
                || ( destType.isPrimitive() && ClassUtil.transferToPrime(srcType) == destType)
                || ( srcType.isPrimitive() && ClassUtil.transferToPrime(destType) == srcType)){
            result = sourceValue;

        } else if( (destType==long.class || destType==Long.class) && srcType == BigInteger.class ){
            result = ((BigInteger)sourceValue).longValue();

            //support nanos
        } else if( (destType==long.class || destType==Long.class) && srcType == Instant.class ){
            result = (long)(((Instant) sourceValue).getEpochSecond() * Math.pow(10, 9)) + ((Instant) sourceValue).getNano();

        } else if( (destType==long.class || destType==Long.class) && (srcType == Date.class || srcType == Timestamp.class) ){
            result = ((Date)sourceValue).getTime();

        } else if( (destType==long.class || destType==Long.class) && srcType == Double.class ){
            result = ((Double)sourceValue).longValue();

        } else if( (destType == int.class || destType == Integer.class) && srcType == Double.class){
            result = ((Double)sourceValue).intValue();

        } else if( (destType==float.class || destType==Float.class) && srcType == BigDecimal.class){
            result = ((BigDecimal)sourceValue).floatValue();

        } else if( (destType==double.class || destType==Double.class) && srcType == BigDecimal.class){
            result = ((BigDecimal)sourceValue).doubleValue();

        } else if( destType.isEnum() && srcType == String.class){
            result = Enum.valueOf((Class) destType, (String)sourceValue);

        } else if(destType == Date.class && srcType == Long.class){
            result = new Date((Long) sourceValue);

        } else if(destType==Date.class && srcType == Double.class){
            result = new Date(((Double)sourceValue).longValue());

            //support nanos
        } else if(destType == Instant.class && srcType == Long.class){
            long epochSecond = (long) ((Long)sourceValue / Math.pow(10, 9));
            long nanoAdjustment = (Long)sourceValue - epochSecond;
            result = Instant.ofEpochSecond(epochSecond, nanoAdjustment);

        } else if( destType==Instant.class && srcType == String.class ){
            result = Instant.parse((CharSequence) sourceValue);

        }

        if(null == result){
//            throw new ApplicationRuntimeException("can not CAST from "+srcType+" to "+destType);
            throw new RuntimeException("can not CAST from "+srcType+" to "+destType);
        }
        return (R) result;
    }
}
