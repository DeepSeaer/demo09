package com.cn.controller;

import com.cn.entity.User;
import com.cn.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "用户类服务")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiModelProperty(value = "保存用户")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void save(@RequestParam @ApiParam("姓名")String name, @RequestParam @ApiParam("通行证")String pass){
        userService.saveUser(name, pass);
    }

    @ApiModelProperty(value = "删除用户")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public void delete(@RequestParam @ApiParam("姓名")String name){
        userService.delete(name);
    }

    @ApiModelProperty(value = "更新用户")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public void update(@RequestParam @ApiParam("姓名")String name, @RequestParam @ApiParam("通行证")String pass){
        userService.update(name, pass);
    }

    @ApiModelProperty(value = "查找用户")
    @RequestMapping(value = "/findByName", method = RequestMethod.GET)
    public User findByName(@RequestParam @ApiParam("姓名")String name){
        return userService.findByName(name);
    }

    @ApiModelProperty(value = "查询用户列表")
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public List<User> queryUserList(@RequestParam @ApiParam("姓名") String name,
                                    @RequestParam @ApiParam("通行证") String pass,
                                    @RequestParam(defaultValue = "1") Integer currentPage,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return userService.queryUsers(name, pass, currentPage, pageSize);
    }

}
