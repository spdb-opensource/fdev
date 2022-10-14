package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.service.ISccDeploymentService;
import com.spdb.fdev.spdb.service.ISccModifyKeyService;
import com.spdb.fdev.spdb.service.ISccYamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * @Author:guanz2
 * @Date:2021/10/5-13:48
 * @Description: Scc 生成yaml 的实现类
 */
@Component
public class SccYamlServiceImpl implements ISccYamlService {

    @Autowired
    private ISccModifyKeyService sccModifyKeyService;

    @Autowired
    private ISccDeploymentService sccDeployService;

    @Value("${scc_old_yaml_path}")
    private String path;

    @Override
    public List<LinkedHashMap<String, Object>> getSccOldYaml(String resource_code) throws Exception {
        List<LinkedHashMap<String, Object>> data = sccDeployService.getSccDeployCondition(resource_code);
        if (CommonUtils.isNullOrEmpty(data)) {
            throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
        }
        for(int i = 0; i < data.size(); i++ ){
            //生成文件
            this.generageFile(path, (String) data.get(i).get("namespace_code"), (String) data.get(i).get("resource_code"), (LinkedHashMap) data.get(i).get("yaml"));
        }
        return data;
    }

    @Override
    public List<LinkedHashMap<String, Object>> getSccNewYaml(String resource_code, String tag) throws Exception {
        List<LinkedHashMap<String, Object>> modifyData = sccModifyKeyService.queryModifyKeys(resource_code);
        List<LinkedHashMap<String, Object>> oldData = sccDeployService.getSccDeployCondition(resource_code);
        List<LinkedHashMap<String, Object>>  data = new ArrayList<>();

        //scc_deployment一定要先有对应的应用数据。这个应用先要投产过
        if(CommonUtils.isNullOrEmpty(oldData)){
            throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
        }

        if (CommonUtils.isNullOrEmpty(modifyData)) {
            // 没有修改过，就取旧的
            data = oldData;
        } else{
            Set namespace_rescource = new HashSet();
            for(int i = 0; i < oldData.size(); i++){
                String keySet = oldData.get(i).get("namespace_code")+"###"+oldData.get(i).get("resource_code");
                namespace_rescource.add(keySet);
            }
            LinkedHashMap change = new LinkedHashMap();
            for( int j = 0; j < modifyData.size(); j++){
                String  KEY = modifyData.get(j).get("namespace_code")+"###"+modifyData.get(j).get("resource_code");
                if(namespace_rescource.contains(KEY)){
                    change.put(KEY, modifyData.get(j));
                }
            }
            for(int k = 0; k < oldData.size(); k++){
                String keySet = oldData.get(k).get("namespace_code")+"###"+oldData.get(k).get("resource_code");
                if(change.containsKey(keySet)){
                    oldData.set(k, (LinkedHashMap<String, Object>) change.get(keySet));
                }
            }
            data = oldData;
        }
        for(int i = 0; i < data.size() ; i++){
            LinkedHashMap temp = (LinkedHashMap) data.get(i).get("yaml");
            if(temp.get("kind").equals("Deployment")){
                //对tag处理 对版本单独判断是否相等,imagVersion
                List containers = (List) ((Map)((Map)((Map)temp.get("spec")).get("template")).get("spec")).get("containers");
                if(!CommonUtils.isNullOrEmpty(containers)){
                    for (int j = 0 ; j < containers.size() ; j++){
                        String TAG = (String) ((Map)containers.get(j)).get("image");
                        String[] tagList = TAG.split(":");
                        if(tagList[tagList.length-1].hashCode() != tag.hashCode()){
                            tagList[tagList.length-1]=tag;
                        }
                        String resultTag="";
                        for (int k = 0; k < tagList.length; k++) {
                            resultTag=resultTag+":"+tagList[k];
                        }
                        ((Map)containers.get(j)).put("image", resultTag.substring(1, resultTag.length()));
                    }
                }
            }
        }
        return data;
    }

    //生成文件
    private void generageFile(String path, String namespace_code, String resource_code, LinkedHashMap data){
        String targetPath = path+"/"+resource_code+"/"+namespace_code+"/"+resource_code+".yaml";
        for(int i = 0; i < targetPath.length() ; i++){
            if(targetPath.charAt(i) == '/'){
                File temp = new File(targetPath.substring(0, i));
                if(!temp.exists()){
                    temp.mkdir();
                }
            }
        }
        File file = new File(targetPath);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        try (FileWriter writer = new FileWriter(file);){
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
