package com.auto.service;

import com.auto.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService {

    void addUser(Map<String, String> map) throws Exception;

    List<User> queryUser(Map<String, String> map) throws Exception;

    void deleteUser(Map map) throws Exception;

    void updateUser(Map map) throws Exception;
}
