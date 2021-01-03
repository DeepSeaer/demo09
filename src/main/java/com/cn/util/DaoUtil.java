package com.cn.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class DaoUtil {

    private static Logger logger = LoggerFactory.getLogger(DaoUtil.class);

    public static <T> String buildSelectColumn(Class cls, String alias){
        StringBuilder sb = new StringBuilder();
        for(Class<?> clazz = cls; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            Field[] fileds = clazz.getDeclaredFields();
            if(fileds!=null && fileds.length != 0){
                for (int i=0;i<fileds.length;i++){
                    String columnName = getColumnName(fileds[i],true);
                    if(!StringUtils.isEmpty(columnName) && !"serialVersionUID".equalsIgnoreCase(columnName)){
                        sb.append(alias+ "."+columnName+",");
                    }
                }
            }
        }
        String str = sb.toString().trim();
        if(str.endsWith(",")){
            str = str.substring(0,str.length()-1);
        }

        return str;
    }

    private static String getColumnName(Field field,boolean showColumn){
        String columnName = null;
        Column columnAnno = field.getAnnotation(Column.class);
        if(columnAnno!=null){
            if(showColumn){
                columnName = columnAnno.name();
                if(StringUtils.isEmpty(columnName)){
                    columnName = field.getName();
                }
            } else {
                columnName = field.getName();
            }
            return columnName;

        }

        JoinColumn joinColumnAnno = field.getAnnotation(JoinColumn.class);
        if(joinColumnAnno!=null){
            if(showColumn){
                columnName = joinColumnAnno.name();
                if(StringUtils.isEmpty(columnName)){
                    columnName = field.getName();
                }
            } else {
                columnName = field.getName();
            }
            return columnName;
        }

        Annotation[] annos = field.getAnnotations();
        if(annos==null || annos.length==0){
            columnName = field.getName();
        } else {
            boolean flag = true;
            for (Annotation annotation:annos){
                if(annotation.annotationType()== ManyToMany.class
                        ||annotation.annotationType()== Embedded.class
                        ||annotation.annotationType()== OneToMany.class
                        ||annotation.annotationType()==OneToOne.class){
                    flag = false;
                    break;
                }
            }
            if(flag){
                columnName = field.getName();
            }
        }

        return columnName;
    }
}
