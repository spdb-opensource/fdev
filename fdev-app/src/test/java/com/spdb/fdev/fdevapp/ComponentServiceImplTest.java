package com.spdb.fdev.fdevapp;

import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.IArchetypeService;
import com.spdb.fdev.fdevapp.spdb.service.IComponentService;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ComponentServiceImplTest {

    @Autowired
    private IComponentService componentService;

    @Autowired
    private IArchetypeService archetypeService;

    @Test
    public void test() throws Exception {
        String archetype_id = "5df896521dcf5400150c73cb";
        String localTempProjectPath = "D:/kelan/nbh-jnl-configcode-parent";
        Map archetype = archetypeService.queryArchetype(archetype_id);
        archetype.put(Constant.ENCODING, Constant.GBK);
        if (archetype != null
                && Constant.GBK.equals(archetype.get(Constant.ENCODING))
                && !Constant.VUE.equals(archetype.get(Dict.TYPE))) {
            String content = componentService.queryConfigTemplate(Dict.MASTER, (String) archetype.get(Dict.ID));
            if (StringUtils.isNotBlank(content)) {
                String configPath = localTempProjectPath + File.separator + "gitlab-ci/fdev-application.properties";
                File configFile = new File(configPath);
                if (configFile != null) {
                    CommonUtils.replaceContent(configFile, content);
                } else {
                    System.out.println("应用环境配置模块文件不存在");
                }
            }
        }

    }

}