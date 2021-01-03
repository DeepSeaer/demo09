package com.cn.service;

import com.cn.entity.User;

import java.util.List;

public interface UserService {

    public void saveUser(String name, String pass);

    public void delete(String name);

    public void update(String name, String pass);

    public User findByName(String name);

    public List<User> queryUsers(String name, String pass, Integer currentPage, Integer pageSize);

}
