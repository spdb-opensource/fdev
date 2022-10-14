package com.auto.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.auto.bean.CaseDataBean;
import com.auto.bean.ModuleBean;

/**
 * 自动生成方法集
 * @author baimy
 *
 */
public class CommonFunction {
	 private static String dbDriver = ReadConfig.dbDriver;
	 private static String dburl = ReadConfig.url;
	 private static String userName = ReadConfig.userName;
	 private static String passWord = ReadConfig.passWord;
	
//	 private static String right_slide="right-slide";//右滑动
	 private static String down_slide="down-slide";//下滑动
	 
	 private static String input="input";//输入框
	 private static String waitinput = "waitinput";//等待录入
	 private static String left_slide="left-slide";//左滑动
	 private static String up_slide="up-slide";//上滑动
	 private static String up_slide_half = "up-slide-half";//上滑一半
	 private static String clear_text = "clear-text";//清空文本框
	 private static String click="clickElement";//点击事件
	 private static String waitClick = "waitClick";//等待点击
	 private static String wait_Click = "wait-Click";//等待点击（录入时间）
	 private static String whileClick="whileClick";//循环上滑半屏点击
	 private static String whileClick2 = "whileClick2";//循环等待点击
	 private static String coordClick = "coordClick";//坐标点击
	 private static String assertExists = "assert-exists";//断言-元素是否存在
	 private static String assertDescEquals = "assert-desc-equals";//断言-文本比对（desc）
	 private static String assertTextEquals = "assert-text-equals";//断言-文本比对（text）
	 private static String assertContains = "assert-contains";//断言-文本包含存在
	 private static String wait = "wait";//等待 默认5秒
	 private static String wait_element_time = "wait-element-time";//等待元素自定义时间
	 private static String safeInput = "safeInput";//安全键盘输入
	 private static String whileFindElementText = "whileFindElementText";//循环半屏获取参数text
	 private static String whileFindElementDesc = "whileFindElementDesc";//循环半屏获取参数desc
	 private static String isExist = "isExist";//元素不存在
	 private static String existClick = "existClick";//弹框处理
	 private static String sleep = "sleep";//强制等待
	 private static String slidingPageUpMpaas = "slidingPageUpMpaas";//向上短滑屏-
	 private static String nextElement = "switchToNextElement";
	 private static String fixedElement = "switchToFixedElement";
	 private static String intervalElement = "switchToIntervalElement";//切换到下一个输入框
	 private static String preElement = "inputPreElement";//预输入元素
	 private static String click_enter = "enter";
	 private static String scrollTobutton = "scrollTobutton";//向下滚动
	 private static String scrollToTop = "scrollToTop";//向上滚动
	 private static String switchToTab = "switchToTab";//tab键
	 private static String Forcewait="Forcewait";//强制等待
	 
