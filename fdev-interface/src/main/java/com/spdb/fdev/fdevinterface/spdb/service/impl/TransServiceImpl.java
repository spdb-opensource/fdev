package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.csii.pe.redis.util.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.TransDao;
import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.entity.TransParamDesciption;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.TransService;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.TransShow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransServiceImpl implements TransService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;
    @Resource
    private TransDao transDao;

    @Override
    public void saveTrans(List<Trans> transList, String appServiceId, String branchName) {
        transDao.deleteTrans(appServiceId, branchName);
        transDao.saveTrans(transList);
    }

    @Override
    public List<Trans> getTransList(@NotNull String serviceId, @NotNull String branch) {
        return transDao.getTransList(serviceId, branch);
    }

    @Override
    public Map showTrans(TransParamShow paramShow) {
        Map<String, Object> returnMap = new HashMap();
        Map transMap = transDao.showTrans(paramShow);
        // 获得全量的交易
        List<Trans> transList = (List<Trans>) transMap.get(Dict.LIST);
        // 组装交易，有多种渠道的交易，只返回给前端一条交易数据
        List<TransShow> transShowList = new ArrayList<>();
        Map<String, TransShow> map = new HashMap<>();
        for (Trans trans : transList) {
            TransShow transShow = CommonUtil.convert(trans, TransShow.class);
            String mapKey = trans.getTransId() + trans.getServiceId() + trans.getBranch();
            // 组装详情Id
            Map<String, String> channelIdMap = new HashMap<>();
            switch (trans.getChannel()) {
                case Dict.CLIENT:
                    channelIdMap.put(Dict.CLIENT, trans.getId());
                    break;
                case Dict.AJSON:
                    channelIdMap.put(Dict.AJSON, trans.getId());
                    break;
                case Dict.HTTP:
                    channelIdMap.put(Dict.HTTP, "");
                    break;
                case Dict.HTML:
                    channelIdMap.put(Dict.HTML, "");
                    break;
                default:
                    break;
            }
            if (map.containsKey(mapKey)) {
                channelIdMap.putAll(map.get(mapKey).getChannelIdMap());
            }
            transShow.setChannelIdMap(channelIdMap);
            map.put(mapKey, transShow);
        }
        for (Map.Entry<String, TransShow> m : map.entrySet()) {
            TransShow transShow = m.getValue();
            // 设置关联的应用Id
            transShow.setAppId(interfaceLazyInitService.getAppIdByName(m.getValue().getServiceId()));
            transShowList.add(transShow);
        }
        returnMap.put(Dict.TOTAL, transMap.get(Dict.TOTAL));
        returnMap.put(Dict.LIST, transShowList);
        return returnMap;
    }

    @Override
    public Map showAllTrans() {
        Map<String, Object> returnMap = new HashMap();
        // 获得全量的交易
        List<Trans> transList = transDao.showAllTrans();
        List<Map<String, Object>> list = new ArrayList<>();
        // 筛选字段
        for (Trans trans : transList) {
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.BRANCH, trans.getBranch());
            map.put("needLogin", trans.getNeedLogin());
            map.put("channelList", trans.getChannelList());
            map.put(Dict.L_SERVICEID, trans.getServiceId());
            map.put(Dict.TRAN_ID, trans.getTransId());
            map.put(Dict.TRANSNAME, trans.getTransName());
            map.put("writeJnl", trans.getWriteJnl());
            list.add(map);
        }
        returnMap.put(Dict.TOTAL, list.size());
        returnMap.put(Dict.LIST, list);
        return returnMap;
    }

    @Override
    public Trans getTransDetailById(String id) {
        Trans trans=transDao.getTransDetailById(id);
        String transId = trans.getTransId();
        String serviceId = trans.getServiceId();
        String channel = trans.getChannel();
        //获取修改后的参数描述和备注
        TransParamDesciption paramDesciption = transDao.getParamDescription(transId, serviceId, channel);
        //关联修改的参数描述或备注
        if (!FileUtil.isNullOrEmpty(paramDesciption)) {
            List<Map<String,Object>> request = paramDesciption.getRequest();
            List<Map<String,Object>> response = paramDesciption.getResponse();
            List<Map<String,Object>> requestParam = trans.getRequest();
            List<Map<String,Object>> responseParam = trans.getResponse();
            if (!FileUtil.isNullOrEmpty(request)) {
                List<Map<String,Object>> requestParamNew = getDescription(requestParam, request);
                trans.setRequest(requestParamNew);
            }
            if (!FileUtil.isNullOrEmpty(response)) {
                List<Map<String,Object>> responseParamNew = getDescription(responseParam, response);
                trans.setResponse(responseParamNew);
            }
        }
        return trans;
    }

    /**
     * 关联获取参数描述或备注
     * @param param1
     * @param param2
     * @return
     */
    private List<Map<String,Object>> getDescription(List<Map<String,Object>> param1, List<Map<String,Object>> param2) {
        List<Map<String,Object>> params = new ArrayList<>();
        for (Map param : param1) {
            if (!FileUtil.isNullOrEmpty(param.get("item"))) {
                for (Map pa : param2) {
                    if (param.get("name") != null && param.get("name").equals(pa.get("name"))) {
                        param.put("content",pa.get("content"));
                        param.put("note",pa.get("note"));
                    }
                    getDescription((List<Map<String,Object>>)param.get("item"), (List<Map<String,Object>>)pa.get("item"));
                }
            } else {
                if(!FileUtil.isNullOrEmpty(param2)){
                    for (Map pa : param2) {
                        if (param.get("name") != null && param.get("name").equals(pa.get("name"))) {
                            param.put("content",pa.get("content"));
                            param.put("note",pa.get("note"));
                        }
                    }
                }

            }
            params.add(param);
        }
        return params;

    }

    @Override
    public void updateTransTags(String id, List<String> tags) {
        Trans trans = transDao.getTransDetailById(id);
        List<Trans> transList = transDao.getTrans(trans);
        List<String> ids = transList.stream().map(Trans::getId).collect(Collectors.toList());
        transDao.updateTransTags(ids, tags);
    }

    public void updateParamDescription(Trans trans){
        String transId = trans.getTransId();
        String serviceId = trans.getServiceId();
        String channel = trans.getChannel();
        TransParamDesciption paramDesciption = transDao.getParamDescription(transId, serviceId, channel);
        if (FileUtil.isNullOrEmpty(paramDesciption)) {
            TransParamDesciption paramDesciptionNew = new TransParamDesciption();
            List<TransParamDesciption> paramDesciptions = new ArrayList<>();
            paramDesciptionNew.setTransId(transId);
            paramDesciptionNew.setServiceId(serviceId);
            paramDesciptionNew.setChannel(channel);
            List<Map<String,Object>> requestParam = trans.getRequest();
            List<Map<String,Object>> responseParam = trans.getResponse();
            if (!FileUtil.isNullOrEmpty(requestParam)) {
                paramDesciptionNew.setRequest(requestParam);
            }
            if (!FileUtil.isNullOrEmpty(responseParam)) {
                paramDesciptionNew.setResponse(responseParam);
            }
            paramDesciptions.add(paramDesciptionNew);
            transDao.saveParamDescription(paramDesciptions);
        } else {
            //将每次修改的参数取并集
            List<Map<String,Object>> allRequestParam = paramUnionSet(trans.getRequest(), paramDesciption.getRequest());
            trans.setRequest(allRequestParam);
            List<Map<String,Object>> allResponseParam = paramUnionSet(trans.getResponse(), paramDesciption.getResponse());
            trans.setResponse(allResponseParam);
            transDao.updateParamDescription(trans);
        }

    }

    @Override
    public void deleteTransData(Map params) {
        transDao.deleteTrans(params);
    }

    /**
     * 将不同版本和分支的参数描述和备注取并集
     * @param newList
     * @param oldList
     * @return
     */
    private List<Map<String,Object>> paramUnionSet(List<Map<String,Object>> newList , List<Map<String,Object>> oldList){
        if(!FileUtil.isNullOrEmpty(newList)&&!FileUtil.isNullOrEmpty(oldList)){
            for (Map newParam : newList) {
                for (Map oldParam : oldList) {
                    if (newParam.get(Dict.NAME) != null && newParam.get("name").equals(oldParam.get("name")) && newParam.get("type") != null && newParam.get("type").equals(oldParam.get("type"))) {
                        List<Map<String,Object>> newparamList = (List<Map<String,Object>>)newParam.get("item");
                        List<Map<String,Object>> oldparamList = (List<Map<String,Object>>)oldParam.get("item");
                        if (CollectionUtils.isNotEmpty(newparamList) || CollectionUtils.isNotEmpty(oldparamList)) {
                            List<Map<String,Object>> allParamList = paramUnionSet(newparamList, oldparamList);
                            newParam.put("item",allParamList);
                        }
                        oldList.remove(oldParam);
                        break;
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(oldList)) {
            if(CollectionUtils.isEmpty(newList)){
                newList=oldList;
            }else{
                newList.addAll(oldList);
            }
        }
        return newList;
    }
    
    @Override
    public Map queryTrans(TransParamShow paramShow) {
        Map transMap = transDao.queryTrans(paramShow);
        return transMap;
    }

	@Override
	public void exprotTrans(Map map,HttpServletResponse resp) throws Exception {
		List<Trans> list = (List<Trans>) map.get(Dict.LIST);
		// excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setCellValue(workbook, 0, 0, 0, "交易ID", cellStyle);
        setCellValue(workbook, 0, 0, 1, "交易名称", cellStyle);
        setCellValue(workbook, 0, 0, 2, "应用名称", cellStyle);
        setCellValue(workbook, 0, 0, 3, "分支", cellStyle);
        setCellValue(workbook, 0, 0, 4, "是否记录流水", cellStyle);
        setCellValue(workbook, 0, 0, 5, "是否登录", cellStyle);
        setCellValue(workbook, 0, 0, 6, "图形验证码", cellStyle);
        int i = 1;
        for (Trans trans : list) {
			setCellValue(workbook, 0, i, 0, trans.getTransId(), null);
			setCellValue(workbook, 0, i, 1, trans.getTransName(), null);
			setCellValue(workbook, 0, i, 2, trans.getServiceId(), null);
			setCellValue(workbook, 0, i, 3, trans.getBranch(), null);
			if(!Util.isNullOrEmpty(trans.getWriteJnl())) {
				setCellValue(workbook, 0, i, 4, "0".equals(trans.getWriteJnl().toString())?"否":"是", null);
			}
			if(!Util.isNullOrEmpty(trans.getNeedLogin())) {
				setCellValue(workbook, 0, i, 5, "0".equals(trans.getNeedLogin().toString())?"否":"是", null);
			}
			setCellValue(workbook, 0, i, 6, trans.getVerCodeType(), null);
			i++;
        }
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "RelaseRqrmntList.xls");
        workbook.write(resp.getOutputStream());
	}

	
	
	/**
	 * excel填值
	 * @param
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
	private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue, CellStyle cellStyle) {
	    Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
        	throw new FdevException(ErrorConstants.GENERATE_EXCEL_FAIL);
        }
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }
        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }
        sheet.getRow(rowIndex).getCell(cellIndex).setCellFormula(null);
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(cellStyle);
        sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
	}
	
}
