package com.auto.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.auto.bean.ModuleBean;

/**
 * 自动生手机成自动化执行方法，xml
 * @author baimy
 *
 */
@Service
public class CommonGeneratorFile extends CommonFunction{
	
	public static List<String> generatorFile(String menuNumber, String testCaseNo, String folder) {

		List<String> retList = new ArrayList<>();
		try {
			//写入XML
			String xmlpath = "src/main/resources/" + folder;
	    	String xmlname = "testng-suite-" + menuNumber + ".xml";
	    	File files = new File(xmlpath + "/" + xmlname);
	    	if(!files.exists()){
	    		files.getParentFile().mkdir();
	    		try {
	    	        //创建文件
	    	        files.createNewFile();
	    	    } catch (IOException e) {
	    	        e.printStackTrace();
	    	    }
	    	}
	    	PrintStream prt = new PrintStream(new FileOutputStream(new File(xmlpath,xmlname)));
			//导包
			prt.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			prt.println("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\">");
			prt.println("<suite name=\"fdev自动化测试_" + menuNumber + "\"   parallel=\"test\" thread-count=\"5\">");
			
			//查询模块下案例（支持按模块生成文件和单条案例生成文件）
			ArrayList<ModuleBean> lst =  getModuleCaseData(CommonModuleSql.testcaseFileSql(testCaseNo, menuNumber));
			for (int i = 0; i < lst.size(); i++) {
				List<String> moduleList = new ArrayList<>();
				ModuleBean bean= (ModuleBean) lst.get(i);
				//写入java执行文件（以案例为维度生成java文件）
				String rootpath = "src/main/java";
				String path = "com/auto/testcase/" + folder;
				rootpath = rootpath + "/" + path;
				//名称规则：模块名称+案例编号
		    	String name = bean.getMenuNo() + bean.getTestcaseNo() + ".java";
		    	
		    	File file = new File(rootpath + "/" + name);
		    	if(!file.exists()){
		    		file.getParentFile().mkdirs();
		    		try {
		    	        //创建文件
		    	        file.createNewFile();
		    	    } catch (IOException e) {
		    	        e.printStackTrace();
		    	    }
		    	}
		    	String xpath = path + "/" + bean.getMenuNo() + bean.getTestcaseNo();
		    	moduleList = CommonFunction.readJavaTest(bean, path, rootpath, name, menuNumber);
		    	CommonFunction.readXmlTest(bean, prt, xpath);
		    	retList.addAll(moduleList);
		    	retList.add(bean.getMenuNo());
			}
			prt.println("</suite>");
			prt.close();	
			retList = (List<String>)(List)retList.stream().distinct().collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retList;
	}

}
