package com.mantis.service;

public interface IMantisUserService {
    /**
     * 注册mantis用户
     * @param userName
     * @param password
     * @throws Exception
     */
    void regeisterMantisUser(String userName, String password, String userNameCn, String email) throws  Exception;

    /**
     * 修改mantis单个项目权限
     * @param userName
     * @param projectId
     * @param level
     * @throws Exception
     */
    void cancelProjectAccessLevel(String userName, String projectId, String level) throws  Exception;

    void cancelAllUserProjectLevel() throws Exception;

    void queryAllMantisUser() throws Exception;

    String queryProjectUserLevel(String projectId, String userName) throws  Exception;
}
