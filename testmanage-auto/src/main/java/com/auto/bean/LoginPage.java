package com.auto.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;

import com.auto.bean.LoginBean;
import com.auto.common.Function;
import com.auto.common.PageSelector;

public class LoginPage{


//---------定位页面元素---------------------	

	
    /**
     * 密码小键盘，数字键
     */
	public static String dizXpath(int x){
		String dizX="//div[@id='keybordskopener']/div/div[2]/ul[1]/li["+x+"]";
		return dizX;
	}
    /**
     * 图片验证码，返回位置属性
     */
	public By getTokenImg_xpath(){
		return PageSelector.tokenImg;
	}

//------------Action--------------

	
	public static void fdevLogin(String url,String browser,String userName,String passWord,String name) {
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.wait(5);		
		Function.inputText(PageSelector.USERNAME, userName);
		Function.inputText(PageSelector.PASSWORD, passWord);
		Function.clickElement(PageSelector.LOGIN_BUTTON);
		Function.wait(3);
		Function.getScreen(name);
	}
			
    /**
     * 小键盘输入密码
     */
	public  static void userPw(String str){

		String passW[]=str.split("");

		Function.clickElement(PageSelector.pwdImg);
		String dizRe[]=new String[10];
	
		for(int i=1;i<11;i++){
			String dizTe=Function.getElementText(By.xpath(dizXpath(i)));		
			dizRe[i-1]=dizTe;
			System.out.print(dizRe[i-1]);

		}
		
		for(int i=0;i<passW.length;i++){
			for(int j=0;j<dizRe.length;j++){
				if (passW[i].equals(dizRe[j])){
					int x=j+1;
					Function.clickElement(By.xpath(dizXpath(x)));
				}
			}
        }
    }



	


//	----------登录入口封装------------
    /**
     * 使用密码小键盘登录网银
     */
	public void loginKeyborad(String url,String browser,String userName,String passWord,String testName){
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.wait(5);		
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
		userPw(passWord);
		Function.wait(3);
		Function.clickElement(PageSelector.conX);
		Function.getScreen(testName);
		Function.clickElement(PageSelector.loginButton);
		Function.wait(3);
		Function.getScreen(testName);
	}
	/**
     * 使用密码小键盘登录网银
     */
	public void loginKeyborad1(String url,String browser,String userName,String passWord,String testName,String flag){
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.switchToFrame(PageSelector.iframeC);
		if("E".equals(flag)){
			Function.clickElement(PageSelector.choooseEleE);
		}
		else if("V".equals(flag)){
			Function.clickElement(PageSelector.choooseEleV);
		}
		Function.wait(5);
		Function.switchToParentFrame();
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
		Function.inputText(PageSelector.pwd, passWord);
		Function.wait(3);
		Function.getScreen(testName);
	}
	
	/**
	 * 使用密码输入框登录
	 */
	public void loginEnterPassword(String url,String browser,String userName,String passWord,String testNo){
		Function.closeBrowser();
		Function.openUrl(url, browser);
	//	Function.clickElement(last);
		Function.wait(5);		
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
		Function.inputText(PageSelector.pwd, passWord);
		Function.wait(3);
		Function.getScreen(testNo);

		
//		maskClose();
	}
	/**
	 * 未登录进入欢迎页
	 * @param url
	 * @param browser
	 * @param userName
	 * @param passWord
	 * @param testNo
	 */
	public void nologinEnterPassword(String url,String browser,String testNo){
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.getScreen(testNo);
	}
	/**
	 * 四方钱登录-使用密码输入框登录
	 */
	public static void siFangQLoginKeyboard(String url,String browser,String userName,String passWord,String testName){
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.clickElement(PageSelector.last);//点击四方钱按钮，进入登录页面
		Function.wait(5);		
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
		userPw(passWord);
		Function.wait(3);
		Function.getScreen(testName);
		Function.clickElement(PageSelector.loginButton);
//		maskClose();
	}
	/**
	 * 四方钱-密码小键盘登录
	 */
	public static void siFangQLoginEnterPassword(String url,String browser,String userName,String passWord,String testName){
		Function.closeBrowser();
		Function.openUrl(url, browser);
		Function.clickElement(PageSelector.last);//点击四方钱按钮，进入登录页面
		Function.wait(5);		
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
		Function.inputText(PageSelector.pwd, passWord);
		Function.wait(3);
		Function.getScreen(testName);
//		Function.clickElement(PageSelector.loginButton);
//		maskClose();
	}
	
