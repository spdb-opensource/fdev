package com.spdb.fdev.fuser.spdb.service;

public interface FtmsUserService {
	/**
	 * 同步用户离职信息到ftms平台
	 * @param user_name_en
	 * @throws Exception
	 */
    public void isLeave(String user_name_en,String status) throws Exception;
}
