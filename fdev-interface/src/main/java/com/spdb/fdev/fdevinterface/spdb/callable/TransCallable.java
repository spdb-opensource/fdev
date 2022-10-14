package com.spdb.fdev.fdevinterface.spdb.callable;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import com.spdb.fdev.fdevinterface.spdb.dto.TransTemplate;
import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Component(Dict.TRANS_CALLABLE)
public class TransCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(TransCallable.class);

    // 路径相关
    @Value(value = "${path.trans.files}")
    private String transFiles;
    @Value(value = "${path.trans.mclient.files}")
    private String mclientFiles;
    @Value(value = "${path.trans.json.files}")
    private String jsonFiles;
    @Value(value = "${path.trans.template}")
    private String templateFilePath;
    @Value(value = "${path.trans.chain}")
    private String chainFilePath;

    private String mclientFilesPath;
    private String jsonFilesPath;

    /**
     * 扫描项目所有的交易定义文件
     *
     * @return 返回被jsonResult装载的执行后的成功信息和失败信息
     */
    @Override
    public Object call() {
        logger.info(Constants.TRANS_SCAN_START);
        Map returnMap = new HashMap();
        try {
            // 获取template文件路径
            String templatePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, templateFilePath);
            // 获取chain文件路径
            String chainPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, chainFilePath);
            // 获取trans文件夹路径
            String transFilesPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, transFiles);
            if (StringUtils.isEmpty(templatePath) || StringUtils.isEmpty(chainPath) || StringUtils.isEmpty(transFilesPath)) {
                return returnMap;
            }
            // 获取trans文件夹下所有文件路径
            List<String> transSubFilesPath = new ArrayList<>();
            transSubFilesPath = FileUtil.getSubTransFilesPath(transFilesPath, transSubFilesPath);
            mclientFilesPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, mclientFiles);
            jsonFilesPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, jsonFiles);
            // 获取此次扫描需要保存的数据
            List<Trans> transList = getTransList(transSubFilesPath, templatePath, chainPath);
            // 持久化trans
            transService.saveTrans(transList, super.getAppServiceId(), super.getBranchName());
            logger.info(Constants.TRANS_SCAN_END);
            returnMap.put(Dict.SUCCESS, Constants.TRANS_SCAN_SUCCESS);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 获得trans的消息头文件路径
     *
     * @return
     */
    private List<String> getHeaderFilePath() {
        List<String> headerPathList = new ArrayList<>();
        // 请求头路径：src/main/resources/packets/mclient/XmlRequestPacket.xml
        String reqPath = mclientFilesPath + pathJoin + Constants.TRANS_REQ_HEADER;
        // 响应头路径：src/main/resources/packets/mclient/XmlResponsePacket.xml
        String rspPath = mclientFilesPath + pathJoin + Constants.TRANS_RSP_HEADER;
        File reqPathFile = new File(reqPath);
        File rspPathFile = new File(rspPath);
        if (reqPathFile.exists()) {
            headerPathList.add(reqPath);
        } else {
            throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{"没有找到trans的请求头文件" + reqPath + "！"});
        }
        if (rspPathFile.exists()) {
            headerPathList.add(rspPath);
        } else {
            throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{"没有找到trans的响应头文件" + rspPath + "！"});
        }
        return headerPathList;
    }

    /**
     * 解析appHeader.json
     *
     * @return
     */
    private Trans getJsonHeader() {
        String jsonHeaderPath = jsonFilesPath + pathJoin + Constants.TRANS_JSON_HEADER;
        File jsonHeaderFile = new File(jsonHeaderPath);
        if (jsonHeaderFile.exists()) {
            Trans trans = new Trans();
            try (FileReader fileReader = new FileReader(jsonHeaderPath)) {
                // 获取appHeader.json的内容
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);
                JsonObject reqHeader = jsonObject.getAsJsonObject(Dict.REQHEADER);
                JsonObject rspHeader = jsonObject.getAsJsonObject(Dict.RSPHEADER);
                // 解析请求头参数列表
                List<Param> reqHeaderParamList = FileUtil.analysisParams(reqHeader, 3);
                trans.setReqHeader(FileUtil.convertToTransDetail(reqHeaderParamList));
                // 解析响应头参数列表
                List<Param> rspHeaderParamList = FileUtil.analysisParams(rspHeader, 3);
                trans.setRspHeader(FileUtil.convertToTransDetail(rspHeaderParamList));
                return trans;
            } catch (Exception e) {
                logger.error("{}", Constants.ANALYSIS_FILE_ERROR + jsonHeaderPath + "，" + e.getMessage());
                throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{Constants.ANALYSIS_FILE_ERROR + jsonHeaderPath + "！"});
            }
        } else {
            throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{"没有找到trans的头文件" + jsonHeaderPath + "！"});
        }
    }

    /**
     * 解析json文件
     *
     * @param jsonPath
     * @return
     */
    private Trans getJsonBody(String jsonPath) {
        JsonParser jsonParser = new JsonParser();
        File jsonHeaderFile = new File(jsonPath);
        Trans trans = new Trans();
        if (jsonHeaderFile.exists()) {
            try (FileReader fileReader = new FileReader(jsonPath)) {
                JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);
                // 解析元数据
                JsonObject metadata = jsonObject.getAsJsonObject(Dict.METADATA);
                if (metadata == null) {
                    throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{jsonPath + "，不存在Metadata！"});
                }
                String interfaceName = metadata.get(Dict.INTERFACENAME).getAsString();
                String serviceId = metadata.get(Dict.SERVICEID).getAsString();
                if (!super.getAppServiceId().equalsIgnoreCase(serviceId)) {
                    throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{jsonPath + "，ServiceId不一致！"});
                }
                trans.setRequestType(metadata.get(Dict.REQUESTTYPE).getAsString());
                trans.setReqProtocol(metadata.get(Dict.REQUESTPROTOCOL).getAsString());
                trans.setTransName(interfaceName);

                // 解析请求体
                JsonObject request = jsonObject.getAsJsonObject(Dict.REQUEST);
                if (request == null) {
                    throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{jsonPath + "，不存在Request！"});
                }
                JsonObject requestProperties = request.getAsJsonObject(Dict.PROPERTIES);
                String requestSchemaValue = request.get("$schema").getAsString();
                trans.setRequest(Lists.newArrayList());
                if (requestProperties != null) {
                    JsonObject reqBody = requestProperties.getAsJsonObject(Dict.REQBODY);
                    if (requestSchemaValue.contains("draft-04")) {
                        trans.setRequest(FileUtil.convertToTransDetail(FileUtil.analysisParams(reqBody, 4)));
                    } else {
                        trans.setRequest(FileUtil.convertToTransDetail(FileUtil.analysisParams(reqBody, 3)));
                    }
                }
                // 解析响应体
                JsonObject response = jsonObject.getAsJsonObject(Dict.RESPONSE);
                if (response == null) {
                    throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{jsonPath + "，不存在Response！"});
                }
                JsonObject responseProperties = response.getAsJsonObject(Dict.PROPERTIES);
                String responseSchemaValue = response.get("$schema").getAsString();
                trans.setResponse(Lists.newArrayList());
                if (responseProperties != null) {
                    JsonObject rspBody = responseProperties.getAsJsonObject(Dict.RSPBODY);
                    if (responseSchemaValue.contains("draft-04")) {
                        trans.setResponse(FileUtil.convertToTransDetail(FileUtil.analysisParams(rspBody, 4)));
                    } else {
                        trans.setResponse(FileUtil.convertToTransDetail(FileUtil.analysisParams(rspBody, 3)));
                    }
                }
            } catch (FdevException e) {
                throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{errorMessageUtil.get(e)});
            } catch (Exception e) {
                logger.error("{}", Constants.ANALYSIS_FILE_ERROR + jsonPath + "，" + e.getMessage());
                throw new FdevException(ErrorConstants.TRANS_SCAN_ERROR, new String[]{Constants.ANALYSIS_FILE_ERROR + jsonPath + "！"});
            }
        } else {
            logger.error("没有找到trans的消息体文件{}！", jsonPath);
        }
        return trans;
    }

    /**
     * 组装交易的pe-template.xml和pe-chain.xml
     *
     * @param templatePath
     * @param chainPath
     * @return
     */
    public List<TransTemplate> getTemplateAndChain(String templatePath, String chainPath) {
        // 获取pe-template.xml文件内容
        List<TransTemplate> transTemplateList = getTemplateList(templatePath);
        // 获取pe-chain.xml文件内容
        Map<String, Integer> transChainMap = getChainList(chainPath);
        // 组装pe-template.xml和pe-chain.xml文件内容
        for (TransTemplate transTemplate : transTemplateList) {
            transTemplate.setNeedLogin(transChainMap.get(transTemplate.getChainId()));
        }
        return transTemplateList;
    }

    /**
     * 解析trans文件夹下xml文件中请求渠道为client，http，html的交易
     *
     * @param transSubFilesPath
     * @return
     */
    public List<Trans> getTransXml(List<String> transSubFilesPath) {
        List<Trans> transList = new ArrayList<>();
        for (String transPath : transSubFilesPath) {
            File transFile = new File(transPath);
            if (!transFile.exists()) {
                continue;
            }
            Element root = FileUtil.getXmlRootElement(transPath, ErrorConstants.TRANS_SCAN_ERROR);
            // 获取所有交易定义元素
            List<Element> transactionList = root.elements(Dict.TRANSACTION);
            for (Element transaction : transactionList) {
                Trans trans = new Trans();
                /** 获取请求渠道 */
                List<Element> channelEleList = transaction.element(Dict.CHANNELS).elements(Dict.CHANNEL);
                List<String> channelList = new ArrayList<>();
                for (Element channelElm : channelEleList) {
                    String channelType = channelElm.attributeValue(Dict.TYPE);
                    // 只扫描渠道为client，http，html的交易
                    if (!StringUtils.isEmpty(channelType) && Dict.CLIENT.equals(channelType)
                            || Dict.HTTP.equals(channelType) || Dict.HTML.equals(channelType)
                            || Dict.AJSON.equals(channelType)) {
                        channelList.add(channelType);
                    }
                }
                if (CollectionUtils.isEmpty(channelList)) {
                    continue;
                }
                /** 获取交易码 */
                String transId = transaction.attributeValue(Dict.ID);
                // 跳过transId为空的扫描 跳过transId为test的扫描
                if (StringUtils.isEmpty(transId) || transId.equals(Dict.TEST)) {
                    continue;
                }
                /** 获取template */
                String templateName = transaction.attributeValue(Dict.TEMPLATE);
                /** 获取交易名称 */
                Element descriptionElm = transaction.element(Dict.DESCRIPTION);
                String description = getTransName(descriptionElm);
                /** 获取是否记流水和图片验证码 */
                Element settingElm = transaction.element(Dict.SETTING);
                Map map = getVerCodeAndWriteJnl(settingElm);
                if (map.containsKey(Dict.WRITEJNL)) {
                    trans.setWriteJnl(Integer.valueOf(map.get(Dict.WRITEJNL).toString()));
                }
                trans.setVerCodeType("");
                if (map.containsKey(Dict.VERCODETYPE)) {
                    trans.setVerCodeType(map.get(Dict.VERCODETYPE).toString());
                }
                trans.setChannelList(channelList);
                trans.setTransId(transId);
                trans.setTransName(description);
                trans.setTemplateId(templateName);
                trans.setFileName(transPath.substring(transPath.lastIndexOf(pathJoin) + 1));
                transList.add(trans);
            }
        }
        return transList;
    }

    /**
     * 获取交易名称
     *
     * @param descriptionElm
     * @return
     */
    public String getTransName(Element descriptionElm) {
        String description = "";
        if (Util.isNullOrEmpty(descriptionElm)) {
            return description;
        }
        String transDescStr = descriptionElm.getText();
        if (StringUtils.isEmpty(transDescStr)) {
            return description;
        }
        // 兼容过去的规范,如果当前描述字符串包含@,则认为@trsname或@transName后面跟着的是交易名
        if (transDescStr.contains("@")) {
            String[] split = transDescStr.split("@");
            for (String tempStr : split) {
                if (tempStr.contains(Dict.TRANSNAME) || tempStr.contains(Dict.TRSNAME)) {
                    int nameIndex = tempStr.contains(Dict.TRANSNAME) ? 9 : 7;
                    transDescStr = tempStr.substring(nameIndex);
                    break;
                }
            }
        }
        // 处理后如果字符串里还包括@符号，说明没有配置@trsname或@transName，直接返回
        if (transDescStr.contains("@")) {
            return description;
        }
        // 去除所有的换行,制表符以及空格
        Pattern pattern = Pattern.compile("\t|\r|\n");
        Matcher matcher = pattern.matcher(transDescStr);
        description = matcher.replaceAll("");
        description = description.replaceAll(" +", "");
        return description;
    }

    /**
     * 获取是否记流水和图片校验码
     *
     * @param settingElm
     * @return
     */
    public Map getVerCodeAndWriteJnl(Element settingElm) {
        Map map = new HashMap<>();
        if (Util.isNullOrEmpty(settingElm)) {
            return map;
        }
        List<Element> paramElms = settingElm.elements(Dict.PARAM);
        if (Util.isNullOrEmpty(paramElms)) {
            return map;
        }
        for (Element paramElm : paramElms) {
            String nameValue = paramElm.attributeValue(Dict.NAME);
            // 判断是否记录流水，如果没有配置，则使用template里的配置
            if (Dict.WRITELOG.equals(nameValue) || Dict.WRITEJNL.equals(nameValue)) {
                String flag = paramElm.getTextTrim().toLowerCase();
                if (Dict.YES.equals(flag) || Dict.TRUE.equals(flag) || Dict.Y.equals(flag)) {
                    map.put(Dict.WRITEJNL, 1);
                }
            }
            if (Dict.VERCODETYPE.equals(nameValue)) {
                map.put(Dict.VERCODETYPE, paramElm.getTextTrim());
            }
        }
        return map;
    }

    /**
     * 全部扫描：组装trans、template、消息头、消息体
     *
     * @param transSubFilesPath
     * @param templatePath
     * @param chainPath
     * @return
     */
    public List<Trans> scanAll(List<String> transSubFilesPath, String templatePath, String chainPath) {
        List<Trans> transList = getTransXml(transSubFilesPath);
        List<Trans> saveTransList = new ArrayList<>();
        List<TransTemplate> transTemplateList = getTemplateAndChain(templatePath, chainPath);
        for (Trans trans : transList) {
            // 设置是否记流水及是否需要登录
            for (TransTemplate transTemplate : transTemplateList) {
                if (transTemplate.getTemplateId().equals(trans.getTemplateId())) {
                    if (StringUtils.isEmpty(trans.getWriteJnl())) {
                        trans.setWriteJnl(transTemplate.getWriteJnl());
                    }
                    trans.setNeedLogin(transTemplate.getNeedLogin());
                    break;
                }
            }
            trans.setServiceId(super.getAppServiceId());
            trans.setBranch(super.getBranchName());
            trans.setRequestType("");
            trans.setReqProtocol(Dict.HTTP);
            // 标签默认设置为空
            trans.setTags(Lists.newArrayList());
            // 设置初始值
            trans.setReqHeader(new ArrayList<>());
            trans.setRspHeader(new ArrayList<>());
            trans.setRequest(new ArrayList<>());
            trans.setResponse(new ArrayList<>());
            trans.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            trans.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            List<String> channelList = trans.getChannelList();
            for (String channel : channelList) {
                Trans saveTrans = CommonUtil.convert(trans, Trans.class);
                saveTrans.setChannel(channel);
                // 获取client渠道的交易详情
                if (Dict.CLIENT.equals(channel) && !StringUtils.isEmpty(mclientFilesPath)) {
                    saveTrans.setRequestType(Dict.XML);
                    // 获取trans的头文件路径
                    List<String> headerPathList = getHeaderFilePath();
                    // 解析消息头
                    Map headerMap = getHeader(headerPathList);
                    saveTrans.setReqHeader((List<Map<String, Object>>) headerMap.get(Dict.REQ_HEADER));
                    saveTrans.setRspHeader((List<Map<String, Object>>) headerMap.get(Dict.RSP_HEADER));
                    String rFilePath = mclientFilesPath + pathJoin + Dict.R + trans.getTransId() + Constants.XML_FILE;
                    String sFilePath = mclientFilesPath + pathJoin + Dict.S + trans.getTransId() + Constants.XML_FILE;
                    // 解析请求体
                    List<Map<String, Object>> request = getMclientXML(rFilePath);
                    saveTrans.setRequest(request);
                    // 解析响应体
                    List<Map<String, Object>> response = getMclientXML(sFilePath);
                    saveTrans.setResponse(response);
                }
                // 获取ajson渠道的交易详情
                if (Dict.AJSON.equals(channel) && !StringUtils.isEmpty(jsonFilesPath)) {
                    // 解析消息头
                    Trans jsonHeader = getJsonHeader();
                    saveTrans.setReqHeader(jsonHeader.getReqHeader());
                    saveTrans.setRspHeader(jsonHeader.getRspHeader());
                    String jsonFilePath = jsonFilesPath + pathJoin + trans.getTransId() + Constants.JSON_FILE;
                    // 解析消息体
                    Trans jsonBody = getJsonBody(jsonFilePath);
                    saveTrans.setRequestType(jsonBody.getRequestType());
                    saveTrans.setReqProtocol(jsonBody.getReqProtocol());
                    saveTrans.setRequest(jsonBody.getRequest());
                    saveTrans.setResponse(jsonBody.getResponse());
                }
                saveTransList.add(saveTrans);
            }
        }
        return saveTransList;
    }

    /**
     * 保留历史标签
     *
     * @param transSubFilesPath
     * @param templatePath
     * @param chainPath
     * @return
     */
    public List<Trans> getTransList(List<String> transSubFilesPath, String templatePath, String chainPath) {
        // 获得数据库里面的交易，目前都为client
        List<Trans> oldTransList = transService.getTransList(super.getAppServiceId(), super.getBranchName());
        List<Trans> newTransList = scanAll(transSubFilesPath, templatePath, chainPath);
        for (Trans newTrans : newTransList) {
            for (Trans oldTrans : oldTransList) {
                // 对老数据做更新
                if (newTrans.getTransId().equals(oldTrans.getTransId()) && newTrans.getChannel().equals(oldTrans.getChannel())) {
                    // 保留上一版本中的交易标签
                    newTrans.setTags(oldTrans.getTags());
                    break;
                }
            }
        }
        return newTransList;
    }

    /**
     * 解析pe-template.xml
     *
     * @param templatePath
     */
    private List<TransTemplate> getTemplateList(String templatePath) {
        List<TransTemplate> transTemplateList = new ArrayList<>();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(templatePath, ErrorConstants.TRANS_SCAN_ERROR);
        // 获取所有交易定义元素
        List<Element> templateList = root.elements(Dict.TEMPLATE);
        for (Element template : templateList) {
            TransTemplate transTemplate = new TransTemplate();
            String templateId = template.attributeValue(Dict.ID);
            String chainId = template.attributeValue(Dict.CHAIN);
            transTemplate.setTemplateId(templateId);
            transTemplate.setChainId(chainId);
            Element setting = template.element(Dict.SETTING);
            if (Util.isNullOrEmpty(setting)) {
                transTemplate.setWriteJnl(0);
                transTemplateList.add(transTemplate);
                continue;
            }
            List<Element> paramList = setting.elements(Dict.PARAM);
            for (Element param : paramList) {
                String name = param.attributeValue(Dict.NAME);
                transTemplate.setWriteJnl(0);
                if (Dict.WRITELOG.equals(name) || Dict.WRITEJNL.equals(name)) {
                    String flag = param.getTextTrim().toLowerCase();
                    if (Dict.YES.equals(flag) || Dict.TRUE.equals(flag) || Dict.Y.equals(flag)) {
                        transTemplate.setWriteJnl(1);
                    }
                    transTemplateList.add(transTemplate);
                    break;
                }
            }
        }
        return transTemplateList;
    }

    /**
     * 解析pe-chain.xml
     *
     * @param chainPath
     */
    private Map<String, Integer> getChainList(String chainPath) {
        Map<String, Integer> chainMap = new HashMap<>();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(chainPath, ErrorConstants.TRANS_SCAN_ERROR);
        // 获取所有交易定义元素
        List<Element> chainList = root.elements(Dict.CHAIN);
        for (Element chain : chainList) {
            String chainId = chain.attributeValue(Dict.ID);
            Element commands = chain.element(Dict.COMMANDS);
            List<Element> refList = commands.elements(Dict.REF);
            for (Element ref : refList) {
                String refValue = ref.getTextTrim();
                chainMap.put(chainId, 0);
                if (Dict.RULE_COMMAND.equals(refValue)) {
                    chainMap.put(chainId, 1);
                    break;
                }
            }
        }
        return chainMap;
    }

    /**
     * 获取头文件
     *
     * @param headerPathList
     * @return
     */
    private Map getHeader(List<String> headerPathList) {
        Map headerMap = new HashMap();
        for (String headerPath : headerPathList) {
            if (headerPath.contains(Constants.TRANS_REQ_HEADER)) {
                List<Map<String, Object>> reqHeader = getMclientXML(headerPath);
                headerMap.put(Dict.REQ_HEADER, reqHeader);
            } else {
                List<Map<String, Object>> rspHeader = getMclientXML(headerPath);
                headerMap.put(Dict.RSP_HEADER, rspHeader);
            }
        }
        return headerMap;
    }

    /**
     * 解析mclient文件夹下的xml文件（消息头、消息体）
     *
     * @param mclientFullPath
     * @return
     */
    public List<Map<String, Object>> getMclientXML(String mclientFullPath) {
        List<Map<String, Object>> xmlParamMapList = new ArrayList<>();
        File file = new File(mclientFullPath);
        if (!file.exists()) {
            return xmlParamMapList;
        }
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(mclientFullPath, ErrorConstants.TRANS_SCAN_ERROR);
        // 跳过空白文件
        if (Util.isNullOrEmpty(root)) {
            return xmlParamMapList;
        }
        List<Element> list;
        // 消息头的xml中root下层是xmlSegment，消息体的xml中root下层是xmlElementWhithChild,
        Element xmlSegmentElm = root.element(Dict.XML_SEGMENT);
        if (Util.isNullOrEmpty(xmlSegmentElm)) {
            list = root.elements(Dict.XML_ELEMENT_WHITH_CHILD);
        } else {
            list = xmlSegmentElm.elements(Dict.XML_ELEMENT_WHITH_CHILD);
        }
        // 获取元素信息
        for (Element property : list) {
            // 获取所有参数所在标签节点
            List<Element> paramsElms = property.elements();
            if (Util.isNullOrEmpty(paramsElms)) {
                return xmlParamMapList;
            }
            for (Element paramsElm : paramsElms) {
                String elmName = paramsElm.getName();
                if (!Dict.XML_ELEMENT_WHITH_CHILD.equals(elmName)) {
                    // 处理非集合的单个请求参数 ,xmlElementWhithChild是集合类型的参数
                    Map<String, Object> paramsMap = analysisParamsElement(paramsElm);
                    xmlParamMapList.add(paramsMap);
                } else {
                    // 处理集合的参数,从这里才能开始递归,因为xml文档结构在第一层参数定义块缺失上层<loopXmlElement>
                    List<Map<String, Object>> collectionParamsList = analysisXMLCollectionParams(paramsElm);
                    xmlParamMapList.addAll(collectionParamsList);
                }
            }
        }
        return xmlParamMapList;
    }

    /**
     * 解析集合类型的参数
     *
     * @param paramsElm
     * @return
     */
    private List<Map<String, Object>> analysisXMLCollectionParams(Element paramsElm) {
        List<Map<String, Object>> itemsMapList = new ArrayList<>();
        // 此处是被嵌套在集合里的里层集合
        List<Element> loopXmlElementList = paramsElm.elements("loopXmlElement");
        if (CollectionUtils.isEmpty(loopXmlElementList)) {
            return itemsMapList;
        }
        for (Element currentloopXmlElement : loopXmlElementList) {
            // 处理此集合标签的信息
            Map<String, Object> xmlParamMap = analysisParamsElement(currentloopXmlElement);
            // 拿到loop里的参数节点,注意此出代码块解析出来的全是当前xmlParamMap的item的值,即list标签下的标签
            List<Element> elements = currentloopXmlElement.elements();
            if (null != elements) {
                List<Map<String, Object>> currentItemMapList = new ArrayList<>();
                for (Element currentElement : elements) {
                    String currentElementName = currentElement.getName();
                    if (!Dict.XML_ELEMENT_WHITH_CHILD.equals(currentElementName)) {
                        // 处理非集合的单个请求参数
                        Map<String, Object> paramsMap = analysisParamsElement(currentElement);
                        currentItemMapList.add(paramsMap);
                    } else {
                        // 递归解析集合参数
                        List<Map<String, Object>> collectionParamList = analysisXMLCollectionParams(currentElement);
                        currentItemMapList.addAll(collectionParamList);
                    }
                }
                xmlParamMap.put("item", currentItemMapList);
            }
            itemsMapList.add(xmlParamMap);
        }
        return itemsMapList;
    }

    /**
     * 处理非集合的单个请求参数
     *
     * @param paramsElm
     * @return
     */
    private Map<String, Object> analysisParamsElement(Element paramsElm) {
        String elmName = paramsElm.getName();
        String paramName = paramsElm.attributeValue("name");// 真正的返回参数字段
        // 参数类型,已知单个参数标签的种类:<fixAmountElement> 和
        // <fixStringElement>,以及集合类型的<loopXmlElement>
        String paramType = "Object";
        if (elmName.equals("loopXmlElement")) {
            paramType = "List";
        } else if (elmName.indexOf("Element") != -1) {
            paramType = elmName.substring(3, elmName.indexOf("Element"));
        }
        String paramDescription = paramsElm.attributeValue("content");// 参数描述,旧规范是content , 新规范是description
        paramDescription = !Util.isNullOrEmpty(paramDescription) ? paramDescription
                : paramsElm.attributeValue("description");
        String length = paramsElm.attributeValue("maxLength");// 参数描述
        String required = paramsElm.attributeValue("required");// 参数是否必须输入
        if (!Util.isNullOrEmpty(required)) {
            required = required.equals("true") ? "1" : "0";
        } else {
            required = "0";
        }
        String trim = paramsElm.attributeValue("trim");// 参数是否做trim
        if (!Util.isNullOrEmpty(trim)) {
            trim = trim.equals("true") ? "1" : "0";
        } else {
            trim = "0";
        }
        String decimal = paramsElm.attributeValue("decimal");// 参数小数位精度
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Dict.NAME, paramName);
        paramsMap.put(Dict.CONTENT, paramDescription);
        paramsMap.put(Dict.TYPE, paramType);
        paramsMap.put(Dict.OPTION, required);
        paramsMap.put(Dict.LENGTH, length);
        paramsMap.put(Dict.TRIM, trim);
        paramsMap.put(Dict.DECIMAL, decimal);
        paramsMap.put(Dict.NOTE, "");
        return paramsMap;
    }

}
