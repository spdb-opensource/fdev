package com.spdb.fdev.fdevinterface.base.utils;

import com.google.common.collect.Lists;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Soap提供方、Soap调用方、Sop调用方公共方法
 */
@Component
public class SopSoapUtil {

    private SopSoapUtil() {
    }

    /**
     * 解析Soap提供方、Soap调用方报文头
     *
     * @param headerPath
     * @return
     */
    public static Map analysisHeaderFile(String headerPath, String errorCode) {
        Map map = new HashMap();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(headerPath, errorCode);
        List<Element> complexTypeList = root.elements(Dict.COMPLEXTYPE);
        if (CollectionUtils.isEmpty(complexTypeList)) {
            return map;
        }
        for (Element complexType : complexTypeList) {
            String complexTypeName = complexType.attributeValue(Dict.NAME);
            if (Dict.REQHEADERTYPE.equals(complexTypeName)) {
                // 解析请求头
                List<Param> reqHeaderList = getHeader(complexType);
                map.put(Dict.L_REQHEADER, reqHeaderList);
            }
            if (Dict.RSPHEADERTYPE.equals(complexTypeName)) {
                // 解析响应头
                List<Param> rspHeaderList = getHeader(complexType);
                map.put(Dict.L_RSPHEADER, rspHeaderList);
            }
        }
        return map;
    }

    /**
     * 解析请求头及响应头
     *
     * @param complexType
     * @return
     */
    public static List<Param> getHeader(Element complexType) {
        List<Param> paramList = new ArrayList<>();
        Element sequence = complexType.element(Dict.SEQUENCE);
        List<Element> elementList = sequence.elements(Dict.ELEMENT);
        if (CollectionUtils.isEmpty(elementList)) {
            return paramList;
        }
        for (Element element : elementList) {
            String name = element.attributeValue(Dict.NAME);
            String type = element.attributeValue(Dict.TYPE);
            String description = element.attributeValue(Dict.DESCRIPTION);
            String flag = element.attributeValue(Dict.MINOCCURS);
            Integer required = 1;
            if (!StringUtils.isEmpty(flag) && "0".equals(flag)) {
                required = 0;
            }
            String alias = element.attributeValue(Dict.ALIAS);
            if (!StringUtils.isEmpty(type)) {
                type = type.split(":")[1];
            }
            Param param = new Param(name, description, type, required, alias);
            paramList.add(param);
        }
        return paramList;
    }

    /**
     * 解析Soap提供方、Soap调用方的mapping.xml文件
     *
     * @param mappingXmlPath
     * @return
     */
    public static List<Map> analysisMappingXmlFile(String mappingXmlPath, String errorCode) {
        List<Map> mapList = new ArrayList<>();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(mappingXmlPath, errorCode);
        List<Element> beanList = root.elements(Dict.BEAN);
        if (CollectionUtils.isEmpty(beanList)) {
            return Collections.emptyList();
        }
        // 获取元素信息
        for (Element bean : beanList) {
            // 获取beanid 判断是不是restUriMapping
            String beanId = bean.attributeValue(Dict.ID);
            if (Dict.IDENTITYMAPPING.equals(beanId) || Dict.IDENTITYMAPPING4MONITOR.equals(beanId)
                    || (mappingXmlPath.contains(Dict.CLIENT) && Dict.IDENTITYMAPPINGCLIENT.equals(beanId))) {
                Element property = bean.element(Dict.PROPERTY);
                // 获取property的孩子节点
                Element props = property.element(Dict.PROPS);
                Element lists = property.element(Dict.LIST);
                if(!Util.isNullOrEmpty(props)) {
                	List<Element> propList = props.elements(Dict.PROP);
                    List<Map> list = getPropList(propList);
                    if (CollectionUtils.isNotEmpty(list)) {
                        mapList.addAll(list);
                    }
                }else if(!Util.isNullOrEmpty(lists)) {
                	List<Element> propList = lists.elements(Dict.VALUE);
                	List<Map> list = getValueList(propList);
                    if (CollectionUtils.isNotEmpty(list)) {
                        mapList.addAll(list);
                    }
                }
            }
        }
        return mapList;
    }