	/**
	 * 输入动态密码
	 * @param testNo
	 */
	public void loginSendMsg(String testNo){
		
		Function.clickElement(PageSelector.loginButton);
		if(Function.isElementExist(PageSelector.loginPass)){
			if(Function.isElementExist(PageSelector.textCenter)){
				Function.clickElement(PageSelector.textCenter);
				Function.clickElement(PageSelector.conforimButn);	
				Function.inputText(PageSelector.mobilePasswd, "123456");
				Function.clickElement(PageSelector.conforimButn);
			}else{
				Function.clickElement(PageSelector.sendPasswd);
				Function.inputText(PageSelector.mobilePasswd, "123456");
				Function.clickElement(PageSelector.sendMsg);
				
			}

			Function.getScreen(testNo);
		}
		
	}
	public static void submit(){
		Function.clickElement(PageSelector.loginButton);
	}
    /**
     * 先进入菜单再登录
     */	
	public void logninKeyBehind(String userName,String passWord){
		Function.switchToFrame(PageSelector.iframeX);
		Function.inputText(PageSelector.userId, userName);
//		userPw(passWord);
//		conButton();
		Function.clickElement(PageSelector.loginButton);	
		Function.clickElement(PageSelector.maskClose);
		Function.switchToParentFrame();
	}

	/**
	 * 客户信息补录，点击跳过按钮
	 */
	public void loginJump() {
	 	Function.switchToCurrentPageAndNoCloseFormer();
        Function.switchToFrame(PageSelector.contentIframe);
        boolean a=Function.isElementExist(PageSelector.Pass);
        if(a==true){
        	try {
				Thread.sleep(5000);
				Function.clickElement(PageSelector.Pass);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	   Function.switchToParentFrame();
        }                
	}
	/**
	 * 关闭弹框
	 */
	public void kuangButton0() { 
		boolean a=Function.isElementExist(PageSelector.kuangButton0);
        if(a==true){				
			Function.clickElement(PageSelector.kuangButton0);			
        } 
	}
	/**
	 * 暂不签订
	 */
	public void UpdatePrivacyPolicy() { 
        boolean a=Function.isElementExist(PageSelector.popupDivTopDiv);
        if(a==true){				
			Function.clickElement(PageSelector.popupDivTopDiv);			
        }                
	}
	
	/**
	 * 点击关闭
	 */
	public void kuangButton1() { 
        boolean a=Function.isElementExist(PageSelector.kuangButton1);
        if(a==true){				
			Function.clickElement(PageSelector.kuangButton1);			
        }                
	}
	public  void login(String url, String browser, String userName,String  passW, String testNo){
        if(url.contains("211")||url.contains("36")){
        	
        	loginKeyborad(url,browser,userName,passW,testNo);
        }else{
        	loginEnterPassword(url, browser, userName, passW, testNo);
            loginSendMsg(testNo); 	
        }
		
	}
	  /**
     * 获取数据
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
	public Iterator<Object[]> getData(String sql) throws ClassNotFoundException, SQLException {
        ResultSet result = Function.getNewTestData(sql);
        List returnTobe = new ArrayList();
        while (result.next()) {
            LoginBean loginBean = new LoginBean();
//            loginBean.setTestdata_username(result.getString("testdata_username"));
//            loginBean.setQuery_password(result.getString("query_password"));
//            loginBean.setTestcase_no(result.getString("testcase_no"));
//            loginBean.setTestcase_name(result.getString("testcase_name"));
//            loginBean.setTestcase_desc(result.getString("testcase_desc"));
//            loginBean.setExpected_result(result.getString("expected_result"));
//            loginBean.setMethod(result.getString("method"));
//            loginBean.setStatus(result.getString("status"));
            returnTobe.add(new Object[]{loginBean});
        }
        return returnTobe.iterator();
    }
	
	public static void main(String args[]){
		String url="xxx";
		String browser="chrome70";
		String userName="xxx";
		String passWord="xxx";
		String testName="00";
		siFangQLoginEnterPassword(url,browser,userName,passWord,testName);
		submit();
		String a=Function.getAlertText();
		System.out.print("fanhui="+a);
	}

}