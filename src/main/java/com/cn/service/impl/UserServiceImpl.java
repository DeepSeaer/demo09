package com.cn.service.impl;

import com.cn.dao.UserDao;
import com.cn.entity.User;
import com.cn.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserServiceImpl extends JpaBaseDaoImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void delete(String name) {
        User user = userDao.findByName(name);
        userDao.delete(user);
    }

    @Override
    public void update(String name, String pass) {
        User user = userDao.findByName(name);
        user.setPass(pass);
        userDao.save(user);
    }

    @Override
    public User findByName(String name) {
        return userDao.findByName(name);
    }

    @Override
    public void saveUser(String name, String pass) {
        User user = new User();
        user.setName(name);
        user.setPass(pass);
        userDao.save(user);
    }

    @Override
    public List<User> queryUsers(String name, String pass, Integer currentPage, Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select u.id, u.name, u.pass");
        buildQueryUsers(sql, name, pass, params);
        return getResultsBySQL(User.class, sql.toString(), params, currentPage, pageSize);
    }

    public void buildQueryUsers(StringBuilder sql, String name, String pass, Map<String, Object> params) {
        sql.append(" from t_user u ");
        StringBuilder condition = new StringBuilder();
        condition.append(" where 1=1 ");
        if (StringUtils.isNotBlank(name)) {
            condition.append("and u.name=:name ");
            params.put("name", name);
        }
        if (StringUtils.isNotBlank(pass)) {
            condition.append("and u.pass=:pass ");
            params.put("pass", pass);
        }
        sql.append(condition);
    }
}