	 private static String waitUntilElementVisible = "waitUntilElementVisible";//等待显示
	 private static String switchToNextPage = "switchToNextPage";//切换到新打开标签页面
	 private static String switchToPrePage = "switchToPrePage";//切换到上一个标签页面
	 private static String switchToFixedPage = "switchToFixedPage";//切换到固定标签页面
	 
	 
	 /**
     * 连接mysql案例数据库
     * 
     * @param sql
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ResultSet getNewTestData(String sql) throws SQLException, ClassNotFoundException {
        // DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        Class.forName(dbDriver);
        // 获取连接
        Connection conn = DriverManager.getConnection(dburl, userName, passWord);
        if (conn.isClosed()) {
            System.out.println("Database connection failed");
        }
        // 获取创建语句对象
        Statement stmt = conn.createStatement();
        // 执行sql语句，获取查询结果集
        return stmt.executeQuery(sql);
    }
	/**
	 * 组件sql数据转换
	 * @param sql
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
    public static Iterator<Object> getModuleData(String sql) throws ClassNotFoundException, SQLException {
    	System.out.println("sql:==="+sql);
        ResultSet result = getNewTestData(sql);
        List<Object> returnTobe = new ArrayList<Object>();
        while (result.next()) {
        	ModuleBean bean = new ModuleBean();
        	
        	if(isExistColumn(result,"module_id")){
        		bean.setModuleId(result.getString("module_id"));
        	}
        	if(isExistColumn(result,"module_no")){
        		bean.setModuleNo(result.getString("module_no"));
        	}
        	if(isExistColumn(result,"module_name")){
        		bean.setModuleName(result.getString("module_name"));
        	}
        	if(isExistColumn(result,"module_group")){
        		bean.setModuleGroup(result.getString("module_group"));
        	}
        	if(isExistColumn(result,"module_name_cn")){
        		bean.setModuleNameCn(result.getString("module_name_cn"));
        	}
        	if(isExistColumn(result,"element_id")){
        		bean.setElementId(result.getString("element_id"));
        	}
        	if(isExistColumn(result,"element_data")){
        		bean.setElementData(result.getString("element_data"));
        	}
        	if(isExistColumn(result,"element_dynamic_type")){
        		bean.setElementDynamicType(result.getString("element_dynamic_type"));
        	}
        	if(isExistColumn(result,"element_content")){
        		bean.setElementContent(result.getString("element_content"));
        	}
        	if(isExistColumn(result,"element_name")){
        		bean.setElementName(result.getString("element_name"));
        	}
        	if(isExistColumn(result,"element_dir")){
        		bean.setElementDir(result.getString("element_dir"));
        	}
        	if(isExistColumn(result,"exe_times")){
        		bean.setExeTimes(result.getString("exe_times"));
        	}
        	
        	if(isExistColumn(result,"testcase_no")){
        		bean.setTestcaseNo(result.getString("testcase_no"));
        	}
        	if(isExistColumn(result,"func_point")){
        		bean.setFuncPoint(result.getString("func_point"));
        	}
        	if(isExistColumn(result,"testcase_name")){
        		bean.setTestcaseName(result.getString("testcase_name"));
        	}
        	if(isExistColumn(result,"precondition")){
        		bean.setPrecondition(result.getString("precondition"));
        	}
        	if(isExistColumn(result,"testcase_desc")){
        		bean.setTestcaseDesc(result.getString("testcase_desc"));
        	}
        	if(isExistColumn(result,"expected_result")){
        		bean.setExpectedResult(result.getString("expected_result"));
        	}
        	
        	if(isExistColumn(result,"menu_no")){
        		bean.setMenuNo(result.getString("menu_no"));
        	}
        	if(isExistColumn(result,"meun_name")){
        		bean.setMeunName(result.getString("meun_name"));
        	}
        	if(isExistColumn(result,"secondary_menu")){
        		bean.setSecondaryMenu(result.getString("secondary_menu"));
        	}
        	if(isExistColumn(result,"username")){
        		bean.setUsername(result.getString("username"));
        	}
        	if(isExistColumn(result,"queryPwd")){
        		bean.setQueryPwd(result.getString("queryPwd"));
        	}
        	if(isExistColumn(result,"transPwd")){
        		bean.setTransPwd(result.getString("transPwd"));
        	}
        	if(isExistColumn(result,"productId")){
        		bean.setProductId(result.getString("productId"));
        	}
        	if(isExistColumn(result,"amount")){
        		bean.setAmount(result.getString("amount"));
        	}
        	if(isExistColumn(result,"rate")){
        		bean.setRate(result.getString("rate"));
        	}
        	if(isExistColumn(result,"productType")){
        		bean.setProductType(result.getString("productType"));
        	}
            returnTobe.add(bean);
        }
        return returnTobe.iterator();
    }
    /**
     * 字段是否存在
     * @param rs
     * @param columnName
     * @return
     */
    public static boolean isExistColumn(ResultSet rs, String columnName) {
    	try {			
    		if (rs.findColumn(columnName) > 0 ) {	
    			return true;			
    		} 	
		}catch (SQLException e) {	
    		return false;	
    	}		
    	return false;
	}
    
//    public static List<Map<String,String>> mapListOracle(ModuleBean bean){
//    	String sql = CommonModuleSql.Modulesql("");
//    	
//    	ResultSet result;
//    	List<Map<String,String>> map = new ArrayList<Map<String,String>>();
//    	Map<String,String> mapColumn = new LinkedHashMap<String,String>();
//    	mapColumn = new Map<String, String>().
//    	return null;
//    }
    
    /**
     * 组件方法生成
     * @param moduleGroup
     * @param path
     * @param rootpath
     * @param name
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public static void readModuleJavaTest(String moduleGroup,String path,String rootpath,String name) throws FileNotFoundException, ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
    	String paths = path.replace("/", ".");
    	String moduleGroups = moduleGroup+"Module";
		PrintStream prt = new PrintStream(new FileOutputStream(new File(rootpath, name)));
		prt.println("package " + paths + ";");
		prt.println("import org.openqa.selenium.By;");
		prt.println("import org.openqa.selenium.WebElement;");
		prt.println("import com.auto.common.BasePage;");
		prt.println("import com.auto.common.Function;");
		prt.println("import java.util.ArrayList;");
		prt.println("import com.auto.common.CommonModuleSql;");
		prt.println("import com.auto.bean.CaseDataBean;");
		prt.println("import com.auto.bean.ModuleBean;");
		prt.println("import com.auto.common.CommonFunction;");
		prt.println("public class " + moduleGroups + " extends BasePage {");
		//查询模块下所有组件
		ArrayList<ModuleBean> lst = getModuleCaseData(CommonModuleSql.Modulesql(moduleGroup));
		for (int i = 0; i < lst.size(); i++) {
			ModuleBean bean= (ModuleBean) lst.get(i);
			prt.println("/**");
			prt.println("*" + bean.getModuleNameCn());
			prt.println("*/");
			prt.println("public static void " + bean.getModuleName() + "(String name,CaseDataBean data)throws Exception{");
//			prt.println("try {");
			prt.println("ArrayList<ModuleBean> rtn = CommonFunction.getModuleCaseData(CommonModuleSql.CaseModuleDetailSql(name));");
			
