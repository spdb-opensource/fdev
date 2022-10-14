package com.auto.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.auto.entity.User;

@Repository
public interface UserMapper {

    void addUser(User user) throws Exception;

    List<User> queryUser(@Param("userName") String search, @Param("valid") String valid) throws Exception;
    
    List<User> queryUserByName(@Param("userName") String search, @Param("userId") String userId) throws Exception;

    void deleteUser(@Param("userId") String userId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateUser(@Param("userId") String userId, @Param("userName") String userName,
                    @Param("queryPwd") String queryPwd, @Param("userNameEn") String userNameEn,
                    @Param("time") String time) throws Exception;
}