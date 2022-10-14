package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ICommonDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelTemplateDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author: lisy26
 * @date: 2020/11/16 10:59
 * @ClassName ModelTemplateServiceImpl
 * @Description
 **/
@Service
public class ModelTemplateServiceImpl implements IModelTemplateService {

    @Autowired
    private IModelTemplateDao modelTemplateDao;

    @Autowired
    private ServiceUtil serviceUtil;

    @Autowired
    private ICommonDao commonDao;

    @Override
    public ModelTemplate add(ModelTemplate modelTemplate) throws Exception {
        //校验命名规范
        String nameEn = modelTemplate.getNameEn();
        boolean flag;
        String errorMsg;
        flag = Pattern.matches(Constants.PATTERN_MODEL_TEMPLATE_NAME_EN,nameEn);
        errorMsg = "只能是英文和数字和_组成";
        if (!flag){
            errorMsg = nameEn + errorMsg;
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
        }
        String nameCn = modelTemplate.getNameCn();
        flag = Pattern.matches(Constants.PATTERN_MODEL_TEMPLATE_NAME_CN,nameCn);
        errorMsg = "只能是英文、中文、数字和_组成";
        if (!flag){
            errorMsg = nameCn + errorMsg;
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
        }
        //判断英文名、中文名是否重复
        CommonValidate.validateRepeatParam(modelTemplate, Constants.OR, new String[]{Dict.NAMEEN,Dict.NAMECN}, ModelTemplate.class, this.commonDao);
        //判断属性在模板内是否重复
        CommonValidate.validateTemplateRepeatParam(modelTemplate.getEnvKey(),Dict.PROPKEY);
        CommonValidate.validateTemplateRepeatParam(modelTemplate.getEnvKey(),Dict.PROPNAMECN);

        modelTemplate.setDesc(modelTemplate.getDesc());
        //处理实体模板属性
        List<Object> envKey = modelTemplate.getEnvKey();
        modelTemplate.setEnvKey(getModelTemplateAttribute(envKey));
        return this.modelTemplateDao.add(modelTemplate);
    }

    @Override
    public Map pageQuery(Map map) throws Exception {
        return this.modelTemplateDao.pageQuery(map);
    }

    @Override
    public ModelTemplate queryById(String id) throws Exception {
        return modelTemplateDao.queryById(id);
    }


    /**
     * 处理实体模板属性
     *
     * @param envKey
     */
    private List<Object> getModelTemplateAttribute(List<Object> envKey) {
        List<Object> modelTemplateAttributeList = new ArrayList<>();
        for (Object envKeyObj : envKey) {
            Map<String, Object> map = (Map<String, Object>) envKeyObj;
            Map<String, Object> modelTemplateAttributeMap = new HashMap<>();
            modelTemplateAttributeMap.put(Dict.PROP_KEY, map.get(Dict.PROPKEY));
            modelTemplateAttributeMap.put(Dict.PROP_NAME_CN, map.get(Dict.PROPNAMECN));
            modelTemplateAttributeMap.put(Dict.DESC, map.get(Dict.DESC));
            modelTemplateAttributeMap.put(Dict.DATA_TYPE, map.get(Dict.DATATYPE));
            modelTemplateAttributeList.add(modelTemplateAttributeMap);
        }
        return modelTemplateAttributeList;
    }

	@Override
    public ModelTemplate queryByNameEn(String nameEn) throws Exception {
        return modelTemplateDao.queryByNameEn(nameEn);
    }

}
