package com.auto.common;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;


/**
 * 自动生成模块组件文件及组件方法
 * @author baimy
 *
 */
@Service
public class CommonModuleGenerateFile {
//	private static String moduleGroup  = ReadConfig.moduleGroup;
	/**
	 * @param args
	 */
	public static void generatorModule(List<String> moduleList)  {
		try {
			for(int i=0; i<moduleList.size(); i++){
				
				//生成模块组件方法文件
				String rootpath = "src/main/java";
				String path = "com/auto/module";
				
				rootpath = rootpath + "/" + path;
				//规则：组件归属的模块名称+Module
		    	String name = moduleList.get(i) + "Module" + ".java";
		    	File file = new File(rootpath+"/"+name);
		    	if(!file.exists()) {
		    		file.getParentFile().mkdir();
		    		try {
		    	        //创建文件
		    	        file.createNewFile();
		    	    } catch (IOException e) {
		    	        e.printStackTrace();
		    	    }
		    	}
		    	//写入组件方法
		    	CommonFunction.readModuleJavaTest(moduleList.get(i), path, rootpath, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
