package com.spdb.fdev.fdevinterface.spdb.quartz;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.EsbRelationDao;
import com.spdb.fdev.fdevinterface.spdb.entity.EsbRelation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
@RefreshScope
public class DownloadEsbExcelCallable implements Runnable {

    private Logger logger = LoggerFactory.getLogger(DownloadEsbExcelCallable.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EsbRelationDao esbRelationDao;
    @Value("${esb.export.url}")
    private String esbExportUrl;
    @Value("${esb.export.dir}")
    private String esbExportDir;
    @Value("${sheet.name}")
    private String sheetName;
    @Value("${consumer.sys.ids}")
    private String consumerSysIds;

    private static List<String> headers = new ArrayList<>();

    static {
        headers.add(Dict.SERVICE_ID_AND_NAME);
        headers.add(Dict.OPERATION_ID_AND_NAME);
        headers.add(Dict.TRAN_ID);
        headers.add(Dict.TRAN_NAME);
        headers.add(Dict.SOURCE_SYS_ID);
        headers.add(Dict.CONSUMER_SYS_ID_AND_NAME);
        headers.add(Dict.PROVIDER_SYS_ID_AND_NAME);
        headers.add(Dict.AIM_SYS_ID);
        headers.add(Dict.CONSUMER_MSG_TYPE);
        headers.add(Dict.PROVIDER_MSG_TYPE);
        headers.add(Dict.THROUGH);
        headers.add(Dict.STATE);
        headers.add(Dict.UPDATE_TIMES);
        headers.add(Dict.ONLINE_DATE);
        headers.add(Dict.ONLINE_VER);
        headers.add(Dict.REMARK);
        headers.add(Dict.PROTOCOL_ID);
    }

    @Override
    public void run() {
        List<String> filePathList = new ArrayList<>();
        String[] consumerSysIdArray = consumerSysIds.split(",");
        try {
            // 下载consumerSysId为0014-0017的Excel
            for (String consumerSysId : consumerSysIdArray) {
                String filePath = downloadRelationExcel(consumerSysId);
                filePathList.add(filePath);
            }
            List<Map<String, Object>> allRelationList = new ArrayList<>();
            for (String fileName : filePathList) {
                List<Map<String, Object>> relationList = getExcelData(fileName);
                if (CollectionUtils.isNotEmpty(relationList)) {
                    allRelationList.addAll(relationList);
                }
            }
            // 存库
            saveEsbRelation(allRelationList);
        } catch (FdevException exception) {
            logger.info("定时下载Excel出错：{}", exception.getArgs()[0]);
            throw exception;
        } finally {
            // 删除下载的表格
            FileUtil.deleteFiles(new File(esbExportDir));
        }

    }

    /**
     * 根据consumerSysId下载Excel
     *
     * @param consumerSysId
     */
    public String downloadRelationExcel(String consumerSysId) {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.L_SERVICEID, "");
        param.put(Dict.SERVICE_NAME, "");
        param.put(Dict.OPERATION_ID, "");
        param.put(Dict.OPERATION_NAME, "");
        param.put(Dict.TRAN_ID, "");
        param.put(Dict.TRAN_NAME, "");
        param.put(Dict.SOURCE_SYS_ID, "");
        param.put(Dict.CONSUMER_SYS_ID, consumerSysId);
        param.put(Dict.PROVIDER_SYS_ID, "");
        param.put(Dict.AIM_SYS_ID, "");
        param.put(Dict.CONSUMER_MSG_TYPE, "");
        param.put(Dict.PROVIDER_MSG_TYPE, "");
        param.put(Dict.THROUGH, "");
        param.put(Dict.STATE, "");
        param.put(Dict.START_DATE, "");
        param.put(Dict.END_DATE, "");
        param.put(Dict.SERVICE_AND_OPERATION_ID, "");
        param.put(Dict.ECODES, "");
        param.put(Dict.TO_SHOW, "2");
        param.put(Dict.PROTOCOL_ID, "");
        String url;
        try {
            url = esbExportUrl + URLEncoder.encode(JSON.toJSONString(param), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"参数编码出错！"});
        }
        logger.info(url);
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);
        try {
            return writeToExcel(responseEntity, consumerSysId);
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"写入Excel出错！"});
        }
    }

    /**
     * 将响应内容写到文件中
     *
     * @param responseEntity
     * @param consumerSysId
     * @return
     * @throws IOException
     */
    private String writeToExcel(ResponseEntity<byte[]> responseEntity, String consumerSysId) throws IOException {
        if (!Paths.get(esbExportDir).toFile().exists()) {
            Files.createDirectories(Paths.get(esbExportDir));
        }
        // 表格以consumerSysId命名，如0014.xlsx
        if (StringUtils.isEmpty(consumerSysId)) {
            consumerSysId = "all";
        }
        String fileName = consumerSysId + "." + responseEntity.getHeaders().getContentDisposition().getFilename().split("\\.")[1];
        Files.write(Paths.get(esbExportDir + fileName), Objects.requireNonNull(responseEntity.getBody(), "下载文件内容为空！"));
        return esbExportDir + fileName;
    }

    /**
     * 根据fileName解析Excel
     *
     * @param fileName
     * @return
     */
    private List<Map<String, Object>> getExcelData(String fileName) {
        List<Map<String, Object>> relationList = new ArrayList<>();
        Workbook workbook = null;
        try (InputStream fileInputStream = new FileInputStream(fileName)) {
            workbook = new XSSFWorkbook(fileInputStream);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"解析Excel出错！"});
        }
        Sheet sheet = workbook.getSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        // num是标题，lastRowNum是最后一行的行号
        for (int num = 1; num <= lastRowNum; num++) {
            Map<String, Object> map = new HashMap<>();
            Row row = sheet.getRow(num);
            Iterator<Cell> cellIterator = row.cellIterator();
            int index = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch (cell.getCellTypeEnum()) {
                    case FORMULA:
                        map.put(headers.get(index), cell.getCellFormula());
                        break;
                    case NUMERIC:
                        map.put(headers.get(index), cell.getNumericCellValue());
                        break;
                    case STRING:
                        map.put(headers.get(index), cell.getStringCellValue().trim());
                        break;
                    case BLANK:
                        map.put(headers.get(index), "");
                        break;
                    case BOOLEAN:
                        map.put(headers.get(index), cell.getBooleanCellValue());
                        break;
                    case ERROR:
                        map.put(headers.get(index), "ERROR VALUE");
                        break;
                    default:
                        break;
                }
                index++;
            }
            map.put("rowNum", num);
            relationList.add(map);
        }
        return relationList;
    }

    /**
     * 存库
     */
    private void saveEsbRelation(List<Map<String, Object>> allRelationList) {
        // 去重
        removeRepeat(allRelationList);
        List<EsbRelation> esbRelationList = new ArrayList<>();
        for (Map<String, Object> map : allRelationList) {
            String serviveIdAndName = String.valueOf(map.get(Dict.SERVICE_ID_AND_NAME));
            String operationIdAndName = String.valueOf(map.get(Dict.OPERATION_ID_AND_NAME));
            String[] serviveIdAndNames = serviveIdAndName.split("/");
            String[] operationIdAndNames = operationIdAndName.split("/");
            String serviceId = serviveIdAndNames[0];
            String serviceName = serviveIdAndNames[1];
            String operationId = operationIdAndNames[0];
            String operationName = operationIdAndNames[1];
            String serviceAndOperationId = serviceId + operationId;
            EsbRelation esbRelation = new EsbRelation();
            esbRelation.setServiceAndOperationId(serviceAndOperationId);
            esbRelation.setServiceId(serviceId);
            esbRelation.setServiceName(serviceName);
            esbRelation.setOperationId(operationId);
            esbRelation.setOperationName(operationName);
            esbRelation.setTranId(String.valueOf(map.get(Dict.TRAN_ID)));
            esbRelation.setTranName(String.valueOf(map.get(Dict.TRAN_NAME)));
            esbRelation.setConsumerSysIdAndName(String.valueOf(map.get(Dict.CONSUMER_SYS_ID_AND_NAME)));
            esbRelation.setProviderSysIdAndName(String.valueOf(map.get(Dict.PROVIDER_SYS_ID_AND_NAME)));
            esbRelation.setConsumerMsgType(String.valueOf(map.get(Dict.CONSUMER_MSG_TYPE)));
            esbRelation.setProviderMsgType(String.valueOf(map.get(Dict.PROVIDER_MSG_TYPE)));
            esbRelation.setState(String.valueOf(map.get(Dict.STATE)));
            esbRelation.setOnlineVer(String.valueOf(map.get(Dict.ONLINE_VER)));
            esbRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            esbRelationList.add(esbRelation);
        }
        esbRelationDao.deleteEsbRelation();
        esbRelationDao.saveEsbRelation(esbRelationList);
    }

    /**
     * 根据 serviceId+operationId+tansId+consumerMsgType+providerMsgType 去重
     *
     * @param allRelationList
     */
    private void removeRepeat(List<Map<String, Object>> allRelationList) {
        Iterator<Map<String, Object>> iterator = allRelationList.iterator();
        Set<String> set = new HashSet<>(allRelationList.size());
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            String serviceId = String.valueOf(next.get(Dict.SERVICE_ID_AND_NAME)).split("/")[0];
            String operationId = String.valueOf(next.get(Dict.OPERATION_ID_AND_NAME)).split("/")[0];
            String tranId = String.valueOf(next.get(Dict.TRAN_ID));
            String consumerMsgType = String.valueOf(next.get(Dict.CONSUMER_MSG_TYPE));
            String providerMsgType = String.valueOf(next.get(Dict.PROVIDER_MSG_TYPE));
            String serviceAndOperationId = serviceId + operationId + tranId + consumerMsgType + providerMsgType;
            if (!set.add(serviceAndOperationId)) {
                iterator.remove();
            }
        }
    }

}