    public static List<Map> getPropList(List<Element> propList) {
        List<Map> mapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(propList)) {
            // 获取param的属性值
            for (Element prop : propList) {
                // poops eg:<prop key="{http://esb.spdbbiz.com/services/S120030118}ReqPreCtfQry">FV38</prop>
                String key = prop.attributeValue(Dict.KEY);
                String interfaceAlias = prop.getText();
                if (Dict.WEB_SERVICE_DEMO.equals(interfaceAlias)) {
                    continue;
                }
                String transId = "";
                if (!StringUtils.isEmpty(key)) {
                    // 取key里面的serviceProvider
                    transId = key.split("/")[4];
                    if (transId.contains("}Req")) {
                        transId = transId.replace("}Req", "");
                    }
                    if (transId.contains("}Rsp")) {
                        transId = transId.replace("}Rsp", "");
                    }
                }
                Map<String, String> map = new HashMap<>();
                map.put(Dict.INTERFACEALIAS, interfaceAlias);
                map.put(Dict.TRANSID, transId);
                map.put(Dict.L_URI, key);
                mapList.add(map);
            }
        }
        return mapList;
    }

    /**
     * 解析Soap提供方、Soap调用方、Sop调用方报文体
     *
     * @param xmlFilePath
     * @return
     */
    public static Map analysisInterfaceXml(String xmlFilePath, String errorCode) {
        Map map = new HashMap();
        // 默认是最新版本的接口
        map.put(Dict.ISNEW, 1);
        // 默认版本为0
        map.put(Dict.VER, 0);
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(xmlFilePath, errorCode);
        if (root == null) {
            map.put(Dict.L_REQUEST, Lists.newArrayList());
            map.put(Dict.L_RESPONSE, Lists.newArrayList());
            return map;
        }
        String interfaceName = root.attributeValue(Dict.DESCRIPTION);
        map.put(Dict.L_INTERFACENAME, interfaceName);
        // 解析请求体
        List<Param> sndParamList = Lists.newArrayList();
        Element snd = root.element(Dict.SND);
        if (snd != null) {
            Element sndKeys = snd.element(Dict.KEYS);
            sndParamList = analysisSndOrRcv(sndKeys);
        }
        // 解析响应体
        List<Param> rcvParamList = Lists.newArrayList();
        Element rcv = root.element(Dict.RCV);
        if (rcv != null) {
            Element rcvKeys = rcv.element(Dict.KEYS);
            rcvParamList = analysisSndOrRcv(rcvKeys);
        }
        // <rcv>是指接收到的报文字段，<snd>是指发送出去的报文字段
        // 对调用方系统，<rcv>是响应体，<snd>是请求体，在xml里面先写<snd>后写<rcv>
        // 对提供方系统，<rcv>是请求体，<snd>是响应体，在xml里面先写<rcv>后写<snd>
        if (ErrorConstants.SOAP_PROVIDER_SCAN_ERROR.equals(errorCode)) {
            map.put(Dict.L_REQUEST, rcvParamList);
            map.put(Dict.L_RESPONSE, sndParamList);
        } else {
            map.put(Dict.L_REQUEST, sndParamList);
            map.put(Dict.L_RESPONSE, rcvParamList);
        }
        return map;
    }

    /**
     * 解析请求体和响应体
     *
     * @param keys
     * @return
     */
    public static List<Param> analysisSndOrRcv(Element keys) {
        if (keys == null) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        List<Element> itemList = keys.elements(Dict.ITEM);
        List<Element> indexsList = keys.elements(Dict.INDEXS);
        List<Element> keysList = keys.elements(Dict.KEYS);
        // 解析单个参数
        paramList.addAll(analysisItem(itemList));
        // 解析List参数
        paramList.addAll(analysisIndexs(indexsList));
        // 解析其他的keys
        paramList.addAll(analysisKeys(keysList));
        return paramList;
    }

    /**
     * 解析item元素
     *
     * @param elementList
     * @return
     */
    public static List<Param> analysisItem(List<Element> elementList) {
        if (CollectionUtils.isEmpty(elementList)) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        for (Element item : elementList) {
            String name = item.attributeValue(Dict.NAME);
            String description = item.attributeValue(Dict.DESCRIPTION);
            String type = getType(item.attributeValue(Dict.TYPE));
            String flag = item.attributeValue(Dict.REQUIRED);
            Integer required = 0;
            if (!StringUtils.isEmpty(flag) && Dict.TRUE.equals(flag)) {
                required = 1;
            }
            String aliase = item.attributeValue(Dict.ALIASE);
            Param param = new Param(name, description, type, required, aliase);
            paramList.add(param);
        }
        return paramList;
    }

    /**
     * 解析Indexs List
     *
     * @param indexsList
     * @return
     */
    public static List<Param> analysisIndexs(List<Element> indexsList) {
        if (CollectionUtils.isEmpty(indexsList)) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        for (Element indexs : indexsList) {
            String name = indexs.attributeValue(Dict.NAME);
            String description = indexs.attributeValue(Dict.DESCRIPTION);
            String type = getType(indexs.attributeValue(Dict.TYPE));
            String flag = indexs.attributeValue(Dict.REQUIRED);
            Integer required = 0;
            if (Dict.TRUE.equals(flag)) {
                required = 1;
            }
            String aliase = indexs.attributeValue(Dict.ALIASE);
            Param indexsParam = new Param(name, description, type, required, aliase);
            Element indexsKeys = indexs.element(Dict.KEYS);
            if (indexsKeys != null) {
                List<Element> indexsItemList = indexsKeys.elements(Dict.ITEM);
                List<Element> indexsIndexsList = indexsKeys.elements(Dict.INDEXS);
                List<Element> keysKeysList = indexsKeys.elements(Dict.KEYS);
                // 解析indexs里item单个参数
                List<Param> indexsParamList = analysisItem(indexsItemList);
                // 递归解析indexs的indexs参数
                if (CollectionUtils.isNotEmpty(indexsIndexsList)) {
                    List indexsIndexsParamList = analysisIndexs(indexsIndexsList);
                    indexsParamList.addAll(indexsIndexsParamList);
                }
                // 解析indexs里的keys参数
                if (CollectionUtils.isNotEmpty(keysKeysList)) {
                    List keysKeysParamList = analysisKeys(keysKeysList);
                    indexsParamList.addAll(keysKeysParamList);
                }
                indexsParam.setParamList(indexsParamList);
            }
            paramList.add(indexsParam);
        }
        return paramList;
    }

    /**
     * 解析Keys
     *
     * @param keysList
     * @return
     */
    public static List<Param> analysisKeys(List<Element> keysList) {
        if (keysList == null) {
            return Lists.newArrayList();
        }
        List<Param> paramList = new ArrayList<>();
        for (Element indexs : keysList) {
            String name = indexs.attributeValue(Dict.NAME);
            String description = indexs.attributeValue(Dict.DESCRIPTION);
            String type = getType(indexs.attributeValue(Dict.TYPE));
            String flag = indexs.attributeValue(Dict.REQUIRED);
            Integer required = 0;
            if (Dict.TRUE.equals(flag)) {
                required = 1;
            }
            String aliase = indexs.attributeValue(Dict.ALIASE);
            Param indexsParam = new Param(name, description, type, required, aliase);
            // 获取keys下的item，index，keys
            List<Element> keysItemList = indexs.elements(Dict.ITEM);
            List<Element> keysIndexsList = indexs.elements(Dict.INDEXS);
            List<Element> keysKeysList = indexs.elements(Dict.KEYS);
            // 解析keys里的item单个参数
            List<Param> indexsParamList = analysisItem(keysItemList);
            // 解析keys里的indexs参数
            if (CollectionUtils.isNotEmpty(keysIndexsList)) {
                List keysIndexsParamList = analysisIndexs(keysIndexsList);
                indexsParamList.addAll(keysIndexsParamList);
            }
            // 递归解析keys里的参数
            if (CollectionUtils.isNotEmpty(keysKeysList)) {
                List keysKeysParamList = analysisKeys(keysKeysList);
                indexsParamList.addAll(keysKeysParamList);
            }
            indexsParam.setParamList(indexsParamList);
            paramList.add(indexsParam);
        }
        return paramList;
    }

    private static String getType(String type) {
        if (StringUtils.isEmpty(type)) {
            return type;
        }
        switch (type.toLowerCase()) {
            case Constants.JAVA_STRING:
                type = Dict.STRING;
                break;
            case Constants.INDEXED_DATA:
                type = Dict.LIST;
                break;
            case Constants.KEYED_DATA:
                type = Dict.OBJECT;
                break;
            default:
                break;
        }
        return type;
    }
    
    public static List<Map> getValueList(List<Element> propList) {
        List<Map> mapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(propList)) {
            // 获取param的属性值
            for (Element prop : propList) {
                // poops eg:{http://esb.spdbbiz.com/services/S120030006}ReqMsgSignDtlQuery=5762
                String value = prop.getStringValue();
                String interfaceAlias = "";
                String transId = "";
                String url = "";
                if (!StringUtils.isEmpty(value)) {
                	interfaceAlias = value.split("=")[1];
                	String lastString = value.split("/")[4];
                	String wsdlNum = lastString.split("}")[0];
                	String req = lastString.split("}")[1].split("=")[0];
                	transId = wsdlNum + req;
                	url = value.split("Req")[0];
                }
                Map<String, String> map = new HashMap<>();
                map.put(Dict.INTERFACEALIAS, interfaceAlias);
                map.put(Dict.TRANSID, transId);
                map.put(Dict.L_URI, url);
                mapList.add(map);
            }
        }
        return mapList;
    }

}
