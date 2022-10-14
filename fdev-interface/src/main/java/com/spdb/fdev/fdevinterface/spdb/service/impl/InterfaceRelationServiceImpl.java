package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.MD5Util;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.EsbRelationDao;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceDao;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceRelationDao;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import com.spdb.fdev.fdevinterface.spdb.entity.*;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceRelationService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceService;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceDetailShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceRelationShow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class InterfaceRelationServiceImpl implements InterfaceRelationService {

    @Resource
    private InterfaceDao interfaceDao;
    @Resource
    private EsbRelationDao esbRelationDao;
    @Resource
    private InterfaceRelationDao interfaceRelationDao;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;
    @Autowired
    private RestTransportServiceImpl restTransportServiceImpl;
    @Resource
    private InterfaceService interfaceService;
    @Value("${finterface.nas}")
    private String tmpPath;
    @Value("${finterface.api}")
    private String finterfaceApi;

    @Override
    public void saveRestRelation(List<RestRelation> restRelationList, String appServiceId, String branchName) {
        // 删除RestRelation
        interfaceRelationDao.deleteRestRelation(appServiceId, branchName);
        for (RestRelation restRelation : restRelationList) {
            restRelation.setServiceCalling(appServiceId);
            restRelation.setBranch(branchName);
            restRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        }
        interfaceRelationDao.saveRestRelation(restRelationList);
    }

    @Override
    public void deleteRestRelationData(Map params) {
        interfaceRelationDao.deleteRestRelation(params);
    }

    @Override
    public void saveSoapRelationList(List<SoapRelation> soapRelationList) {
        interfaceRelationDao.saveSoapRelationList(soapRelationList);
    }

    @Override
    public List<SoapRelation> getSoapRelationList(String appServiceId, String branchName) {
        return interfaceRelationDao.getSoapRelationList(appServiceId, branchName);
    }

    @Override
    public void updateSoapRelationIsNewByIds(List<String> idList) {
        interfaceRelationDao.updateSoapRelationIsNewByIds(idList);
    }

    @Override
    public void deleteSoapRelationData(Map params) {
        interfaceRelationDao.deleteSoapRelation(params);
    }

    @Override
    public void saveSopRelationList(List<SopRelation> sopRelationList) {
        interfaceRelationDao.saveSopRelationList(sopRelationList);
    }

    @Override
    public List<SopRelation> getSopRelationList(String appServiceId, String branchName) {
        return interfaceRelationDao.getSopRelationList(appServiceId, branchName);
    }

    @Override
    public void updateSopRelationIsNewByIds(List<String> idList) {
        interfaceRelationDao.updateSopRelationIsNewByIds(idList);
    }

    @Override
    public void deleteSopRelationData(Map params) {
        interfaceRelationDao.deleteSopRelation(params);
    }

    @Override
    public List showInterfaceRelation(InterfaceParamShow interfaceParamShow) {
        List<InterfaceRelationShow> list = new ArrayList<>();
        List<InterfaceRelationShow> soapRelationList = getSoapRelation(interfaceParamShow);
        list.addAll(soapRelationList);
        List<InterfaceRelationShow> sopRelationList = getSopRelation(interfaceParamShow);
        list.addAll(sopRelationList);
        List<InterfaceRelationShow> restRelationList = getRestRelation(interfaceParamShow);
        list.addAll(restRelationList);
        if (StringUtils.isNotEmpty(interfaceParamShow.getInterfaceType())) {
            List resultList = new ArrayList();
            for (InterfaceRelationShow interfaceRelationShow : list) {
                if (interfaceRelationShow.getInterfaceType().equals(interfaceParamShow.getInterfaceType())) {
                    resultList.add(interfaceRelationShow);
                }
            }
            return resultList;
        }
        return list;
    }


    @Override
    public String createExcel(Map<String, Object> resultMap) {
        List<InterfaceRelationShow> list = (List) resultMap.get(Dict.LIST);
        List<Map> interfaceList = new ArrayList<>();
        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFFont font = workBook.createFont();
        font.setBold(true);
        XSSFSheet sheet = workBook.createSheet("INDEX");
        XSSFRow hssfRow = sheet.createRow(0);
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        XSSFCellStyle cellStyle0 = getXSSFCellStyle0(workBook, font);
        XSSFCellStyle cellStyle1 = getXSSFCellStyle1(workBook, font);
        XSSFCellStyle cellStyle2 = getXSSFCellStyle2(workBook, font);
        XSSFCellStyle cellStyle3 = getXSSFCellStyle3(workBook, font);
        XSSFCellStyle cellStyle4 = getXSSFCellStyle4(workBook);
        String[] array = {"交易代码", "接口名称", "调用方系统", "提供方系统", "调用方分支", "URI", "请求类型", "请求协议", "接口描述", "提供方应用负责人", "调用方应用负责人"};
        for (int i = 0; i < array.length; i++) {
            XSSFCell headCell = hssfRow.createCell(i);
            headCell.setCellValue(array[i]);
            addBorder(headCell, sheet, cellStyle0);
        }

        for (int i = 0; i < list.size(); i++) {
            hssfRow = sheet.createRow(i + 1);
            InterfaceRelationShow interfaceRelationShow = list.get(i);
            List interfaceId = getSoapAndSopId(interfaceRelationShow);
            interfaceList.addAll(interfaceId);
            XSSFCell cell = hssfRow.createCell(0);
            cell.setCellValue(interfaceRelationShow.getTransId());
            CreationHelper create = workBook.getCreationHelper();
            XSSFHyperlink link = (XSSFHyperlink) create.createHyperlink(HyperlinkType.FILE);
            String url = cell.getStringCellValue();
            if (url.length() > 32) {
                String encoder = "LongTranId" + MD5Util.encoder("", url).substring(8, 24);
                link.setAddress("#'" + encoder + "'!A1");
            } else {
                link.setAddress("#'" + url + "(" + interfaceRelationShow.getInterfaceType() + ")" + "'!A1");
            }
            cell.setHyperlink(link);
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(1);
            cell.setCellValue(interfaceRelationShow.getInterfaceName());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(2);
            cell.setCellValue(interfaceRelationShow.getServiceCalling());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(3);
            cell.setCellValue(interfaceRelationShow.getServiceId());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(4);
            cell.setCellValue(interfaceRelationShow.getBranch());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(5);
            cell.setCellValue(interfaceRelationShow.getUri());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(6);
            cell.setCellValue(interfaceRelationShow.getRequestType());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(7);
            cell.setCellValue(interfaceRelationShow.getRequestProtocol());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(8);
            cell.setCellValue(interfaceRelationShow.getDescription());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(9);
            cell.setCellValue(interfaceRelationShow.getAppServiceManagers());
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(10);
            cell.setCellValue(interfaceRelationShow.getAppCallingManagers());
            addBorder(cell, sheet, cellStyle);
        }
        List<Map> interfaceIdList = mapDistinct(interfaceList);
        for (int i = 0; i < interfaceIdList.size(); i++) {
            InterfaceDetailShow interfaceDetailShow = interfaceService.getInterfaceDetailById((String) interfaceIdList.get(i).get("interfaceId"), (String) interfaceIdList.get(i).get("interfaceType"));
            String transId = interfaceDetailShow.getTransId();
            XSSFSheet sheet2;
            if (transId.length() > 32) {
                String encoder = "LongTranId" + MD5Util.encoder("", transId).substring(8, 24);
                sheet2 = workBook.createSheet(encoder);
            } else {
                sheet2 = workBook.createSheet(transId + "(" + interfaceIdList.get(i).get("interfaceType") + ")");
            }
            XSSFRow row = sheet2.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("TransId");
            addBorder(cell, sheet2, cellStyle1);
            cell = row.createCell(1);
            CellRangeAddress cra = new CellRangeAddress(0, 0, 1, 2);
            sheet2.addMergedRegion(cra);
            cell.setCellValue(interfaceDetailShow.getTransId());
            addBorder(cell, sheet2, cellStyle);
            cell = row.createCell(2);
            addBorder(cell, sheet2, cellStyle);
            cell = row.createCell(3);
            cell.setCellValue("InterfaceName");
            addBorder(cell, sheet2, cellStyle1);
            cell = row.createCell(4);
            cell.setCellValue(interfaceDetailShow.getInterfaceName());
            addBorder(cell, sheet2, cellStyle);


            XSSFRow row1 = sheet2.createRow(1);
            cell = row1.createCell(0);
            cell.setCellValue("Uri");
            addBorder(cell, sheet2, cellStyle1);
            cell = row1.createCell(1);
            CellRangeAddress cra1 = new CellRangeAddress(1, 1, 1, 2);
            sheet2.addMergedRegion(cra1);
            cell.setCellValue(interfaceDetailShow.getUri());
            addBorder(cell, sheet2, cellStyle);
            cell = row1.createCell(2);
            addBorder(cell, sheet2, cellStyle);
            cell = row1.createCell(3);
            cell.setCellValue("RequestType");
            addBorder(cell, sheet2, cellStyle1);
            cell = row1.createCell(4);
            cell.setCellValue(interfaceDetailShow.getRequestType());
            addBorder(cell, sheet2, cellStyle);


            XSSFRow row2 = sheet2.createRow(2);
            cell = row2.createCell(0);
            cell.setCellValue("InterfaceDescription");
            addBorder(cell, sheet2, cellStyle1);
            cell = row2.createCell(1);
            //合并单元格
            CellRangeAddress cra2 = new CellRangeAddress(2, 2, 1, 2);
            sheet2.addMergedRegion(cra2);
            cell.setCellValue(interfaceDetailShow.getDescription());
            addBorder(cell, sheet2, cellStyle);
            cell = row2.createCell(2);
            addBorder(cell, sheet2, cellStyle);
            cell = row2.createCell(3);
            cell.setCellValue("RequestProtocol");
            addBorder(cell, sheet2, cellStyle1);
            cell = row2.createCell(4);
            cell.setCellValue(interfaceDetailShow.getRequestProtocol());
            addBorder(cell, sheet2, cellStyle);


            XSSFRow row3 = sheet2.createRow(3);
            cell = row3.createCell(0);
            cell.setCellValue("接口详情");
            addBorder(cell, sheet2, cellStyle2);
            CellRangeAddress cra3 = new CellRangeAddress(3, 3, 0, 4);
            sheet2.addMergedRegion(cra3);

            XSSFRow row4 = sheet2.createRow(4);
            String[] arr = {"参数名称", "参数描述", "数据类型", "是否必输", "备注"};
            for (int j = 0; j < arr.length; j++) {
                cell = row4.createCell(j);
                cell.setCellValue(arr[j]);
                addBorder(cell, sheet2, cellStyle2);
            }
            XSSFRow row5 = sheet2.createRow(5);
            cell = row5.createCell(0);
            cell.setCellValue("输入");
            addBorder(cell, sheet2, cellStyle3);
            CellRangeAddress cra4 = new CellRangeAddress(5, 5, 0, 4);
            sheet2.addMergedRegion(cra4);
            int rowNum = 6;
            List<Param> request = interfaceDetailShow.getRequest();
            rowNum = creatRowAndCell(rowNum, request, sheet2, workBook, cellStyle, cellStyle4);
            XSSFRow rowOut = sheet2.createRow(rowNum);
            cell = rowOut.createCell(0);
            cell.setCellValue("输出");
            addBorder(cell, sheet2, cellStyle3);
            CellRangeAddress cra5 = new CellRangeAddress(rowNum, rowNum, 0, 4);
            sheet2.addMergedRegion(cra5);
            rowNum++;
            List<Param> response = interfaceDetailShow.getResponse();
            creatRowAndCell(rowNum, response, sheet2, workBook, cellStyle, cellStyle4);

        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = sdf.format(date);
        String excelPath = tmpPath + Constants.INTERFACECALL + Constants.CONFIG_FILE_PRE + timeStamp + ".xlsx";
        String fileName = Constants.CONFIG_FILE_PRE + timeStamp + ".xlsx";
        FileUtil.export(excelPath, tmpPath + Constants.INTERFACECALL, workBook);
        return finterfaceApi + "/interface/exportFile/" + fileName;

    }

    /**
     * 添加表格边框及样式
     *
     * @param cell
     * @param sheet
     * @param cellStyle
     */
    private void addBorder(XSSFCell cell, XSSFSheet sheet, XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(cellStyle);
        if (sheet.getSheetName().equals("INDEX")) {
            sheet.setColumnWidth(cell.getColumnIndex(), cell.getStringCellValue().getBytes().length * 400 > 20000 ? 9000 : cell.getStringCellValue().getBytes().length * 400);
        } else {
            sheet.setColumnWidth(cell.getColumnIndex(), 6000);
        }
    }

    private XSSFCellStyle getXSSFCellStyle0(XSSFWorkbook workBook, XSSFFont font) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle getXSSFCellStyle1(XSSFWorkbook workBook, XSSFFont font) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle getXSSFCellStyle2(XSSFWorkbook workBook, XSSFFont font) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle getXSSFCellStyle3(XSSFWorkbook workBook, XSSFFont font) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.GREEN.index);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle getXSSFCellStyle4(XSSFWorkbook workBook) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
        return cellStyle;
    }

    /**
     * 附表中输入输出参数填充
     *
     * @param rowNum
     * @param required
     * @param sheet
     * @param workBook
     * @return
     */
    private int creatRowAndCell(int rowNum, List<Param> required, XSSFSheet sheet, XSSFWorkbook workBook,
                                XSSFCellStyle cellStyle, XSSFCellStyle cellStyle4) {
        for (Param param : required) {
            if (!FileUtil.isNullOrEmpty(param.getParamList())) {
                XSSFRow rowx = sheet.createRow(rowNum);
                XSSFCell cellx = rowx.createCell(0);
                cellx.setCellValue("LoopResult");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowx.createCell(1);
                cellx.setCellValue("循环体");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowx.createCell(2);
                cellx.setCellValue("object[]");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowx.createCell(3);
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowx.createCell(4);
                cellx.setCellValue("start");
                addBorder(cellx, sheet, cellStyle4);
                rowNum++;
                List<Param> paramList = param.getParamList();
                for (Param pa : paramList) {
                    String t = pa.getType();
                    if (!FileUtil.isNullOrEmpty(pa.getParamList())) {
                        XSSFRow rowx1 = sheet.createRow(rowNum);
                        XSSFCell cellx1 = rowx1.createCell(0);
                        cellx1.setCellValue("LoopResult");
                        addBorder(cellx1, sheet, cellStyle4);
                        cellx1 = rowx1.createCell(1);
                        cellx1.setCellValue("循环体");
                        addBorder(cellx1, sheet, cellStyle4);
                        cellx1 = rowx1.createCell(2);
                        cellx1.setCellValue("object[]");
                        addBorder(cellx1, sheet, cellStyle4);
                        cellx1 = rowx1.createCell(3);
                        addBorder(cellx1, sheet, cellStyle4);
                        cellx1 = rowx1.createCell(4);
                        cellx1.setCellValue("start");
                        addBorder(cellx1, sheet, cellStyle4);
                        rowNum++;
                        rowNum = creatRowAndCell(rowNum, pa.getParamList(), sheet, workBook, cellStyle, cellStyle4);
                        XSSFRow rowxEnd = sheet.createRow(rowNum);
                        cellx = rowxEnd.createCell(0);
                        cellx.setCellValue("LoopResult");
                        addBorder(cellx, sheet, cellStyle4);
                        cellx = rowxEnd.createCell(1);
                        cellx.setCellValue("循环体");
                        addBorder(cellx, sheet, cellStyle4);
                        cellx = rowxEnd.createCell(2);
                        cellx.setCellValue("object[]");
                        addBorder(cellx, sheet, cellStyle4);
                        cellx = rowxEnd.createCell(3);
                        addBorder(cellx, sheet, cellStyle4);
                        cellx = rowxEnd.createCell(4);
                        cellx.setCellValue("End");
                        addBorder(cellx, sheet, cellStyle4);
                        rowNum++;


                    } else {
                        XSSFRow rowx1 = sheet.createRow(rowNum);
                        cellx = rowx1.createCell(0);
                        cellx.setCellValue(pa.getName());
                        addBorder(cellx, sheet, cellStyle);
                        cellx = rowx1.createCell(1);
                        cellx.setCellValue(pa.getDescription());
                        addBorder(cellx, sheet, cellStyle);
                        cellx = rowx1.createCell(2);
                        cellx.setCellValue(t);
                        addBorder(cellx, sheet, cellStyle);
                        cellx = rowx1.createCell(3);
                        if (param.getRequired() == null) {
                            cellx.setCellValue("N");
                        } else {
                            if (1 == param.getRequired()) {
                                cellx.setCellValue("Y");
                            } else {
                                cellx.setCellValue("N");
                            }
                        }
                        addBorder(cellx, sheet, cellStyle);
                        cellx = rowx1.createCell(4);
                        if (FileUtil.isNullOrEmpty(pa.getRemark())) {
                            cellx.setCellValue("-");
                        } else {
                            cellx.setCellValue(pa.getRemark());
                        }
                        addBorder(cellx, sheet, cellStyle);
                        rowNum++;

                    }

                }
                XSSFRow rowxEnd = sheet.createRow(rowNum);
                cellx = rowxEnd.createCell(0);
                cellx.setCellValue("LoopResult");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowxEnd.createCell(1);
                cellx.setCellValue("循环体");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowxEnd.createCell(2);
                cellx.setCellValue("object[]");
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowxEnd.createCell(3);
                addBorder(cellx, sheet, cellStyle4);
                cellx = rowxEnd.createCell(4);
                cellx.setCellValue("End");
                addBorder(cellx, sheet, cellStyle4);
                rowNum++;

            } else {
                XSSFRow rowx = sheet.createRow(rowNum);
                XSSFCell cellx = rowx.createCell(0);
                cellx.setCellValue(param.getName());
                addBorder(cellx, sheet, cellStyle);
                cellx = rowx.createCell(1);
                cellx.setCellValue(param.getDescription());
                addBorder(cellx, sheet, cellStyle);
                cellx = rowx.createCell(2);
                cellx.setCellValue(param.getType());
                addBorder(cellx, sheet, cellStyle);
                cellx = rowx.createCell(3);
                if (param.getRequired() == null) {
                    cellx.setCellValue("N");
                } else {
                    if (1 == param.getRequired()) {
                        cellx.setCellValue("Y");
                    } else {
                        cellx.setCellValue("N");
                    }
                }
                addBorder(cellx, sheet, cellStyle);
                cellx = rowx.createCell(4);
                if (FileUtil.isNullOrEmpty(param.getRemark())) {
                    cellx.setCellValue("-");
                } else {
                    cellx.setCellValue(param.getRemark());
                }
                addBorder(cellx, sheet, cellStyle);
                rowNum++;
            }

        }
        return rowNum;

    }

    @Override
    public Map ExcelList(List<RestApi> restApiList, List<InterfaceRelationShow> interfaceRelationShowList) {
        Map interfaceMap = new HashMap();
        for (InterfaceRelationShow interfaceRelationShow : interfaceRelationShowList) {
            String transId = interfaceRelationShow.getTransId();
            String serviceId = interfaceRelationShow.getServiceId();
            String branch = interfaceRelationShow.getBranch();
            String interfaceType = interfaceRelationShow.getInterfaceType();
            if ("REST".equals(interfaceType)) {
                for (RestApi restApiMap : restApiList) {
                    if (transId.equals(restApiMap.getTransId()) && serviceId.equals(restApiMap.getServiceId()) && branch.equals(restApiMap.getBranch())) {
                        interfaceRelationShow.setInterfaceName(restApiMap.getInterfaceName());
                        interfaceRelationShow.setRequestType(restApiMap.getRequestType());
                        interfaceRelationShow.setRequestProtocol(restApiMap.getRequestProtocol());
                        interfaceRelationShow.setDescription(restApiMap.getDescription());
                        break;
                    }
                }
            }
        }
        for (InterfaceRelationShow interfaceRelationShow : interfaceRelationShowList) {
            String appServiceName = interfaceRelationShow.getServiceId();//提供方
            String appCallingName = interfaceRelationShow.getServiceCalling();//调用方
            String appServiceManagers = interfaceLazyInitService.getAppManagers(appServiceName);
            String appCallingManagers = interfaceLazyInitService.getAppManagers(appCallingName);
            interfaceRelationShow.setAppServiceManagers(appServiceManagers);
            interfaceRelationShow.setAppCallingManagers(appCallingManagers);
        }
        interfaceMap.put(Dict.LIST, interfaceRelationShowList);
        return interfaceMap;
    }

    private List<InterfaceRelationShow> getRestRelation(InterfaceParamShow interfaceParamShow) {
        List<InterfaceRelationShow> interfaceRelationShowList = new ArrayList<>();
        List<RestRelation> relationList = interfaceRelationDao.showRestRelation(interfaceParamShow);
        List<RestRelation> restRelationList = new ArrayList<>();
        if ("other".equals(interfaceParamShow.getBranchDefault())) {
            for (RestRelation rest : relationList) {
                if (!"master".equals(rest.getBranch()) && !"SIT".equals(rest.getBranch())) {
                    restRelationList.add(rest);
                }
            }
        } else {
            restRelationList = relationList;
        }
        for (RestRelation restRelation : restRelationList) {
            InterfaceRelationShow interfaceRelationShow = CommonUtil.convert(restRelation, InterfaceRelationShow.class);
            String branch = interfaceRelationShow.getBranch();
            // 除了master分支的其他分支，调的都是SIT的接口
            if (!Dict.MASTER.equals(branch)) {
                branch = Dict.SIT;
            }
            RestApi restApi = interfaceDao.getRestApiName(interfaceRelationShow.getTransId(), interfaceRelationShow.getServiceId(), branch);
            // 关联RestApi中文名称及接口Id
            if (restApi != null) {
                interfaceRelationShow.setInterfaceName(restApi.getInterfaceName());
                interfaceRelationShow.setInterfaceId(restApi.getId());
            }
            // 获取serviceProvider的应用Id
            interfaceRelationShow.setServiceProviderAppId(interfaceLazyInitService.getAppIdByName(interfaceRelationShow.getServiceId()));
            // 获取callingParty的应用Id
            interfaceRelationShow.setCallingAppId(interfaceLazyInitService.getAppIdByName(interfaceRelationShow.getServiceCalling()));
            interfaceRelationShowList.add(interfaceRelationShow);
        }
        return interfaceRelationShowList;
    }


    private List<InterfaceRelationShow> getSoapRelation(InterfaceParamShow interfaceParamShow) {
        List<InterfaceRelationShow> interfaceRelationShowList = new ArrayList<>();
        List<SoapRelation> relationList = interfaceRelationDao.showSoapRelation(interfaceParamShow);
        List<SoapRelation> resultList = new ArrayList<>();
        if ("other".equals(interfaceParamShow.getBranchDefault())) {
            for (SoapRelation soap : relationList) {
                if (!"master".equals(soap.getBranch()) && !"SIT".equals(soap.getBranch())) {
                    resultList.add(soap);
                }
            }
        } else {
            resultList = relationList;
        }

        List<InterfaceRelationShow> updateNameList = new ArrayList<>();
        List<InterfaceRelationShow> updateServiceIdList = new ArrayList<>();
        for (SoapRelation soapRelation : resultList) {
            InterfaceRelationShow interfaceRelationShow = CommonUtil.convert(soapRelation, InterfaceRelationShow.class);
            String branch = interfaceRelationShow.getBranch();
            // 除了master分支的其他分支，调的都是SIT的接口
            if (!Dict.MASTER.equals(branch)) {
                branch = Dict.SIT;
            }
            String transId = interfaceRelationShow.getTransId();
            // 将数据库中的交易码展示为 服务ID+操作码
            interfaceRelationShow.setServiceAndOperationId(transId);
            String interfaceRelationShowServiceId = interfaceRelationShow.getServiceId();
            // 关联SoapApi接口Id及提供方应用英文名
            SoapApi soapApi = interfaceDao.getSoapApi(transId, branch);
            if (soapApi != null) {
                interfaceRelationShow.setInterfaceId(soapApi.getId());
                String serviceId = soapApi.getServiceId();
                interfaceRelationShow.setServiceId(serviceId);
                updateServiceIdList.add(interfaceRelationShow);
                // 获取serviceProvider的应用Id
                interfaceRelationShow.setServiceProviderAppId(interfaceLazyInitService.getAppIdByName(serviceId));
            } else {
                interfaceRelationShow.setInterfaceId(interfaceRelationShow.getId());
            }
            //关联ESB数据：4位交易码和接口名称
            List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelation(transId);
            if (!FileUtil.isNullOrEmpty(esbRelationList)) {
                EsbRelation esbRelation = esbRelationList.get(0);
                // 更新为ESB最新接口名称及提供方名称
                if ((esbRelation.getTranName() != null && !esbRelation.getTranName().equals(interfaceRelationShow.getInterfaceName()))
                        || (esbRelation.getProviderSysIdAndName() != null && !esbRelation.getProviderSysIdAndName().equals(interfaceRelationShowServiceId))) {
                    interfaceRelationShow.setInterfaceName(esbRelation.getTranName());
                    if (interfaceRelationShowServiceId.contains("/")) {
                        interfaceRelationShow.setServiceId(esbRelation.getProviderSysIdAndName());
                    }
                    updateNameList.add(interfaceRelationShow);
                }
                interfaceRelationShow.setTransId(esbRelation.getTranId());
            } else {
                // 未获取到4位交易码，说明该接口不在下载的ESB文档里
                interfaceRelationShow.setTransId("");
            }
            // 获取callingParty的应用Id
            interfaceRelationShow.setCallingAppId(interfaceLazyInitService.getAppIdByName(interfaceRelationShow.getServiceCalling()));
            //判断transId是否为空 赋值接口别名
            if(FileUtil.isNullOrEmpty(interfaceRelationShow.getTransId())) {
            	interfaceRelationShow.setTransId(interfaceRelationShow.getInterfaceAlias());
            }
            interfaceRelationShowList.add(interfaceRelationShow);
        }
        interfaceRelationDao.updateSoapRelationServiceId(updateServiceIdList);
        interfaceRelationDao.updateSoapRelationInterfaceName(updateNameList);
        return interfaceRelationShowList;
    }

    private List<InterfaceRelationShow> getSopRelation(InterfaceParamShow interfaceParamShow) {
        List<InterfaceRelationShow> interfaceRelationShowList = new ArrayList<>();
        List<SopRelation> relationList = interfaceRelationDao.showSopRelation(interfaceParamShow);
        List<SopRelation> resultList = new ArrayList<>();
        if ("other".equals(interfaceParamShow.getBranchDefault())) {
            for (SopRelation sop : relationList) {
                if (!"master".equals(sop.getBranch()) && !"SIT".equals(sop.getBranch())) {
                    resultList.add(sop);
                }
            }
        } else {
            resultList = relationList;
        }

        List<InterfaceRelationShow> updateNameList = new ArrayList<>();
        for (SopRelation sopRelation : resultList) {
            InterfaceRelationShow interfaceRelationShow = CommonUtil.convert(sopRelation, InterfaceRelationShow.class);
            interfaceRelationShow.setInterfaceId(interfaceRelationShow.getId());
            // 关联ESB数据：服务ID+操作ID和接口名称
            List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelation(sopRelation.getTransId());
            if (!FileUtil.isNullOrEmpty(esbRelationList)) {
                EsbRelation esbRelation = esbRelationList.get(0);
                String serviceAndOperationId = esbRelation.getServiceAndOperationId();
                if (StringUtils.isNotEmpty(serviceAndOperationId) && serviceAndOperationId.length() > 10) {
                    interfaceRelationShow.setEsbServiceId(serviceAndOperationId.substring(0, 10));
                    interfaceRelationShow.setEsbOperationId(serviceAndOperationId.substring(10));
                    interfaceRelationShow.setServiceAndOperationId(serviceAndOperationId);
                }
                if ((esbRelation.getTranName() != null && !esbRelation.getTranName().equals(interfaceRelationShow.getInterfaceName()))
                        || (esbRelation.getProviderSysIdAndName() != null && !esbRelation.getProviderSysIdAndName().equals(interfaceRelationShow.getServiceId()))) {
                    interfaceRelationShow.setInterfaceName(esbRelation.getTranName());
                    interfaceRelationShow.setServiceId(esbRelation.getProviderSysIdAndName());
                    updateNameList.add(interfaceRelationShow);
                }
            }
            // 获取callingParty的应用Id
            interfaceRelationShow.setCallingAppId(interfaceLazyInitService.getAppIdByName(interfaceRelationShow.getServiceCalling()));
            //判断transId是否为空 赋值接口别名
            if(FileUtil.isNullOrEmpty(interfaceRelationShow.getTransId())) {
            	interfaceRelationShow.setTransId(interfaceRelationShow.getInterfaceAlias());
            }
            interfaceRelationShowList.add(interfaceRelationShow);
        }
        // 更新为ESB最新接口名称及提供方名称
        interfaceRelationDao.updateSopRelationInterfaceName(updateNameList);
        return interfaceRelationShowList;
    }

    /**
     * 导出文件 接口
     *
     * @param fileName
     * @return
     */
    @Override
    public void exportFile(String fileName, HttpServletResponse response) throws Exception {
        String excelPath = tmpPath + Constants.INTERFACECALL + fileName;
        FileUtil.commonDown(fileName, excelPath, response);
    }

    @Override
    public void relateEsbData() {
        //关联esb初始化数据
        List<SoapRelation> soapRelationList = new ArrayList<>();
        List<SopRelation> sopRelationList = new ArrayList<>();
        List<SoapRelation> soapResult = interfaceRelationDao.addSoapField();
        List<SopRelation> sopResult = interfaceRelationDao.addSopField();
        List<String> soapTransIds = soapResult.stream().map(SoapRelation::getTransId).collect(Collectors.toList());
        List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelationByConsumerMsgType(soapTransIds);
        for (SoapRelation soapRelation : soapResult) {
            for (EsbRelation esbRelation : esbRelationList) {
                if (soapRelation.getTransId().equals(esbRelation.getServiceAndOperationId())) {
                    interfaceRelationDao.deleteSoap(soapRelation);
                    soapRelation.setEsbTransId(esbRelation.getTranId());
                    soapRelation.setEsbServiceId(esbRelation.getServiceId());
                    soapRelation.setEsbOperationId(esbRelation.getOperationId());
                    soapRelationList.add(soapRelation);
                    break;
                }
            }
        }
        interfaceRelationDao.saveSoapRelationList(soapRelationList);
        List<String> transIds = sopResult.stream().map(SopRelation::getTransId).collect(Collectors.toList());
        List<EsbRelation> esbRelationList1 = esbRelationDao.getEsbRelationByTranId(transIds);
        for (SopRelation sopRelation : sopResult) {
            for (EsbRelation esbRelation : esbRelationList1) {
                if (sopRelation.getTransId().equals(esbRelation.getTranId())) {
                    interfaceRelationDao.deleteSop(sopRelation);
                    sopRelation.setEsbTransId(esbRelation.getTranId());
                    sopRelation.setEsbServiceId(esbRelation.getServiceId());
                    sopRelation.setEsbOperationId(esbRelation.getOperationId());
                    sopRelationList.add(sopRelation);
                    break;
                }
            }
        }
        interfaceRelationDao.saveSopRelationList(sopRelationList);
    }

    private List<Map> getSoapAndSopId(InterfaceRelationShow interfaceRelationShow) {
        List<Map> interfaceId = new ArrayList<>();
        if ("REST".equals(interfaceRelationShow.getInterfaceType())) {
            if (!FileUtil.isNullOrEmpty(interfaceRelationShow.getInterfaceId())) {
                Map map = new HashMap();
                map.put("interfaceId", interfaceRelationShow.getInterfaceId());
                map.put("interfaceType", interfaceRelationShow.getInterfaceType());
                interfaceId.add(map);
            }
        } else if ("SOAP".equals(interfaceRelationShow.getInterfaceType())) {
            SoapRelation soapRelation = interfaceRelationDao.getSoapId(interfaceRelationShow.getEsbTransId());
            Map map = new HashMap();
            map.put("interfaceId", soapRelation.getId());
            map.put("interfaceType", soapRelation.getInterfaceType());
            interfaceId.add(map);
        } else if ("SOP".equals(interfaceRelationShow.getInterfaceType())) {
            SopRelation sopRelation = interfaceRelationDao.getSopId(interfaceRelationShow.getEsbTransId());
            Map map = new HashMap();
            map.put("interfaceId", sopRelation.getId());
            map.put("interfaceType", sopRelation.getInterfaceType());
            interfaceId.add(map);
        }
        return interfaceId;
    }

    @Override
    public List<RestRelation> queryRestRelation(String serviceCalling) {
        return interfaceRelationDao.getRestRelation(serviceCalling);
    }

    @Override
    public List<RestRelation> getRestRetation() {
        return interfaceRelationDao.queryRestRelation();
    }


    private List mapDistinct(List<Map> interfaceId) {
        List<Map> interfaceIdList = new ArrayList<>();
        Map msp = new HashMap();
        for (Map map : interfaceId) {
            String id = (String) map.get("interfaceId");
            map.remove("interfaceId");
            msp.put(id, map);
        }
        Set<String> mspKey = msp.keySet();
        for (String key : mspKey) {
            Map map = (Map) msp.get(key);
            map.put("interfaceId", key);
            interfaceIdList.add(map);

        }
        return interfaceIdList;
    }


    /**
     * 服务链路信息查询接口
     * 循环查询当前应用为调用方的rest和soap和sop接口列表信息
     */
    @Override
    public Map getServiceChainInfo(Map params) {
        String serviceId = (String) params.get(Dict.L_SERVICEID);
        String branch = (String) params.get(Dict.BRANCH);
        //查询当前应用信息
        Map<String, Object> appInfo = restTransportServiceImpl.getAppInfo(serviceId);
        Map<String, List<String>> totalMap = new HashMap<>();
        List<String> tmp = new ArrayList<>();
        tmp.add(serviceId);
        totalMap.put(serviceId, tmp);
        List<String> serviceIdList = Arrays.asList(serviceId);
        Map resultMap = new HashMap<>();
        resultMap.put("_appInfo", appInfo);
        //循环查询以当前应用为调用方的调用关系信息
        do {
            Map<String, Object> map = queryRelation(serviceIdList, totalMap, branch);
            serviceIdList = (List<String>) map.get("_appList");
            for (String key : map.keySet()) {
                if (!"_appList".equals(key)) {
                    List<Map<String, Object>> layer = new ArrayList<Map<String, Object>>();
                    Map<String, Object> relationMap = (Map) map.get(key);
                    relationMap.keySet().forEach(s -> {
                        List<Map> tempList = (ArrayList) relationMap.get(s);
                        Map<String, Object> tempMap = new HashMap<>();
                        tempMap.put(Dict.L_SERVICEID, s);
                        tempMap.put("relations", tempList);
                        layer.add(tempMap);
                    });
                    resultMap.put(key, layer);
                }
            }
        } while (CollectionUtils.isNotEmpty(serviceIdList));

        return resultMap;
    }

    /**
     * 查询serviceIdList中所有应用为调用方的且非total中为提供方的调用关系
     *
     * @param serviceIdList
     * @param total
     * @param branch
     * @return
     */
    private Map<String, Object> queryRelation(List<String> serviceIdList, Map<String, List<String>> totalMap, String branch) {
        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        for (String s : serviceIdList) {
            List<String> totalList = totalMap.get(s);
            InterfaceParamShow interfaceParamShow = new InterfaceParamShow();
            interfaceParamShow.setServiceCalling(s);
            interfaceParamShow.setBranchDefault(branch);
            List<Map> interfaceList = interfaceRelationDao.queryAllRelation(interfaceParamShow, totalList);
            if (CollectionUtils.isNotEmpty(interfaceList)) {
                List<Map> filteredInterfaceList = new ArrayList<>();
                Map<String, Object> relationMap = new HashMap<>();
                interfaceList.forEach(r -> {
                    String serviceId = (String) r.remove(Dict.SERVICE_ID);
                    if (StringUtils.isNotEmpty(serviceId)) {
                        r.put(Dict.ID, r.remove(Dict.L_ID).toString());
                        if (relationMap.containsKey(serviceId)) {
                            List<Map> list1 = (ArrayList) relationMap.get(serviceId);
                            list1.add(r);
                        } else {
                            List<Map> list1 = new ArrayList<>();
                            list1.add(r);
                            relationMap.put(serviceId, list1);
                            list.add(serviceId);
                            List<String> tmp = new ArrayList<>();
                            totalList.forEach(t -> {
                                tmp.add(t);
                            });
                            tmp.add(serviceId);
                            if (totalMap.containsKey(serviceId)) {
                                List<String> list2 = totalMap.get(serviceId);
                                list2.forEach(o -> {
                                    if (!tmp.contains(o)) {
                                        tmp.add(o);
                                    }
                                });
                            }
                            totalMap.put(serviceId, tmp);
                        }
                    }
                });
                map.put(s, relationMap);
            }
        }
        map.put("_appList", list);
        return map;
    }

    @Override
    public Map queryAllRelationByType(String interfaceType) {
        InterfaceParamShow interfaceParamShow = new InterfaceParamShow();
        interfaceParamShow.setBranchDefault(Dict.MASTER);
        List list = new ArrayList<>();
        if (interfaceType.equals(Dict.REST)) {
            list = getRestRelation(interfaceParamShow);
        } else if (interfaceType.equals(Dict.SOAP)) {
            list = getSoapRelation(interfaceParamShow);
        } else if (interfaceType.equals(Dict.SOP)) {
            list = getSopRelation(interfaceParamShow);
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.TOTAL, list.size());
        map.put(Dict.LIST, list);
        return map;
    }

}
