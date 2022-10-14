package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ModelSetDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelSet;
import com.spdb.fdev.fdevenvconfig.spdb.service.ModelSetService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

@Service
public class ModelSetServiceImpl implements ModelSetService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private ModelSetDao modelSetDao;
    @Autowired
    private IModelDao modelDao;

    /**
     * 获取实体组类型
     *
     * @return
     */
    private Map<String, Object> getTemplateMap() {
        Yaml yaml = new Yaml();
        InputStream ymlInputStream = CommonUtils.getTemplateYml("model-template/model-template.yml");
        Map<String, Object> ymlMap = yaml.load(ymlInputStream);
        return (Map<String, Object>) ymlMap.get(Dict.TEMPLATE);
    }

    @Override
    public List<Map<String, Object>> getModelSetTemplate() {
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> templateMap = getTemplateMap();
        for (Map.Entry<String, Object> entry : templateMap.entrySet()) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put(Dict.TEMPLATE, entry.getKey());
            typeMap.put(Dict.SECOND_CATEGORY, entry.getValue());
            responseList.add(typeMap);
        }
        return responseList;
    }

    @Override
    public List<Map<String, Object>> getModles(Map<String, Object> requestMap) {
        String template = (String) requestMap.get(Dict.TEMPLATE);
        if (StringUtils.isEmpty(template)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TEMPLATE, Constants.PARAM_CAN_NOT_NULL});
        }
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> templateMap = getTemplateMap();
        List<String> secondCategoryList = (List<String>) templateMap.get(template);
        if (CollectionUtils.isEmpty(secondCategoryList)) {
            return responseList;
        }
        for (String secondCategory : secondCategoryList) {
            Map<String, Object> secondCategoryMap = new HashMap<>();
            List<Model> modelList = modelDao.getModelBySecondCategory(secondCategory);
            secondCategoryMap.put(Dict.SECOND_CATEGORY, secondCategory);
            secondCategoryMap.put(Dict.MODELS_INFO, modelList);
            responseList.add(secondCategoryMap);
        }
        return responseList;
    }

    @Override
    public void saveModelSet(Map<String, Object> requestMap) throws Exception{
        String nameCn = (String) requestMap.get(Constants.NAME_CN);
        String template = (String) requestMap.get(Dict.TEMPLATE);
        List<String> models = (List<String>) requestMap.get(Dict.MODELS);
        if (StringUtils.isEmpty(nameCn) || StringUtils.isEmpty(template) || CollectionUtils.isEmpty(models)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Constants.NAME_CN + "," + Dict.TEMPLATE + "," + Dict.MODELS, Constants.PARAM_CAN_NOT_NULL});
        }
        // 校验中文名，不能重复
        ModelSet modelSetByName = modelSetDao.getModelSetByName(nameCn);
        if (modelSetByName != null) {
            throw new FdevException(ErrorConstants.INSERT_MODEL_SET_ERROR, new String[]{"实体组中文名不能重复，换一个试试"});
        }
        // 校验是否存在相同的实体组
        ModelSet modelSetByModels = modelSetDao.getModelSetByModels(models);
        if (modelSetByModels != null && models.size() == modelSetByModels.getModels().size()) {
            throw new FdevException(ErrorConstants.INSERT_MODEL_SET_ERROR, new String[]{"实体组已存在，中文名为:" + modelSetByModels.getNameCn()});
        }
        ModelSet modelSet = new ModelSet();
        modelSet.setNameCn(nameCn);
        modelSet.setTemplate(template);
        modelSet.setModels(models);
        modelSet.setOpno(serviceUtil.getOpno());
        modelSetDao.saveModelSet(modelSet);
    }

    @Override
    public Map<String, Object> listModelSetsByPage(Map<String, Object> requestMap) {
        String nameCn = (String) requestMap.get(Constants.NAME_CN);
        String template = (String) requestMap.get(Dict.TEMPLATE);
        String id = (String) requestMap.get(Dict.ID);
        Object pageObj = requestMap.get(Dict.PAGE);
        Object perPageObj = requestMap.get(Dict.PER_PAGE);
        if (pageObj == null || perPageObj == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.PAGE + "," + Dict.PER_PAGE, Constants.PARAM_CAN_NOT_NULL});
        }
        Map<String, Object> modelSetMapByPage = modelSetDao.listModelSetsByPage(nameCn, template, id, (int) pageObj, (int) perPageObj);
        List<ModelSet> modelSetList = (List<ModelSet>) modelSetMapByPage.get(Dict.LIST);
        Map<String, Object> responseMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (ModelSet modelSet : modelSetList) {
            List<String> models = modelSet.getModels();
            List<Model> modelList = modelDao.getByModelIds(models);
            Map<String, Object> modelSetMap;
            try {
                modelSetMap = BeanUtils.describe(modelSet);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
            }
            modelSetMap.put(Dict.MODELS_INFO, modelList);
            list.add(modelSetMap);
        }
        responseMap.put(Dict.TOTAL, modelSetMapByPage.get(Dict.TOTAL));
        responseMap.put(Dict.LIST, list);
        return responseMap;
    }


    @Override
    public void updateModelSet(Map<String, Object> requestMap) throws Exception{
        String id = (String) requestMap.get(Dict.ID);
        List<String> models = (List<String>) requestMap.get(Dict.MODELS);
        if (StringUtils.isEmpty(id) || CollectionUtils.isEmpty(models)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "," + Dict.MODELS, Constants.PARAM_CAN_NOT_NULL});
        }
        modelSetDao.updateModelSet(id, models, serviceUtil.getOpno());
    }

    @Override
    public void deleteModelSet(Map<String, Object> requestMap) throws Exception{
        String id = (String) requestMap.get(Dict.ID);
        if (StringUtils.isEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, Constants.PARAM_CAN_NOT_NULL});
        }
        modelSetDao.deleteModelSet(id, serviceUtil.getOpno());
    }

    @Override
    public ModelSet queryById(String id) {
        return modelSetDao.queryById(id);
    }

    @Override
    public ModelSet queryByName(String name) {
        return modelSetDao.queryByName(name);
    }

    @Override
    public List<Model> queryTemplateContainsModel() {
        Map<String, Object> templateMap = getTemplateMap();
        Set<String> secodSets = new HashSet<>();
        for (Map.Entry<String, Object> map : templateMap.entrySet()) {
            if (CollectionUtils.isNotEmpty((Collection) map.getValue())) {
                secodSets.addAll((Collection<? extends String>) map.getValue());
            }
        }
        List<Model> models = modelDao.queryBySecondcategoryList(secodSets);
        return models;
    }
}
