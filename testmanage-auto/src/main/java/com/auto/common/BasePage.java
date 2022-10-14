package com.auto.common;

import com.auto.common.PageSelector;
import com.auto.bean.CaseDataBean;
import com.auto.bean.LoginBean;
import com.auto.bean.LoginPage;

public class BasePage extends Function implements PageElement {
	
	private static String fdev_url = ReadConfig.fdev_url;
	private static String browser = ReadConfig.browser;
	
	
    /**
     * 登陆
     * 
     * @author T-fud
     * @param login
     */
    public static void loginSpdb(LoginBean login) {
    	
    }

    /**
     * 进入首页
     * 
     * @author T-fud
     */
    public static void __init__(CaseDataBean data) {
        LoginPage.fdevLogin(fdev_url, browser, data.getUsername(), data.getQueryPwd(), "1");
        //若登录后有公共弹出，点击我知道了
        if(Function.isElementExist(PageSelector.NOTIFY_KNOWS)){
        	Function.waitUntilElementVisible(PageSelector.NOTIFY_KNOWS);
        	Function.clickElement(PageSelector.NOTIFY_KNOWS);
        };
    }

    /**
     * 安全键盘输入密码
     * 
     * @param str 六位密码
     * @author T-fud
     */
    public static void safeInput(String str) {
    	
    }
    
    public static void init_mpaas(){
    	
    }
}