			//查询组件元素详细信息
			ArrayList<ModuleBean> mlst = getModuleCaseData(CommonModuleSql.ModuleDetailSql(bean.getModuleNo()));
				for (int j = 0; j < mlst.size(); j++) {
					ModuleBean mbean= (ModuleBean) mlst.get(j);
					readModuleMethod(mbean, prt, j);
				}
//			prt.println("} catch (Exception e) {");
//			prt.println("e.printStackTrace();");
//			prt.print("}");
			prt.println("}");
		}
		prt.println("}");
		prt.close();
    }
    
    /**
     * 元素动态操作写入方法
     * @param mbean
     * @param prt
     * @param number
     */
    public static void readModuleMethod(ModuleBean mbean, PrintStream prt, int number){
    	System.out.println(mbean.getElementDynamicType());
    	String elementContent = null;
    	if(mbean.getElementContent() != null && !"".equals(mbean.getModuleGroup())){
    		if(mbean.getElementType().equals("id")){
    			elementContent = "By.id(rtn.get("+number+").getElementContent())";
    		}else if(mbean.getElementType().equals("xpath")){
    			elementContent = "By.xpath(rtn.get("+number+").getElementContent())";
    		}
    	}
    	prt.println("//操作："+mbean.getElementName());
		//点击
		if(click.equals(mbean.getElementDynamicType()) ){
			prt.println("Function.clickElement("+elementContent+");");
		}
		//等待点击
		else if(waitClick.equals(mbean.getElementDynamicType())){
			prt.println("Function.waitClick("+elementContent+");");
		}
		//显示等待
		else if(waitUntilElementVisible.equals(mbean.getElementDynamicType())){
			prt.println("Function.waitUntilElementVisible(" + elementContent + ");");
		}
		// 等待    直到元素可以点击
		else if("waitElementToBeClickable".equals(mbean.getElementDynamicType())){
			prt.println("Function.waitElementToBeClickable(" + elementContent + ");");
		}
		// 点击    直到元素可以点击
		else if("clickElementToBeClickable".equals(mbean.getElementDynamicType())){
			prt.println("Function.clickElementToBeClickable(" + elementContent + ");");
		}
		else if("refreshPage".equals(mbean.getElementDynamicType())){
			prt.println("Function.refreshPage();");
		}
		// 在当前浏览器打开新标签页并切换
		else if("openUrlInBrower".equals(mbean.getElementDynamicType())) {
			String assertData  = mbean.getElementData().substring(0,1).toUpperCase() + mbean.getElementData().substring(1);
			prt.println("Function.openUrlInBrower(data.get" + assertData + "());");
		}
		// 切换到下一个文本框
		else if(nextElement.equals(mbean.getElementDynamicType())) {
			prt.println("WebElement element" + number + " = Function.switchToNextElement(element" + (number - 1) + ");");
		}
		// 切换到固定文本框
		else if(fixedElement.equals(mbean.getElementDynamicType())) {
			String ele = "Function.findElement(By.xpath(rtn.get("+mbean.getElementData()+").getElementContent()))";
			prt.println("WebElement element" + number + " = Function.switchToNextElement(" + ele + ");");
		}
		// 切换到固定文本框
		else if(intervalElement.equals(mbean.getElementDynamicType())) {
			int step = 1;
			if(mbean.getElementData() != null && !"".equals(mbean.getElementData())) {
				step = step + Integer.valueOf(mbean.getElementData());
			}
			prt.println("WebElement element" + number + " = Function.switchToNextElement(element" + (number - step) + ");");
		}
		// 针对上一步文本框进行输入
		else if(preElement.equals(mbean.getElementDynamicType())) {
			String assertData  = mbean.getElementData().substring(0,1).toUpperCase()+mbean.getElementData().substring(1);
			prt.println("WebElement element" + number + " = Function.inputElement(element" + (number - 1) + ", data.get" + assertData + "());");
		}
		// 输入后点击回车
		else if(click_enter.equals(mbean.getElementDynamicType())) {
			prt.println("Function.clickByEnter(" + elementContent + ");");
		}
		//切换到新标签页面
		else if(switchToNextPage.equals(mbean.getElementDynamicType())) {
			prt.println("Function.switchToNextPage();");
		}
		else if(switchToPrePage.equals(mbean.getElementDynamicType())) {
			prt.println("Function.switchToPrePage();");
		}
		else if(switchToFixedPage.equals(mbean.getElementDynamicType())) {
			prt.println("Function.switchToFixedPage(rtn.get(" + number + ").getExeTimes()");
		}
		//循环上滑半屏点击
		else if(whileClick.equals(mbean.getElementDynamicType())){
			prt.println("Function.whileClick("+elementContent+");");
		}
		//循环等待点击
		else if(whileClick2.equals(mbean.getElementDynamicType())){
			prt.println("Function.whileClick2("+elementContent+");");
		}
		//坐标点击
		else if(coordClick.equals(mbean.getElementDynamicType())){
			String coord = mbean.getExeTimes();
			String[] coords = coord.split(",");
			prt.println("Function.coordClick("+coords[0]+","+coords[1]+");");
		}
		//等待点击（录入等待时间）
		else if(wait_Click.equals(mbean.getElementDynamicType())){
			prt.println("Function.waitClick("+elementContent+","+mbean.getExeTimes()+");");
		}
		//清空文本
		else if(clear_text.equals(mbean.getElementDynamicType())){
			prt.println("Function.clearText(" + elementContent + ", " + mbean.getExeTimes() + ");");
		}
		//输入
		else if(input.equals(mbean.getElementDynamicType())){
			String assertData  = mbean.getElementData().substring(0,1).toUpperCase()+mbean.getElementData().substring(1);
			prt.println("Function.inputText(" + elementContent + ", data.get" + assertData + "());");
		}
		//等待输入
		else if(waitinput.equals(mbean.getElementDynamicType())){
			String assertData  = mbean.getElementData().substring(0,1).toUpperCase()+mbean.getElementData().substring(1);
			prt.println("Function.waitInput("+elementContent+",data.get"+assertData+"());");
		}
		//左滑动
		else if(left_slide.equals(mbean.getElementDynamicType())){
			prt.println("Function.slidingPageLeft(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//上滑动
		else if(up_slide.equals(mbean.getElementDynamicType())){
			prt.println("Function.slidingPageUp(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//下滑动
		else if(up_slide_half.equals(mbean.getElementDynamicType())){
			prt.println("Function.slidingPageUpHalf(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//断言-元素是否存在
		else if(assertExists.equals(mbean.getElementDynamicType())){
			prt.println("Function.assertEquals(wait("+elementContent+"),data);");
		}
		//断言-元素比较desc
		else if(assertDescEquals.equals(mbean.getElementDynamicType())){
			prt.println("String result"+number+" = desc("+elementContent+");");
			String assertData  = mbean.getAssertId().substring(0,1).toUpperCase()+mbean.getAssertId().substring(1);
			prt.println("Assert.assertEquals(desc("+elementContent+"),data.get"+assertData+"());");
			prt.println("Function.print(data.get"+assertData+"(),result"+number+",\"成功\");");
		}
		//断言-元素比较text
		else if(assertTextEquals.equals(mbean.getElementDynamicType())){
			prt.println("String result"+number+" = text("+elementContent+");");
			String assertData  = mbean.getAssertId().substring(0,1).toUpperCase()+mbean.getAssertId().substring(1);
			prt.println("Assert.assertEquals(text("+elementContent+"),data.get"+assertData+"());");
			prt.println("Function.print(data.get"+assertData+"(),result"+number+",\"成功\");");
		}
		//断言-元素包含存在
		else if(assertContains.equals(mbean.getElementDynamicType())){
			prt.println("String result"+number+" = text("+elementContent+");");
			String assertData  = mbean.getAssertId().substring(0,1).toUpperCase()+mbean.getAssertId().substring(1);
			prt.println("if(text("+elementContent+").contains(data.get"+assertData+"())){");
			prt.println("Function.print(data.get"+assertData+"(),result"+number+",\"成功\");");
			prt.println("}");
		}
		//等待元素
		else if(wait.equals(mbean.getElementDynamicType())){
			prt.println("Function.wait("+elementContent+");");
		}
		//等待元素自定义时间
		else if(wait_element_time.equals(mbean.getElementDynamicType())){
			prt.println("Function.wait("+elementContent+",Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//安全键盘输入
		else if(safeInput.equals(mbean.getElementDynamicType())){
			String assertData  = mbean.getElementData().substring(0,1).toUpperCase()+mbean.getElementData().substring(1);
			prt.println("safeInput(data.get"+assertData+"());");
		}
		//循环下滑获取数据desc
		else if(whileFindElementDesc.equals(mbean.getElementDynamicType())){
			prt.println("whileFindElementDesc("+elementContent+");");
		}
		//循环下滑获取数据desc
		else if(whileFindElementText.equals(mbean.getElementDynamicType())){
			prt.println("whileFindElementText("+elementContent+");");
		}
		//元素不存在
		else if(isExist.equals(mbean.getElementDynamicType())){
			prt.println("if(Function.isExist("+elementContent+")){");
			prt.println("Reporter.log(\"预期结果：成功\");");
			prt.println("}");
		}
		//下滑动
		else if(down_slide.equals(mbean.getElementDynamicType())){
			prt.println("Function.downSlidePage(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//弹框处理
		else if(existClick.equals(mbean.getElementDynamicType())){
			prt.println("Function.existClick("+elementContent+");");
		}
		//强制等待
		else if(sleep.equals(mbean.getElementDynamicType())){
			prt.println("Thread.sleep(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		//向上短滑屏
		else if(slidingPageUpMpaas.equals(mbean.getElementDynamicType())){
			prt.println("Function.slidingPageUpMpaas(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
		
		//滚动到底部
		else if(scrollTobutton.equals(mbean.getElementDynamicType())){
			prt.println("Function.scrollTobutton();");
		}
		
		//滚动到顶部
		else if(scrollToTop.equals(mbean.getElementDynamicType())){
			prt.println("Function.scrollToTop();");
		}
		//tab键切换
		else if(switchToTab.equals(mbean.getElementDynamicType())){
			prt.println("Function.switchToTab("+elementContent+");");
		}
		//强制等待
		else if(Forcewait.equals(mbean.getElementDynamicType())){
			prt.println("Thread.sleep(Integer.parseInt(rtn.get("+number+").getExeTimes()));");
		}
    }
    /**
     * 案例执行方法生成写入
     * @param path
     * @param rootpath
     * @param name
     * @throws FileNotFoundException 
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    public static List<String> readJavaTest(ModuleBean bean,String path,String rootpath,String name,String menuNubmer) throws FileNotFoundException, ClassNotFoundException, SQLException{
    	String paths = path.replace("/", ".");
    	List<String> moduleList = new ArrayList<>();
    	//String moduleGroups = moduleGroup+"Module";
		PrintStream prt = new PrintStream(new FileOutputStream(new File(rootpath,name)));
		//导包
		prt.println("package " + paths + ";");
		prt.println("import java.sql.SQLException;");
		prt.println("import org.openqa.selenium.By;");	
		prt.println("import org.testng.Assert;");
		prt.println("import com.auto.common.Function;");
		prt.println("import org.testng.annotations.AfterMethod;");
		prt.println("import org.testng.annotations.BeforeMethod;");
		prt.println("import org.testng.annotations.DataProvider;");
		prt.println("import org.testng.annotations.Test;");
		prt.println("import java.util.Iterator;");
		prt.println("import com.auto.common.CommonFunction;");
		prt.println("import java.util.ArrayList;");
		prt.println("import com.auto.bean.ModuleBean;");
		prt.println("import com.auto.bean.CaseDataBean;");
		prt.println("import com.auto.common.CommonModuleSql;");
		prt.println("import com.auto.module." + bean.getMenuNo() + "Module;");
    	//查询案例组件所有分组
		ArrayList<ModuleBean> mlst =  getModuleCaseData(CommonModuleSql.testcaseGroupSql(bean.getTestcaseNo()));
			for (int j = 0; j < mlst.size(); j++) {
				ModuleBean mbean= (ModuleBean) mlst.get(j);
				if(!bean.getMenuNo().equals(mbean.getModuleGroup())){
					prt.println("import com.auto.module." + mbean.getModuleGroup() + "Module;");
					moduleList.add(mbean.getModuleGroup());
				}
		}
    	//初始化0
		prt.println("/**");
		prt.println(" * " + bean.getTestcaseName());
		prt.println(" * @author baimy");
		prt.println(" *");
		prt.println(" */");
		prt.println("public class " + bean.getMenuNo() + bean.getTestcaseNo() + " extends " 
				+ bean.getMenuNo() + "Module {");
		prt.println("@BeforeMethod");
		prt.println("public void beforeMethod() {");
		prt.println("}");
		//退出
		prt.println("/**");
		prt.println("* 退出");
		prt.println("*/");
		prt.println("@AfterMethod");
		prt.println("public void afterMethod() {");
		prt.println("}");
		//获取案例数据
		prt.println("@DataProvider(name = \"" + bean.getMenuNo() + bean.getTestcaseNo() + "case\")");
		prt.println("public Iterator<Object[]> get" + bean.getMenuNo() + bean.getTestcaseNo() + "caseData() throws ClassNotFoundException, SQLException {");
		prt.print("return CommonFunction.getCaseData(CommonModuleSql.testcaseDetailSql(\"");
		prt.print(bean.getTestcaseNo() + "\"));");
		prt.println("}");
		prt.println("//失败重试 retryAnalyzer = FailRetry.class");
		prt.println("@Test(dataProvider = \"" + bean.getMenuNo() + bean.getTestcaseNo() + "case\")");
		prt.println("public void " + bean.getMenuNo() + bean.getTestcaseNo() + "case(CaseDataBean data) throws Exception{");
		prt.println("//根据案例编号所绑定用户进行登录");
		prt.println("__init__(data);");
		prt.print("print(data.getMenuName(), data.getTestcaseNo(), data.getTestcaseName(), data.getTestcaseDesc());");
//		prt.println(" try {");
		//循环写入案例的组件
		ArrayList<ModuleBean> lst = getModuleCaseData(CommonModuleSql.testcaseModuleSql(bean.getTestcaseNo()));
		int flag = 0;
		for (int j = 0; j < lst.size(); j++) {
			ModuleBean mbean= (ModuleBean) lst.get(j);
			//写入案例的组件
			if(mbean.getModuleGroup() != null && !"".equals(mbean.getModuleGroup())){
				prt.println("//组件 ：" + mbean.getModuleNameCn());
				//写入组件
				if(mbean.getModuleGroup().equals(bean.getMenuNo())){
					prt.println(mbean.getModuleName() + "(\"" + mbean.getModuleName() + "\",data);");
				}else{
					prt.println(mbean.getModuleGroup() + "Module." + mbean.getModuleName() + "(\"" + mbean.getModuleName() + "\",data);");
				}
			}else{
				//写入案例的步骤
				if(flag == 0) {
					prt.println("ArrayList<ModuleBean> rtn = CommonFunction.getModuleCaseData(CommonModuleSql.caseElementsql(\"" + bean.getTestcaseNo() + "\"));");	
				}
				if(mbean.getElementId() != null && !"".equals(mbean.getElementId())) {
					//查询组件元素详细信息
					ArrayList<ModuleBean> lstm =  getModuleCaseData(CommonModuleSql.caseDetailSql(mbean.getStepNo(), mbean.getTestcaseNo()));
					for (int i = 0; i < lstm.size(); i++) {
						//prt.println("ArrayList<ModuleBean> rtn"+flag+" =  getModuleCaseData(CommonModuleSql.caseDetailSql(mbean.getStepNo(),mbean.getTestcaseNo()));");
						ModuleBean lstmbean= (ModuleBean) lstm.get(i);
						readModuleMethod(lstmbean, prt, flag);
					}
				} else {
					ArrayList<ModuleBean> lstm =  getModuleCaseData(CommonModuleSql.caseDetailSql2(mbean.getStepNo(), mbean.getTestcaseNo()));
					for (int i = 0; i < lstm.size(); i++) {
						//prt.println("ArrayList<ModuleBean> rtn"+flag+" =  getModuleCaseData(CommonModuleSql.caseDetailSql2(mbean.getStepNo(),mbean.getTestcaseNo()));");
						ModuleBean lstmbean= (ModuleBean) lstm.get(i);
						readModuleMethod(lstmbean, prt, flag);
					}
				}
				flag = flag+1;
			}
		}
		
//		prt.println("} catch (Exception e) {");
//		prt.println("getScreenShot(data.getMenuName(), data.getTestcaseNo(), \"操作失败\");");
//		prt.println("}");
		prt.println("//截图");
		prt.println("getScreenShot(data.getMenuName(), data.getTestcaseNo());");
		
		prt.println("}");
		prt.println("}");
		prt.close();
		return moduleList;
		
    }
    /**
     * 写入xml
     * @param bean
     * @param prt
     * @param xpath
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void readXmlTest(ModuleBean bean,PrintStream prt,String xpath) throws FileNotFoundException, ClassNotFoundException, SQLException{
    	String path = xpath.replace("/", ".");

    	prt.println("<!--" + bean.getTestcaseName() + "-->");
    	prt.println("<test verbose=\"10\" name=\"" + bean.getTestcaseName() + "\" preserve-order=\"true\">");
		prt.println("<classes>");
		prt.println("<class name=\"" + path + "\" />");
		prt.println("</classes>");
		prt.println("<listeners>");
		prt.println("<listener class-name=\"com.auto.common.TestngListener\" />");
		prt.println("<listener class-name=\"org.uncommons.reportng.HTMLReporter\" />");
		prt.println("<listener class-name=\"org.uncommons.reportng.JUnitXMLReporter\" />");
		prt.println("</listeners>");
		prt.println("</test>");
		
    }
    
    /**
	 * 组件数据转换
	 * @param sql
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
    public static Iterator<Object[]> getCaseData(String sql) throws ClassNotFoundException, SQLException {
    	System.out.println("sql:==="+sql);
        ResultSet result = getNewTestData(sql);
      //  ArrayList<Object> returnTobe = new ArrayList<>();
        List<Object[]> returnTobe = new ArrayList<Object[]>();
        while (result.next()) {
        	CaseDataBean bean = new CaseDataBean();
        	
        	if(isExistColumn(result,"testcase_no")){
        		bean.setTestcaseNo(result.getString("testcase_no"));
        	}
        	if(isExistColumn(result,"func_point")){
        		bean.setFuncPoint(result.getString("func_point"));
        	}
        	if(isExistColumn(result,"testcase_name")){
        		bean.setTestcaseName(result.getString("testcase_name"));
        	}
        	if(isExistColumn(result,"precondition")){
        		bean.setPrecondition(result.getString("precondition"));
        	}
        	if(isExistColumn(result,"testcase_desc")){
        		bean.setTestcaseDesc(result.getString("testcase_desc"));
        	}
        	if(isExistColumn(result,"expected_result")){
        		bean.setExpectedResult(result.getString("expected_result"));
        	}
        	if(isExistColumn(result,"username")){
        		bean.setUsername(result.getString("username"));
        	}
        	if(isExistColumn(result,"queryPwd")){
        		bean.setQueryPwd(result.getString("queryPwd"));
        	}
        	if(isExistColumn(result,"menu_name")){
        		bean.setMenuName(result.getString("menu_name"));
        	}
        	if(isExistColumn(result,"element_content")){
        		bean.setElementContent(result.getString("element_content"));
        	}
        	if(isExistColumn(result,"element_data")){
        		bean.setElementData(result.getString("element_data"));
        	}
        	if(isExistColumn(result,"exe_times")){
        		bean.setExeTimes(result.getString("exe_times"));
        	}
        	
        	if(isExistColumn(result,"module_no")){
        		bean.setModuleNo(result.getString("module_no"));
        	}
        	if(isExistColumn(result,"module_name")){
        		bean.setModuleName(result.getString("module_name"));
        	}
        	if(isExistColumn(result,"element_content")){
        		bean.setElementContent(result.getString("element_content"));
        	}
        	if(isExistColumn(result,"element_name")){
        		bean.setElementName(result.getString("element_name"));
        	}
        	if(isExistColumn(result,"element_Dir")){
        		bean.setElementDir(result.getString("element_Dir"));
        	}
        	if(isExistColumn(result,"element_dynamic_type")){
        		bean.setElementDynamicType(result.getString("element_dynamic_type"));
        	}
        	if(isExistColumn(result,"element_data")){
        		bean.setElementData(result.getString("element_data"));
        	}
        	if(isExistColumn(result,"exe_times")){
        		bean.setExeTimes(result.getString("exe_times"));
        	}
        	if(isExistColumn(result,"assertData1")){
        		bean.setAssertData1(result.getString("assertData1"));
        	}
        	if(isExistColumn(result,"assertData2")){
        		bean.setAssertData2(result.getString("assertData2"));
        	}
        	if(isExistColumn(result,"assertData3")){
        		bean.setAssertData3(result.getString("assertData3"));
        	}
        	if(isExistColumn(result,"assertData4")){
        		bean.setAssertData4(result.getString("assertData4"));
        	}
        	if(isExistColumn(result,"assertData5")){
        		bean.setAssertData5(result.getString("assertData5"));
        	}
        	if(isExistColumn(result,"assertData6")){
        		bean.setAssertData6(result.getString("assertData6"));
        	}
        	if(isExistColumn(result,"assertData7")){
        		bean.setAssertData7(result.getString("assertData7"));
        	}
        	if(isExistColumn(result,"assertData8")){
        		bean.setAssertData8(result.getString("assertData8"));
        	}
        	if(isExistColumn(result,"assertData9")){
        		bean.setAssertData9(result.getString("assertData9"));
        	}
        	if(isExistColumn(result,"assertData10")){
        		bean.setAssertData10(result.getString("assertData10"));
        	}
        	//case1-10
        	if(isExistColumn(result, "case1")) {
        		bean.setCase1(result.getString("case1"));
        	}
        	if(isExistColumn(result, "case2")) {
        		bean.setCase2(result.getString("case2"));
        	}
        	if(isExistColumn(result,"case3")){
        		bean.setCase3(result.getString("case3"));
        	}
        	if(isExistColumn(result, "case4")) {
        		bean.setCase4(result.getString("case4"));
        	}
        	if(isExistColumn(result, "case5")) {
        		bean.setCase5(result.getString("case5"));
        	}
        	if(isExistColumn(result,"case6")){
        		bean.setCase6(result.getString("case6"));
        	}
        	if(isExistColumn(result, "case7")) {
        		bean.setCase7(result.getString("case7"));
        	}
        	if(isExistColumn(result, "case8")) {
        		bean.setCase8(result.getString("case8"));
        	}
        	if(isExistColumn(result,"case9")){
        		bean.setCase9(result.getString("case9"));
        	}
        	if(isExistColumn(result, "case10")) {
        		bean.setCase10(result.getString("case10"));
        	}
        	//case11-20
        	if(isExistColumn(result, "case11")) {
        		bean.setCase11(result.getString("case11"));
        	}
        	if(isExistColumn(result, "case12")) {
        		bean.setCase12(result.getString("case12"));
        	}
        	if(isExistColumn(result,"case13")){
        		bean.setCase13(result.getString("case13"));
        	}
        	if(isExistColumn(result, "case14")) {
        		bean.setCase14(result.getString("case14"));
        	}
        	if(isExistColumn(result, "case15")) {
        		bean.setCase15(result.getString("case15"));
        	}
        	if(isExistColumn(result,"case16")){
        		bean.setCase16(result.getString("case16"));
        	}
        	if(isExistColumn(result, "case17")) {
        		bean.setCase17(result.getString("case17"));
        	}
        	if(isExistColumn(result, "case18")) {
        		bean.setCase18(result.getString("case18"));
        	}
        	if(isExistColumn(result,"case19")){
        		bean.setCase19(result.getString("case19"));
        	}
        	if(isExistColumn(result, "case20")) {
        		bean.setCase20(result.getString("case20"));
        	}
        	
        	//case21-30
        	if(isExistColumn(result, "case21")) {
        		bean.setCase21(result.getString("case21"));
        	}
        	if(isExistColumn(result, "case22")) {
        		bean.setCase22(result.getString("case22"));
        	}
        	if(isExistColumn(result,"case23")){
        		bean.setCase23(result.getString("case23"));
        	}
        	if(isExistColumn(result, "case24")) {
        		bean.setCase24(result.getString("case24"));
        	}
        	if(isExistColumn(result, "case25")) {
        		bean.setCase25(result.getString("case25"));
        	}
        	if(isExistColumn(result,"case26")){
        		bean.setCase26(result.getString("case26"));
        	}
        	if(isExistColumn(result, "case27")) {
        		bean.setCase27(result.getString("case27"));
        	}
        	if(isExistColumn(result, "case28")) {
        		bean.setCase28(result.getString("case28"));
        	}
        	if(isExistColumn(result,"case29")){
        		bean.setCase29(result.getString("case29"));
        	}
        	if(isExistColumn(result, "case30")) {
        		bean.setCase30(result.getString("case30"));
        	}
        	
        	//case31-40
        	if(isExistColumn(result, "case31")) {
        		bean.setCase31(result.getString("case31"));
        	}
        	if(isExistColumn(result, "case32")) {
        		bean.setCase32(result.getString("case32"));
        	}
        	if(isExistColumn(result,"case33")){
        		bean.setCase33(result.getString("case33"));
        	}
        	if(isExistColumn(result, "case34")) {
        		bean.setCase34(result.getString("case34"));
        	}
        	if(isExistColumn(result, "case35")) {
        		bean.setCase35(result.getString("case35"));
        	}
        	if(isExistColumn(result,"case36")){
        		bean.setCase36(result.getString("case36"));
        	}
        	if(isExistColumn(result, "case37")) {
        		bean.setCase37(result.getString("case37"));
        	}
        	if(isExistColumn(result, "case38")) {
        		bean.setCase38(result.getString("case38"));
        	}
        	if(isExistColumn(result,"case39")){
        		bean.setCase39(result.getString("case39"));
        	}
        	if(isExistColumn(result, "case40")) {
        		bean.setCase40(result.getString("case40"));
        	}
        	
           	//case41-50
        	if(isExistColumn(result, "case41")) {
        		bean.setCase41(result.getString("case41"));
        	}
        	if(isExistColumn(result, "case42")) {
        		bean.setCase42(result.getString("case42"));
        	}
        	if(isExistColumn(result,"case43")){
        		bean.setCase43(result.getString("case43"));
        	}
        	if(isExistColumn(result, "case44")) {
        		bean.setCase44(result.getString("case44"));
        	}
        	if(isExistColumn(result, "case45")) {
        		bean.setCase45(result.getString("case45"));
        	}
        	if(isExistColumn(result,"case46")){
        		bean.setCase46(result.getString("case46"));
        	}
        	if(isExistColumn(result, "case47")) {
        		bean.setCase47(result.getString("case47"));
        	}
        	if(isExistColumn(result, "case48")) {
        		bean.setCase48(result.getString("case48"));
        	}
        	if(isExistColumn(result,"case49")){
        		bean.setCase39(result.getString("case49"));
        	}
        	if(isExistColumn(result, "case50")) {
        		bean.setCase50(result.getString("case50"));
        	}
        	
        	// returnTobe.add(bean);
        	returnTobe.add(new Object[]{bean});
        }
        return returnTobe.iterator();
    	
    }
    public static ArrayList<ModuleBean> getModuleCaseData(String sql) throws ClassNotFoundException, SQLException {
    	System.out.println("sql:==="+sql);
        ResultSet result = getNewTestData(sql);
        ArrayList<ModuleBean> returnTobe = new ArrayList<>();
        while (result.next()) {
        	ModuleBean bean = new ModuleBean();
        	
        	if(isExistColumn(result,"module_id")){
        		bean.setModuleId(result.getString("module_id"));
        	}
        	if(isExistColumn(result,"module_no")){
        		bean.setModuleNo(result.getString("module_no"));
        	}
        	if(isExistColumn(result,"module_name")){
        		bean.setModuleName(result.getString("module_name"));
        	}
        	if(isExistColumn(result,"module_group")){
        		bean.setModuleGroup(result.getString("module_group"));
        	}
        	if(isExistColumn(result,"module_name_cn")){
        		bean.setModuleNameCn(result.getString("module_name_cn"));
        	}
        	if(isExistColumn(result,"step_no")){
        		bean.setStepNo(result.getString("step_no"));
        	}
        	if(isExistColumn(result,"element_id")){
        		bean.setElementId(result.getString("element_id"));
        	}
        	if(isExistColumn(result,"element_data")){
        		bean.setElementData(result.getString("element_data"));
        	}
        	if(isExistColumn(result,"element_dynamic_type")){
        		bean.setElementDynamicType(result.getString("element_dynamic_type"));
        	}
        	if(isExistColumn(result,"element_type")){
        		bean.setElementType(result.getString("element_type"));
        	}
        	if(isExistColumn(result,"element_content")){
        		bean.setElementContent(result.getString("element_content"));
        	}
        	if(isExistColumn(result,"element_name")){
        		bean.setElementName(result.getString("element_name"));
        	}
        	if(isExistColumn(result,"element_dir")){
        		bean.setElementDir(result.getString("element_dir"));
        	}
        	if(isExistColumn(result,"exe_times")){
        		bean.setExeTimes(result.getString("exe_times"));
        	}
        	if(isExistColumn(result,"element_step_no")){
        		bean.setElementStepNo(result.getString("element_step_no"));
        	}
        	
        	if(isExistColumn(result,"testcase_no")){
        		bean.setTestcaseNo(result.getString("testcase_no"));
        	}
        	if(isExistColumn(result,"func_point")){
        		bean.setFuncPoint(result.getString("func_point"));
        	}
        	if(isExistColumn(result,"testcase_name")){
        		bean.setTestcaseName(result.getString("testcase_name"));
        	}
        	if(isExistColumn(result,"precondition")){
        		bean.setPrecondition(result.getString("precondition"));
        	}
        	if(isExistColumn(result,"testcase_desc")){
        		bean.setTestcaseDesc(result.getString("testcase_desc"));
        	}
        	if(isExistColumn(result,"expected_result")){
        		bean.setExpectedResult(result.getString("expected_result"));
        	}
        	
        	if(isExistColumn(result,"menu_no")){
        		bean.setMenuNo(result.getString("menu_no"));
        	}
        	if(isExistColumn(result,"meun_name")){
        		bean.setMeunName(result.getString("meun_name"));
        	}
        	if(isExistColumn(result,"secondary_menu")){
        		bean.setSecondaryMenu(result.getString("secondary_menu"));
        	}
        	
        	if(isExistColumn(result,"username")){
        		bean.setUsername(result.getString("username"));
        	}
        	if(isExistColumn(result,"queryPwd")){
        		bean.setQueryPwd(result.getString("queryPwd"));
        	}
        	if(isExistColumn(result,"transPwd")){
        		bean.setTransPwd(result.getString("transPwd"));
        	}
        	
        	if(isExistColumn(result,"productId")){
        		bean.setProductId(result.getString("productId"));
        	}
        	if(isExistColumn(result,"amount")){
        		bean.setAmount(result.getString("amount"));
        	}
        	if(isExistColumn(result,"rate")){
        		bean.setRate(result.getString("rate"));
        	}
        	if(isExistColumn(result,"productType")){
        		bean.setProductType(result.getString("productType"));
        	}
        	
        	if(isExistColumn(result,"assert_id")){
        		bean.setAssertId(result.getString("assert_id"));
        	}
        	
            returnTobe.add(bean);
        }
        return returnTobe;
    }
    
}
