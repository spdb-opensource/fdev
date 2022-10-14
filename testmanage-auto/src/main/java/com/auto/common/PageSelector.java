package com.auto.common;

import org.openqa.selenium.By;

/**
 * Created by xxx on 2018-09-07.
 */

public class PageSelector {
	/* 登录 */
	// public static By textCenter = By.xpath("//div[@class='textCenter']/div[2]");
	// public static By conforimButn = By.xpath("//button[text()='确定']");
	public static By conX = By.xpath("//div[@id='keybordskopener']/div/div[2]/ul[4]/li[8]");
	public static By iframeX = By.xpath("//iframe[@name='login-iframe']");
	public static By userId = By.id("LoginId");
	public static By pwdImg = By.xpath("//div[@id='skopener']/img");
	// public static By pwdImg = By.xpath("//div[@class='softIcon']/img");
	public static By pwd = By.id("Password");
	public static By sendPasswd = By.id("SendPasswd");
	public static By sendMsg = By.id("sendMsg");
	public static By loginButton = By.name("LoginButton");
	public static By errInfo = By.xpath("//div[@class='loginErr']");
	public static By maskClose = By.xpath("//div[@class='mask-wrapper1']/div[1]/img[2]");
	public static By userHover = By.id("user-hover");
    public static By myInfo = By.xpath("//div[@id='myinfo']/div[1]/div/div[2]/div/a");
//    public static By cIframex = By.xpath("//iframe[@name='content-iframe']");
    public static By tokenImg = By.xpath("//img[@id='tokenImg']");
    //下次再说
    public static By nextSay = By.xpath("//button[text()='下次再说']");
    public static By JJ = By.id("JJ");
    public static By JJCC = By.id("JJCC");
    public static By PFB = By.id("PFB");
    public static By special = By.className("goCustom");
    public static By myFirstPageJumpFundSuccess = By.xpath("//span[text()='选择基金产品']");
    public static By myFirstPageJumpFundSuccess1 = By.xpath("//span[text()='基金持仓查询']");
    public static By myFirstPageJumpFundSuccess2 = By.xpath("//span[text()='我的普发宝']");
    //static By last=By.className("last");
    public static By last = By.xpath("//div[@id='hometop']/div[1]/div/ul[2]/li[6]/a");
    /*转账汇款*/
    public static By topNavListOne = By.xpath("//a[@id='menu1']");
    public static By topNavListTwo = By.xpath("//a[@id='menu2']");
    public static By AccountMenuOne = By.xpath("//a[@id='menu1_1_1']");
    public static By childMenuOne = By.xpath("//a[@id='menu2_1_1']");
    public static By childMenutwo = By.xpath("//a[@id='menu2_1_2']");
    public static By childMenuThree = By.xpath("//a[@id='menu2_1_3']");
    public static By childMenuFour = By.xpath("//a[@id='menu2_1_4']");
    public static By childMenuFive = By.xpath("//a[@id='menu2_1_5']");
    public static By childMenuSix = By.xpath("//a[@id='menu2_3_1']");
    public static By childMenuSeven = By.xpath("//a[@id='menu2_3_2']");
    public static By contentIframe = By.xpath("//iframe[@id='content-iframe']");
    //public static By acctNo=By.xpath("//select[@id='AcctNo']");
    public static By acctNo = By.name("AcctNo");
    public static By canUseBalance = By.xpath("//span[@id='CanUseBalance']");
    public static By amountCNY = By.id("Amount");//汇款金额
    public static By payeeName = By.xpath("//input[@id='PayeeName']");
    public static By payeeAcctNo = By.xpath("//input[@id='PayeeAcctNo']");
    public static By payeeBankClick = By.xpath("//a[@id='j-pickBank']");
    public static By PayeeType1_0 = By.xpath("//input[@id='PayeeType1_0']");//收款人账户类型 by：xxx
    public static By postScript = By.xpath("//input[@id='PostScript']");
    public static By radioOTP = By.xpath("//span[@id='dynamicPassword']");//动态密码
    public static By submitButton = By.xpath("//input[@id='SubmitButton']");
    public static By linkpeopleclick = By.className("link-people-click");
    public static By payeeselection = By.xpath("//span[@class='li-span-name']");
    public static By remitanceType2 = By.xpath("//span[@class='unit-wrapper']/span[2]/img");
    public static By provinceId = By.xpath("//select[@id='ProvinceId']");
    public static By CityId = By.xpath("//select[@id='CityId']");
    public static By deptId = By.xpath("//select[@id='DeptId']");
    public static By bankId = By.xpath("//select[@name='BankId']");
    public static By remitanceType1 = By.xpath("//span[@class='unit-wrapper']/span[1]/img");
    public static By payeeType = By.xpath("//select[@name='PayeeType']");
    public static By frequency = By.xpath("//select[@name='Frequency']");
    public static By departureDate = By.id("departure_date");
    public static By radioDigital = By.xpath("//input[@id='authType1']");//数字证书
    public static By textarea = By.id("RemitUsage");
    public static By inBankCheck = By.id("InBankCheck");
    public static By submit = By.id("SubmitButton");
    public static By radiotype2 = By.xpath("//input[@id='Type1']");//手机
    public static By mobile = By.id("mobile");
    public static By radiotype1 = By.xpath("//input[@id='Type0']");//email地址
    public static By email = By.id("email");
    public static By dotrans = By.name("dotrans");
    public static By Password = By.id("ExchangePassword");
    public static By mobilePasswd = By.id("MobilePasswd");
    public static By confirm = By.xpath("//input[@name='确定']");
    public static By rightText = By.xpath("//div[@class='new-account-title']");
    public static By Password2 = By.id("Password");
    public static By submit1 = By.name("submitButton"); //by guz
    public static By popIframe = By.xpath("//iframe[@id='popupIframe']");
    public static By wrapperTitle2 = By.xpath("//div[@id='cview7']/div/div/div/div[1]");//单日限额提示信息
    public static By totalNumber = By.id("TotalNumber");
    public static By uploadFile = By.id("UploadFile");
    public static By remark = By.id("Remark");
    public static By add1 = By.name("add1");
    public static By iframeC = By.xpath("//iframe[@name='content-iframe']");
    public static By choooseEleE=By.xpath("//map[@id='Map1']/area[1]");
    public static By choooseEleV=By.xpath("//map[@id='Map1']/area[2]");
    public static By loginPass = By.xpath("//form[@id='inputMobilePwd']");
    public static By textCenter=By.xpath("//div[@class='textCenter']/div[2]");
    public static By conforimButn=By.xpath("//div[@class='button-group']/div/button");
    //账号信息补录 跳过
    public static By Pass = By.xpath("//button[text()='跳过']");
  //关闭 只有理财版交易提示信息弹窗
    public static By kuangButton0 = By.id("kuangButton0");
    //关闭申报信息弹窗进入财智组合
    public static By kuangButton1 = By.id("kuangButton1");
    public static By popupDivTopDiv = By.id("popupDivTopDiv");
    public static By message = By.xpath("//div[@class='col-12']/div");
    public static By tableRes1 = By.xpath("//table[@id='remit']/tbody/tr[1]/td[1]/table/tbody/tr[1]/th");//跨行汇款成功
    
    
    
    
    
    
    
    
    
    /**
     * 登录页面 用户名
     */
    public static final By USERNAME = By.xpath("//input");
    /**
     * 登录页面 密码
     */
    public static final By PASSWORD = By.xpath("//input[@type='password']");
    /**
     * 登录页面 登录按钮
     */
    public static final By LOGIN_BUTTON = By.xpath("//button");
    
    /**  公告框-我知道了  */
    public static final By NOTIFY_KNOWS = By.xpath("/html/body/div[4]/div[2]/div/div[3]/div/button/span[2]/span/span");
    
    
}


