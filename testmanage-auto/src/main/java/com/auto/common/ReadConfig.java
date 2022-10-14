package com.auto.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ReadConfig {
	
	public static String userName = null;
    public static String passWord = null;
    public static String dbDriver = null;
    public static String url = null;
    public static String baffle=null;
    public static String mock_url=null;
    public static String MockDB_url=null;
    public static String dbflag=null;
    
    public static String db_username = null;
    public static String dbper_pwd = null;
    public static String dbper_driver = null;
    public static String dbper_url = null;
    
    public static String moduleGroup = null;
    public static String menuNubmer  = null;
    public static String testCaseNo = null;
    public static String caseName = null;
    
    public static String fdev_url = null;
	public static String browser = null;
    
    static {

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(new File("src/main/resources/dbcon.properties")))){
            ResourceBundle resource = new PropertyResourceBundle(inputStream);
            userName = resource.getString("Autotest_Username");
            passWord = resource.getString("Autotest_Pwd");
            dbDriver = resource.getString("Autotest_Driver");
            url = resource.getString("Autotest_url");
            MockDB_url=resource.getString("MockDB_url");
            baffle=resource.getString("ebank.per.baffle.openflag");
            dbflag=resource.getString("ebank.per.db.flag");
            
            db_username = resource.getString("Dbper_Username");
            dbper_pwd = resource.getString("Dbper_Pwd");
            dbper_driver = resource.getString("Dbper_Driver");
            dbper_url = resource.getString("Dbper_Url");
            
            moduleGroup = resource.getString("moduleGroup");
            menuNubmer = resource.getString("menuNubmer");
            testCaseNo = resource.getString("testCaseNo");
            caseName = resource.getString("caseName");
            
            fdev_url = resource.getString("fdev.url");
            browser = resource.getString("fdev.browser");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
     
    }


public static void main(String args[]){
	System.out.print(dbflag);
}
    


}
