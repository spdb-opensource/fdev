package com.auto.common;


import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class Function {

    public static WebDriver driver = null;
    public static Select select = null;
    private static long timeOutInSeconds = 10;
    private static Alert alert = null;
    private static String userName=ReadConfig.userName;
    private static String passWord=ReadConfig.passWord;
    private static String dbDriver=ReadConfig.dbDriver;
    
    
    /**
     * 初始化浏览器
     *
     * @param browser 参数值为ie, ff, chrome
     * @return WebDriver
     */
    private static WebDriver driver(String browser) {
    	ChromeOptions opts = new ChromeOptions();
    	opts.addArguments("--headless");
        switch (browser) {
            case "ie":
                System.setProperty("webdriver.ie.driver", ".\\tools\\IEDriverServer.exe");
                driver = new InternetExplorerDriver();
                break;
            case "ff":
            case "firefox":
            case "Firefox":
            case "FireFox":
                System.setProperty("webdriver.firefox.bin", "D:\\tool\\Firefox\\firefox.exe");
                driver = new FirefoxDriver();
                break;
            case "chrome30":
            case "Chrome30":
                System.setProperty("webdriver.chrome.driver", ".\\tools\\chrome30\\chromedriver.exe");
                driver = new ChromeDriver(opts);
                break;
            case "chrome62":
            case "Chrome62":
                System.setProperty("webdriver.chrome.driver", ".\\tools\\chrome62\\chromedriver.exe");
                driver = new ChromeDriver(opts);
                break;
            case "chrome70":
            case "Chrome70":
                System.setProperty("webdriver.chrome.driver", ".\\tools\\chrome70\\chromedriver.exe");
                driver = new ChromeDriver(opts);
                break;
            case "chrome73":	
            case "Chrome73":
                System.setProperty("webdriver.chrome.driver", "/Google/Chrome/Application/chromedriver.exe");
                driver = new ChromeDriver(opts);
                break;
            default:
                try {
                    throw new Exception("浏览器错误!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return driver;
    }

    /**
     * 指定浏览器打开URL
     *
     * @param url
     * @param browser
     */
    public static void openUrl(String url, String browser) {
        driver = driver(browser);
        driver.manage().timeouts().implicitlyWait(timeOutInSeconds, TimeUnit.SECONDS);
        driver.get(url);
        driver.manage().window().maximize();
    }
    
    /**
     * 在当前浏览器打开新地址并切换
     * @param url
     */
    public static void openUrlInBrower(String url) {
    	JavascriptExecutor js = (JavascriptExecutor) driver;
        String window = "window.open('" + url + "')";
        js.executeScript(window);
        switchToNextPage();
    }

    /**
     * 关闭当前浏览器
     */
    public static void closeCurrentBrowser() {
        driver.close();
    }

    /**
     * 关闭所有selenium驱动打开的浏览器
     */
    public static void closeAllBrowser() {
        driver.quit();
    }
      
    public static void closeBrowser(){
    	try {
			Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");
			Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
    } 
    /**
     * 自定义设置浏览器尺寸
     *
     * @param width
     * @param heigth
     */
    public static void setBrowserSize(int width, int heigth) {
        driver.manage().window().setSize(new Dimension(width, heigth));
    }

    /**
     * 获取当前浏览器页面的URL的字符串
     *
     * @return 85
     */
    public static String getURL() {
        return driver.getCurrentUrl();
    }

    /**
     * 获取当前浏览器页面的标题
     *
     * @return 93
     */
    public static String getTitle() {
        return driver.getTitle();
    }

    /**
     * 在浏览器的历史中向后到上一个页面, 即点击浏览器返回
     */
    public static void returnToPreviousPage() {
        driver.navigate().back();
    }

    /**
     * 在浏览器的历史中向前到下一个页面, 如果我们在最新的页面上看, 什么也不做, 即点击浏览器下一页
     */
    public static void forwardToNextPage() {
        driver.navigate().forward();
    }

    /**
     * 最大化浏览器
     */
    public static void maxBrowser() {
        driver.manage().window().maximize();
    }

    /**
     * 设置等待
     */
    public static void wait(int seconds) {
        driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    /**
     * 关闭或取消弹出对话框
     */
    public static void dismissAlert() {
        alert = driver.switchTo().alert();
        alert.dismiss();
    }

    /**
     * 弹出对话框点击确定
     */
    public static void acceptAlert() {
        alert = driver.switchTo().alert();
        alert.accept();
    }

    /**
     * 获取弹出对话框内容
     *
     * @return259
     */
    public static String getAlertText() {
        alert = driver.switchTo().alert();
        return alert.getText();
    }

    /**
     * 判断弹出框
     * @return259      */
    public static boolean isAlertDia() {
    	try{
        alert = driver.switchTo().alert();
        return true;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    /**
     * 弹出对话框输入文本字符串
     *
     * @param text
     */
    public static void inputTextToAlert(String text) {
        alert = driver.switchTo().alert();
        alert.sendKeys(text);
    }

    /**
     * 清空文本输入框
     *
     * @param locator
     * @see org.openqa.selenium.WebElement.clear()
     */
    public static void clearText(By locator, int n) {
    	for(int i = 1;i <= n;i ++) {
    		driver.findElement(locator).sendKeys(Keys.BACK_SPACE);
    	}
    }
    
    /**
     * 根据定位点击回车
     * @param locator
     */
    public static void clickByEnter(By locator) {
        driver.findElement(locator).sendKeys(Keys.ENTER);
    }
    
    /**
     * 获取该元素的可见（没有被CSS隐藏）内文, 包括子元素, 没有任何前导或尾随空白
     *
     * @param locator
     * @return 此元素的内部文本
     */
    public static String text(By locator) {
        return driver.findElement(locator).getText();
    }
    /**
     * 获取该元素详情文本content-desc
     * @param locator
     * @return
     */
    public static String desc(By locator){
    	return driver.findElement(locator).getAttribute("content-desc");
    }
    
    /**
     * 根据元素点击回车
     * @param element
     * @return
     */
    public static void clickElementEnter(By locator) {
    	driver.findElement(locator).sendKeys(Keys.ENTER);
    }
    
    /**
     * 测试报告中 输出测试日志
     * 
     * @param module 案例模块
     * @param testno 案例编号
     * @param name   案例名称
     * @param step   案例描述/操作步骤
     */
    public static void print(String module, String testno, String name, String step) {
        System.out.print(module + testno + " ");
        Reporter.log("案例模块：" + module);
        Reporter.log("案例编号：" + testno);
        Reporter.log("案例名称：" + name);
        Reporter.log("操作步骤：" + step);
    }
    
    /**
     * 全屏截图
     *
     * @param testName:用例名称
     */
    public static void getScreen(String testName) {
        //指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        //设置文件名为当前时间
        String filename = simpleDate();
        try {
            //利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。
            FileUtils.copyFile(srcFile, new File(".//screenshot//" + testName + "_" + filename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全屏截图
     *
     * @param testName:用例名称
     */
    public static void getScreenShot(String module, String testNo) {
        //指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        //设置文件名为当前时间
        String filename = simpleDate();
        try {
            //利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。
            FileUtils.copyFile(srcFile, new File(".//screenshot//" + module + "//" + testNo + "_" + filename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 报错截图
     *
     * @param testNo:用例编号
     * @param module:用例模块
     * @param error:截图文件自定义前缀名称
     * @author xxx
     */
    public static void getScreenShot(String module, String testNo, String error) {
        // 指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        // 设置文件名为当前时间
        String filename = simpleDate();
        try {
            // 利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。
            FileUtils.copyFile(srcFile,
                    new File(".//screenshot//" + module + "//" + testNo + "_" + error + "_" + filename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取系统时间，定义时间格式为yyyy-MM-dd-HH-mm-ss
    public static String simpleDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        String toDate = df.format(new Date());
        return toDate;

    }

    /**
     * 元素是否可见
     *
     * @param locator 定位器
     * @return
     */
    public static boolean ObjectIsEnabled(By locator) {
        boolean isOK;
        try {
            isOK = driver.findElement(locator).isEnabled();
        } catch (NoSuchElementException e) {
            isOK = false;

        }
        return isOK;
    }

    /**
     * 判断页面元素是否存在
     *
     * @param locator 定位器
     * @return
     */
    public static boolean isElementExist(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * 输入文本字符串
     *
     * @param locator
     * @param text
     */
    public static WebElement inputText(By locator, String text) {
        driver.findElement(locator).sendKeys(text);
        return driver.findElement(locator);
    }
    
    /**
     * 输入文本字符串
     *
     * @param locator
     * @param text
     */
    public static WebElement inputElement(WebElement element, String text) {
    	element.sendKeys(text);
        return element;
    }

    /**
     * 点击元素
     *
     * @param locator 定位器
     */
    public static void clickElement(By locator) {
        driver.findElement(locator).click();
    }

    /**
     * 定位元素
     *
     * @param locator 定位器
     * @return
     */
    public static WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    /**
     * 上传文件
     *
     * @param locator
     * @param filePath
     */
    public static void uploadFile(By locator, String filePath) {
        driver.findElement(locator).sendKeys(filePath);
    }

    /**
     * 刷新页面
     */
    public static void refreshPage() {
        driver.navigate().refresh();
    }

    /**
     * WebDriver切换到当前页面
     */
    public static void switchToCurrentPage() {
        String handle = driver.getWindowHandle();
        for (String tempHandle : driver.getWindowHandles()) {
            if (tempHandle.equals(handle)) {
                driver.close();
            } else {
                driver.switchTo().window(tempHandle);
            }
        }
    }

    /**
     * 获取新打开页面
     */
    public static void switchToNextPage() {
        Set<String> handles = driver.getWindowHandles();
        List<String> it = new ArrayList<String>(handles);
        driver.switchTo().window(it.get(1));
    }
    /**
     * 返回旧页面
     */
    public static void switchToPrePage() {
        Set<String> handles = driver.getWindowHandles();
        List<String> it = new ArrayList<String>(handles);
        driver.switchTo().window(it.get(0));
    }
    
    public static void switchToFixedPage(int n) {
    	Set<String> handles = driver.getWindowHandles();
        List<String> it = new ArrayList<String>(handles);
        driver.switchTo().window(it.get(n));
    }

    /** 
     * WebDriver切换到当前页面/浏览器(不关闭上一个页面/浏览器) 
     */ 
    public static void switchToCurrentPageAndNoCloseFormer() { 
        String handle = driver.getWindowHandle(); 
        for (String tempHandle : driver.getWindowHandles()) { 
            if(!tempHandle.equals(handle)) { 
                driver.switchTo().window(tempHandle); 
            } 
        } 
    } 
    
    
    /** 
     * WebDriver返回原页面/浏览器（关闭当前页面/浏览器） 
     */ 
    public static void switchToFormerPage() { 
        String handle = driver.getWindowHandle(); 
        for (String tempHandle : driver.getWindowHandles()) { 
            if(!tempHandle.equals(handle)) { 
                driver.close(); 
                driver.switchTo().window(tempHandle); 
            } 
        } 
    } 

    /**
     * 提交表单
     *
     * @param locator
     * @see org.openqa.selenium.WebElement.submit()
     */
    public static void submitForm(By locator) {
        driver.findElement(locator).submit();
    }

    /**
     * 获取该元素的可见（没有被CSS隐藏）内文, 包括子元素, 没有任何前导或尾随空白
     *
     * @param locator
     * @return 此元素的内部文本
     */
    public static String getElementText(By locator) {
        return driver.findElement(locator).getText();
    }

    public static String getElementValue(By locator){
		return driver.findElement(locator).getAttribute("value");
    }
    
    /**
     * 获取当前元素
     */
     public WebElement getElement(By locator){
         return driver.findElement(locator);
     }
    
    /**
     * @param locator
     * @return
     */
    public static String getElementTextAcount(By locator) {
        return driver.findElement(locator).getText();
    }

    /**
     * 下拉选择框根据选项文本值选择, 即当text="foo", 那么这一项将会被选择:
     * &lt;option value="foo"&gt;Bar&lt;/option&gt;
     *
     * @param locator
     * @param text
     * @see org.openqa.selenium.support.ui.Select.selectByVisibleText(String text)
     */
    public static void selectByValue(By locator, String text) {
        select = new Select(driver.findElement(locator));
        select.selectByValue(text);
    }

    /**
     * 下拉选择框根据选项文本值选择, 即当x=1, 那么这一项将会被选择:
     * &lt;option value="foo"&gt;Bar&lt;/option&gt;
     *
     * @param locator
     * @param x
     * @see org.openqa.selenium.support.ui.Select.selectByVisibleText(String text)
     */
    public static void selectByIndex(By locator, int x) {
        select = new Select(driver.findElement(locator));
        select.selectByIndex(x);
    }

    /**
     * 下拉选择框根据选项文本值选择, 即当text="Bar", 那么这一项将会被选择:
     * &lt;option value="foo"&gt;Bar&lt;/option&gt;
     *
     * @param locator
     * @param text
     * @see org.openqa.selenium.support.ui.Select.selectByVisibleText(String text)
     */
    public static void selectByText(By locator, String text) {
        select = new Select(driver.findElement(locator));
        select.selectByVisibleText(text);
    }

    /**
     * 切换frame
     *
     * @param locator
     * @return 这个驱动程序切换到给定的frame
     * @see org.openqa.selenium.WebDriver.TargetLocator.frame(WebElement frameElement)
     */
    public static void switchToFrame(By locator) {
        driver.switchTo().frame(driver.findElement(locator));
    }

    /**
     * 切换回父frame
     *
     * @return 这个驱动程序聚焦在顶部窗口/第一个frame上
     * @see org.openqa.selenium.WebDriver.TargetLocator.defaultContent()
     */
    public static void switchToParentFrame() {
        driver.switchTo().defaultContent();
    }

    public static String getWindowTitle() {
        return driver.getTitle();
    }
    
    /**
    	切换下一行输入 并返回下一行元素
     */
    public static WebElement switchToNextElement(WebElement element){
        element.sendKeys(Keys.TAB);
        return driver.switchTo().activeElement();
    }
    
    /**
     * 每0.5秒检测一次，等待指定秒数，直至指定元素出现后点击
     * 
     * @param locator 定位器
     * @return
     * @author xxx
     */
    public static void waitClick(By locator, int second) {
        wait(locator, second);
        clickElement(locator);
    }
    
    /**
     * 每0.5秒检测一次，默认等待5秒，直至指定元素出现或超时
     * 
     * @param locator 定位器
     * @return
     * @author xxx
     */
    public static boolean wait(By locator) {
        return wait(locator, 3);
    }
    
    /**
     * 每0.5秒检测一次，等待5秒，直至指定元素出现后点击
     * 
     * @param locator 定位器
     * @return
     * @author xxx
     * @throws InterruptedException 
     */
    public static void waitClick(By locator) throws InterruptedException {
    	Thread.sleep(500);
        if (wait(locator)) {
        	clickElement(locator);
        }
    }
    
    /**
     * 每0.5秒检测一次，自定义等待时间，直至指定元素出现或超时
     * 
     * @param locator 定位器
     * @param second  等待时间-单位:秒
     * @return
     * @author xxx
     */
    public static boolean wait(By locator, int second) {
        WebDriverWait wait = new WebDriverWait(driver, second);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据cookie名称删除cookie
     *
     * @param name cookie的name值
     * @see org.openqa.selenium.WebDriver.Options.deleteCookieNamed(String name)
     */
    public static void deleteCookie(String name) {
        driver.manage().deleteCookieNamed(name);
    }

    /**
     * 删除当前域的所有Cookie
     *
     * @see org.openqa.selenium.WebDriver.Options.deleteAllCookies()
     */
    public static void deleteAllCookies() {
        driver.manage().deleteAllCookies();
    }

    /**
     * 根据名称获取指定cookie
     *
     * @param name cookie名称
     * @return Map&lt;String, String>, 如果没有cookie则返回空, 返回的Map的key值如下:<ul>
     * <li><tt>name</tt>        <tt>cookie名称</tt>
     * <li><tt>value</tt>        <tt>cookie值</tt>
     * <li><tt>path</tt>        <tt>cookie路径</tt>
     * <li><tt>domain</tt>        <tt>cookie域</tt>
     * <li><tt>expiry</tt>        <tt>cookie有效期</tt>
     * </ul>
     * @see org.openqa.selenium.WebDriver.Options.getCookieNamed(String name)
     */
    public static Map<String, String> getCookieByName(String name) {
        Cookie cookie = driver.manage().getCookieNamed(name);
        if (cookie != null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", cookie.getName());
            map.put("value", cookie.getValue());
            map.put("path", cookie.getPath());
            map.put("domain", cookie.getDomain());
            map.put("expiry", cookie.getExpiry().toString());
            return map;
        }
        return null;
    }

    /**
     * 获取当前域所有的cookies
     *
     * @return Set&lt;Cookie>    当前的cookies集合
     * @see org.openqa.selenium.WebDriver.Options.getCookies()
     */
    public static Set<Cookie> getAllCookies() {
        return driver.manage().getCookies();
    }

    /**
     * 用给定的name和value创建默认路径的Cookie并添加, 永久有效
     *
     * @param name
     * @param value
     * @see org.openqa.selenium.WebDriver.Options.addCookie(Cookie cookie)
     * @see org.openqa.selenium.Cookie.Cookie(String name, String value)
     */
    public static void addCookie(String name, String value) {
        driver.manage().addCookie(new Cookie(name, value));
    }

    /**
     * 用给定的name和value创建指定路径的Cookie并添加, 永久有效
     *
     * @param name  cookie名称
     * @param value cookie值
     * @param path  cookie路径
     */
    public static void addCookie(String name, String value, String path) {
        driver.manage().addCookie(new Cookie(name, value, path));
    }

    /**
     * 指定时间内等待直到页面包含文本字符串
     *
     * @param text    期望出现的文本
     * @param seconds 超时时间
     * @return Boolean    检查给定文本是否存在于指定元素中, 超时则捕获抛出异常TimeoutException并返回false
     * @see org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement(WebElement element, String text)
     */
    public static Boolean waitUntilPageContainText(String text, long seconds) {
        try {
            return new WebDriverWait(driver, seconds).until(ExpectedConditions.textToBePresentInElement
                    (driver.findElement(By.tagName("body")), text));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 默认时间等待直到页面包含文本字符串
     *
     * @param text 期望出现的文本
     * @return Boolean    检查给定文本是否存在于指定元素中, 超时则捕获抛出异常TimeoutException并返回false
     * @see org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement(WebElement element, String text)
     */
    public static Boolean waitUntilPageContainText(String text) {
        try {
            return new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.textToBePresentInElement
                    (driver.findElement(By.tagName("body")), text));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定时间内等待直到元素存在于页面的DOM上并可见, 可见性意味着该元素不仅被显示, 而且具有大于0的高度和宽度
     *
     * @param locator 元素定位器
     * @param seconds 超时时间
     * @return Boolean    检查给定元素的定位器是否出现, 超时则捕获抛出异常TimeoutException并返回false
     * @see org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By locator)
     */
    public static Boolean waitUntilElementVisible(By locator, int seconds) {
        try {
            new WebDriverWait(driver, seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * 默认时间内等待直到元素存在于页面的DOM上并可见, 可见性意味着该元素不仅被显示, 而且具有大于0的高度和宽度
     *
     * @param locator 元素定位器
     * @return Boolean    检查给定元素的定位器是否出现, 超时则捕获抛出异常TimeoutException并返回false
     * @see org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By locator)
     */
    public static Boolean waitUntilElementVisible(By locator) {
        try {
            new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 元素可以点击
     * @param locator
     * @return
     */
    public static boolean waitElementToBeClickable(By locator) {
    	boolean click = false;
    	try {
    		new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.elementToBeClickable(locator));
    		click = true;
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return click;
    }
    
    /**
     * 等待元素可以点击,并点击该元素
     * @param locator
     */
    public static void clickElementToBeClickable(By locator) {
    	boolean click = waitElementToBeClickable(locator);
    	if(click) {
    		driver.findElement(locator).click();
    	} else {
    		System.out.println(locator.toString() + "元素不可点击");
    	}
    }

    /**
     * 选择汇款账号，若账户余额为0，则选择下一张卡。
     */
    public static void acctNo(By locator, String text) {

        WebElement selectX = findElement(locator);
        Select selectAcctNo = new Select(selectX);

        int i = 0;
        while (text.equals(0.00)) {
            try {

                selectAcctNo.selectByIndex(i);
                i++;
                Function.wait(5);
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }
    
   

    /**
     * 输入测试日志
     *
     * @param name:案例名称
     * @param step        :操作步骤
     * @param expect      :预期结果
     * @param acturl:实际结果
     * @param result:验证结果
     */
    
    public static void print(String name, String step) {
  
        Reporter.log("案例名称：" + name);
        Reporter.log("操作步骤：" + step);
    }
    public static void print(String testno) {
    	  
        Reporter.log("案例编号：" + testno);

    }

    public static void print(String acturl, String expect, String result) {

        Reporter.log("预期结果：" + expect);
        Reporter.log("实际结果：" + acturl);
        Reporter.log("验证结果：" + result);
    }

 

    
    static ResultSet result;  //结果集
    static List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();  //生成存放结果集的list
    int rowNum = 0;     //总行数
    int curRowNo = 0;   //当前行数

    public static Object[][] getTestData(String sql) throws SQLException, ClassNotFoundException {

    	String url=null;
    			if(Integer.parseInt(ReadConfig.dbflag)==0){
    				url=ReadConfig.url;
    			}else if(Integer.parseInt(ReadConfig.dbflag)==1){
    				url=ReadConfig.mock_url;
    			}
    			
//		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        Class.forName(dbDriver);
        //获取连接
        Connection conn = DriverManager.getConnection(url, userName, passWord);
        if (!conn.isClosed()) {
            System.out.print("数据库连接成功");
        } else {
            System.out.print("数据库连接失败");
        }
        //获取创建语句对象
        Statement stmt = conn.createStatement();
        //执行sql语句，获取查询结果集
        result = stmt.executeQuery(sql);
        List<Object[]> listStr = new ArrayList<Object[]>();
        int colNum = result.getMetaData().getColumnCount();
        while (result.next()) {
            String[] str = new String[colNum];
            for (int i = 0; i < str.length; i++) {
                str[i] = result.getString(i + 1);
                System.out.print(str[i]);

            }
            listStr.add(str);
        }

        Object[][] results = new Object[listStr.size()][];
        for (int i = 0; i < listStr.size(); i++) {
            results[i] = listStr.get(i);
        }
        result.close();
        conn.close();
        stmt.close();
        return results;
    }

    public static ResultSet getNewTestData(String sql) throws SQLException, ClassNotFoundException {
    	String dburl=null;
		if(Integer.parseInt(ReadConfig.dbflag)==0){
			 dburl=ReadConfig.url;
		}else if(ReadConfig.dbflag.equals("1")){
			dburl=ReadConfig.MockDB_url;
		}
//		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        Class.forName(dbDriver);
        //获取连接
        Connection conn = DriverManager.getConnection(dburl, userName,passWord);
        if (!conn.isClosed()) {
            System.out.print("数据库连接成功");
        } else {
            System.out.print("数据库连接失败");
        }
        //获取创建语句对象
        Statement stmt = conn.createStatement();
//      执行sql语句，获取查询结果集
        return stmt.executeQuery(sql);
    }
    
    /**
     * 把ResultSet转成Iterator<Object[]>，case中可直接使用，不需要再进行转换
     * @param sql
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Iterator<Object[]> getNewTestData1(String sql) throws SQLException, ClassNotFoundException {
    	String url=null;
		if(Integer.parseInt(ReadConfig.dbflag)==0){
			url=ReadConfig.url;
		}else if(Integer.parseInt(ReadConfig.dbflag)==1){
			url=ReadConfig.MockDB_url;
		}
    	 Class.forName(dbDriver);
         //获取连接
         Connection conn = DriverManager.getConnection(url,userName, passWord);
         if (!conn.isClosed()) {
             System.out.println("数据库连接成功");
         } else {
             System.out.println("数据库连接失败");
         }
         
         //获取创建语句对象
         Statement stmt = conn.createStatement();
         //执行sql语句，获取查询结果集
         result=stmt.executeQuery(sql);
    	 int colNum = result.getMetaData().getColumnCount();
         List<Object[]> listStr = new ArrayList<Object[]>();
         while (result.next()) {
             String[] str = new String[colNum];
             for (int i = 0; i < str.length; i++) {
                str[i] = result.getString(i + 1);
                System.out.print(str[i]);
             }
             listStr.add(str);
         }      
         Iterator<Object[]> iterator = listStr.iterator();
         result.close();
         conn.close();
         stmt.close();
    	 return iterator;
    }
    
    public static int updateTestData(String sql) throws SQLException, ClassNotFoundException {
    	String dburl=null;
		if(Integer.parseInt(ReadConfig.dbflag)==0){
			 dburl=ReadConfig.url;
		}else if(ReadConfig.dbflag.equals("1")){
			dburl=ReadConfig.MockDB_url;
		}
        Class.forName(dbDriver);
        //获取连接
        Connection conn = DriverManager.getConnection(dburl, userName,passWord);
        if (!conn.isClosed()) {
            System.out.print("数据库连接成功");
        } else {
            System.out.print("数据库连接失败");
        }
        //获取创建语句对象
        Statement stmt = conn.createStatement();
//      执行sql语句，获取查询结果集
        return stmt.executeUpdate(sql);
    } 

    public boolean hasNext() {
        return !(rowNum == 0 || curRowNo >= rowNum);
    }

    public Object[] next() {
        Map<String, String> s = dataList.get(curRowNo);
        Object[] d = new Object[1];
        d[0] = s;
        this.curRowNo++;
        return d;
    }

    public void remove() {
        throw new UnsupportedOperationException("remove unsupported");
    }

    public static void updateValue(String actId, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String scroll = "var fa1=document.getElementById(\"" + actId + "\"); fa1.setAttribute(\"value\",\"" + value + "\");";
        js.executeScript(scroll);
    }
    
    public static String request(String param) throws Exception{

		StringBuffer buffer=null;
//			try {
//				new JsonsUtil2();	
				URL serverUrl= new URL("http://iams.t-guz.minikube");
				URLConnection uct= serverUrl.openConnection();
				HttpURLConnection hutc=(HttpURLConnection)uct;	
				// 设置报文参数								
				hutc.setRequestMethod("POST");				
				// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在 http正文内，因此需要设为true, 默认情况下是false; 
				hutc.setDoOutput(true);				
				// 设置是否从httpUrlConnection读入，默认情况下是true
				hutc.setDoInput(true);	
//				hutc.setAllowUserInteraction(true);
				hutc.setUseCaches(false);
				hutc.setRequestProperty("Content-Type", "application/json");
				hutc.setRequestProperty("Accept", "*/*");
				hutc.setRequestProperty("Connection", "keep-alive");
				hutc.setRequestProperty("accept-encoding", "gzip, deflate");
				hutc.connect();
				// 开启流，写入数据data
				OutputStream out=hutc.getOutputStream();			
				out.write(param.getBytes());				
				out.flush();
				out.close();				
				// 获取返回的数据	
				buffer=new StringBuffer();
				BufferedReader reader = null;
				InputStream ins=hutc.getInputStream();
				reader = new BufferedReader(new InputStreamReader(ins,"UTF-8"));
				String sg=reader.readLine();
				if (sg!= null){
			           buffer.append(sg);
			     }
				
		/*	} catch (Exception e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("请求失败："+e);
				throw e;
				
			}*/
			return buffer.toString();//返回response数据
		}
	/**
	 * 构建请求头 Reqheader
	 * @return
	 * @throws JSONException
	 */
	/**
	 * 构建请求头 Reqheader
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject jsonBuild(String caseId) throws JSONException {
	   	 Map<String, String> map1 = new HashMap<String, String>();
	        map1.put("CaseId", caseId);
	       JSONObject jo = JSONObject.fromObject(map1);;
	       return jo;
	   }
	
	/*
    移动滚动条到底部
	 */
	public static void scrollTobutton() throws InterruptedException{
		Thread.sleep(100);
		String js2="document.documentElement.scrollTop=10000";
		((JavascriptExecutor)driver).executeScript(js2);
		Thread.sleep(100);
	}

	/*
    移动滚动条到顶部
	 */
	public static void scrollToTop() throws InterruptedException{
		Thread.sleep(100);
		String js2="document.documentElement.scrollTop=0";
		((JavascriptExecutor)driver).executeScript(js2);
		Thread.sleep(100);
	} 
	
    public static void Forcewait(int time) throws InterruptedException{
    	Thread.sleep(time);
    };

}
	
	   
		 

