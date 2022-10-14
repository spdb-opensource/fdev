package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.LdapUser;
import com.spdb.fdev.fuser.spdb.entity.user.LdapUserInfo;

public interface LdapUserDao {
	void create(LdapUser user);

	List<LdapUser> findAll();

	LdapUser findOne(String uid);

	List<LdapUser> findByName(String name);

	void update(LdapUser user);

	void delete(LdapUser user);

	boolean authenticate(LdapUser user);

    boolean authenticate(String username, String password);

	boolean authenticateManu(String username, String password);

	boolean checkIsLeaveOrNot(String username, boolean isSpdb);
}
