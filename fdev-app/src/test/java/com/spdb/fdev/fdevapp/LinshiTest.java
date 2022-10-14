package com.spdb.fdev.fdevapp;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LinshiTest {

    private String dir = "D:\\dir\\geren-jijin\\7dfgdfg-sdlfjsld\\中文.docx";
    private String dirTxt = "D:\\dir\\geren-jijin\\7dfgdfg-sdlfjsld\\中文1.txt";
    String creatPath = "个人组-基金%2F任务名-5asdfas1112%2F基金-中文%2Etxt";
    private String url = "http://xxx/api/v4/projects/1861/repository/files/";
    @Value("${gitlab.token}")
    private String token;


    @Test
    public void getTime() {
        System.out.println(System.currentTimeMillis());
        SimpleDateFormat sdt =new SimpleDateFormat("yyyy-MM");
        System.out.println(sdt.format(System.currentTimeMillis()));
    }

    @Test
    public void test3() {
        ResourceBundle bundle = ResourceBundle.getBundle("typeMap");
        System.out.println(bundle.getString("9"));
    }


}
