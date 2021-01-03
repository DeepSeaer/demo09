package com.cn.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class JapConfigBean {

    private static Logger logger = LoggerFactory.getLogger(JapConfigBean.class);

    @Resource
    private Environment env;

    @Resource
    private DataSource dataSource;

    @Bean
    public EntityManagerFactory entityManagerFactory() throws PropertyVetoException {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        //设置配置适配器
        factory.setJpaVendorAdapter(vendorAdapter);
        //配置扫描的实体所在的包
        factory.setPackagesToScan(env.getProperty("hibernate.entitymanager.packagesToScan"));
        //dataSource配置
        factory.setDataSource(dataSource);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.jdbc.batch_size", 50);
        jpaProperties.put("hibernate.naming-strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        jpaProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        factory.setJpaPropertyMap(jpaProperties);

        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws PropertyVetoException {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

}
