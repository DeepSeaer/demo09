package com.cn.dao;

import com.cn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    User findByName(String name);

    User findById(String id);

    User findByNameAndPass(String name, String pass);

    @Query("from User u where u.name = :name")
    User findUser(@Param("name") String name);

}
