package com.spdb.fdev.fuser.spdb.dao.Impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import com.spdb.fdev.fuser.spdb.entity.user.LdapUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.LdapUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.LdapUser;

@Repository
@RefreshScope
public class LdapUserDaoImpl implements LdapUserDao {

	@Value("${ldap.ou}")
    private String ou;
	
	@Resource(name = "fdevldap")
	private LdapTemplate ldapTemplate;

	@Override
	public void create(LdapUser user) {
		ldapTemplate.bind(buildDn(user), null, buildAttributes(user));
	}

	@Resource(name = "uiasldap")
	private LdapTemplate uiasLdapTemplate;

	@Resource(name = "manufactureldap")
	private LdapTemplate manufactureLdapTemplate;

	@Override
	public List<LdapUser> findAll() {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("ou", ou));
		filter.and(new EqualsFilter("objectClass", "person"));
		return ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), new LdapUserContextMapper());
	}

	@Override
	public LdapUser findOne(String uid) {
		Name dn = LdapNameBuilder.newInstance().add("ou", ou).add("uid", uid).build();
		LdapUser user = null;
		try {
			user = ldapTemplate.lookup(dn, new LdapUserContextMapper());
		} catch (NameNotFoundException e) {
			user = null;
		}
		return user;
	}

	@Override
	public List<LdapUser> findByName(String name) {
		LdapQuery q = query().where("objectClass").is("person").and("cn").whitespaceWildcardsLike(name);
		return ldapTemplate.search(q, new LdapUserContextMapper());
	}

	@Override
	public void update(LdapUser user) {
//		ldapTemplate.rebind(buildDn(user), null, buildAttributes(user)); // 此方式会替换所有属性
		ldapTemplate.modifyAttributes(buildDn(user), buildMods(user)); // 替换指定的属性
	}

	@Override
	public void delete(LdapUser user) {
		ldapTemplate.unbind(buildDn(user));
	}

	@Override
	public boolean authenticate(LdapUser user) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "person"));
		filter.and(new EqualsFilter("uid", user.getUid()));
		return ldapTemplate.authenticate("", filter.encode(), user.getPassword());
	}

	@Override
	public boolean authenticate(String username, String password) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass","person"));
		filter.and(new EqualsFilter("mailNickname",username));
		return uiasLdapTemplate.authenticate("", filter.encode(), password);
	}


	@Override
	public boolean authenticateManu(String username, String password) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass","person"));
		filter.and(new EqualsFilter("mailNickname",username));
		return manufactureLdapTemplate.authenticate("", filter.encode(), password);
	}

	private Name buildDn(LdapUser user) {
		return LdapNameBuilder.newInstance().add("ou", ou).add("uid", user.getUid()).build();
	}

	private Attributes buildAttributes(LdapUser user) {
		Attributes attrs = new BasicAttributes();
		BasicAttribute ocattr = new BasicAttribute("objectClass");
		ocattr.add("top");
		ocattr.add("inetOrgPerson");
		ocattr.add("person");
		attrs.put(ocattr);
		attrs.put("ou", ou);// ou
		if (!CommonUtils.isNullOrEmpty(user.getUserNameCn())) {
			attrs.put("cn", user.getUserNameCn());// realname
		}
		if (!CommonUtils.isNullOrEmpty(user.getUserNameEn())) {
			attrs.put("sn", user.getUserNameEn());// username
		}
		if (!CommonUtils.isNullOrEmpty(user.getMail())) {
			attrs.put("mail", user.getMail());// mail
		}
		if (!CommonUtils.isNullOrEmpty(user.getPassword())) {
			attrs.put("userPassword", user.getPassword());// password
		}
		if (!CommonUtils.isNullOrEmpty(user.getMobile())) {
			attrs.put("mobile", user.getMobile());// password
		}
		return attrs;
	}
	
	private ModificationItem[] buildMods(LdapUser user) {
		List<ModificationItem> mods = new ArrayList<>();
		// cn, sn, userPassword, mail, mobile
		if (!CommonUtils.isNullOrEmpty(user.getUserNameCn())) {
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("cn", user.getUserNameCn()));
			mods.add(mod);
		}
		if (!CommonUtils.isNullOrEmpty(user.getUserNameEn())) {
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("sn", user.getUserNameEn()));
			mods.add(mod);
		}
		if (!CommonUtils.isNullOrEmpty(user.getPassword())) {
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("userPassword", user.getPassword()));
			mods.add(mod);
		}
		if (!CommonUtils.isNullOrEmpty(user.getMail())) {
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("mail", user.getMail()));
			mods.add(mod);
		}
		if (!CommonUtils.isNullOrEmpty(user.getMobile())) {
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("mobile", user.getMobile()));
			mods.add(mod);
		}
		return mods.toArray(new ModificationItem[] {});
	}

	private static class LdapUserContextMapper extends AbstractContextMapper<LdapUser> {

		@Override
		protected LdapUser doMapFromContext(DirContextOperations context) {
			LdapUser ldapUser = new LdapUser();
			ldapUser.setUid(context.getStringAttribute("uid"));
			if (!CommonUtils.isNullOrEmpty(context.getObjectAttribute("userPassword"))) {
				ldapUser.setPassword(new String((byte[]) context.getObjectAttribute("userPassword")));
			}
			ldapUser.setUserNameCn(context.getStringAttribute("cn"));
			ldapUser.setUserNameEn(context.getStringAttribute("sn"));
			ldapUser.setMail(context.getStringAttribute("mail"));
			ldapUser.setMobile(context.getStringAttribute("mobile"));
			return ldapUser;
		}

	}

	@Override
	public boolean checkIsLeaveOrNot(String email,boolean isSpdb) {
		LdapQuery q = query().where("objectClass").is("top").and("objectClass").is("person")
				.and("objectClass").is("organizationalPerson")
				.and("objectClass").is("user")
				.and("mail").is(email);
		List<LdapUserInfo> ldapUserInfo;
		if(isSpdb){
			//查询行内人员信息
			ldapUserInfo = uiasLdapTemplate.find(q, LdapUserInfo.class);
		}else {
			//查询厂商人员信息
			ldapUserInfo = manufactureLdapTemplate.find(q, LdapUserInfo.class);
		}
		if(CommonUtils.isNullOrEmpty(ldapUserInfo)){
			//通过ldap查不到用户信息，说明已离职
			return true;
		}
		return false;
	}

}
