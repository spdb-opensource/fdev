package com.spdb.fdev.fdemand.spdb.service.impl;

import com.google.common.collect.Table.Cell;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.DemandEnum.DemandDeferStatus;
import com.spdb.fdev.fdemand.base.dict.DemandEnum.DemandStatusEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.DictDao;
import com.spdb.fdev.fdemand.spdb.dao.impl.DemandBaseInfoDaoImpl;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;

import net.bytebuddy.matcher.HasSuperTypeMatcher;

import org.apache.http.concurrent.Cancellable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class ExportExcelServiceImpl implements ExportExcelService {
	  private Logger logger = LoggerFactory.getLogger(this.getClass());
		@Autowired
		private IDemandBaseInfoService demandBaseInfoService;
		@Autowired
		private IImplementUnitService implementUnitService;
		@Autowired
		private IRoleService roleService;
		@Autowired
		private RestTransport restTransport;
		@Autowired
		private DemandBaseInfoDaoImpl demandBaseInfoDaoImpl;
		@Autowired
		private IFdevUserService fdevUserService;
		@Autowired
		private DictDao dictDao;

	@Override
	public void ToExportAssessExcelByDemandId(String id,HttpServletResponse resp) throws Exception{
		DemandBaseInfo demandBaseInfo=demandBaseInfoService.queryById(id);
		if (CommonUtils.isNullOrEmpty(demandBaseInfo)) {
			throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR, new String[]{id});
		}
		if (demandBaseInfo.getDemand_status_normal()<=DemandStatusEnum.EVALUATE.getValue()) {
			throw new FdevException(ErrorConstants.DEMAND_ASSESS_ERROR);
		}
		if (demandBaseInfo.getDemand_status_special()==DemandDeferStatus.DEFER.getValue()||demandBaseInfo.getDemand_status_special()==DemandDeferStatus.RECOVER.getValue()) {
			throw new FdevException(ErrorConstants.EXPORT_ASSESSEXPORT_ERROR);
		}


    	List<FdevImplementUnit> fdevImplementUnits= (List<FdevImplementUnit>) implementUnitService.queryByDemandId(id);
    	if (CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
			throw new FdevException(ErrorConstants.IMPLEMENTUNIT_NULL_ERROR, new String[]{id});
		}
    	String getid=demandBaseInfo.getId();

    	User user = CommonUtils.getSessionUser();
    	boolean isDemandManager = roleService.isDemandManager();

    	boolean isDemandLeader = roleService.isDemandLeader(id, user.getId());

		if (!(isDemandLeader||isDemandManager)) { throw new
		FdevException(ErrorConstants.ROLE_ERROR,new String[] {id}); }


    	Map<String,String> dataMap=new HashMap<String, String>();

		dataMap.put(Dict.OA_CONTACT_NAME, demandBaseInfo.getOa_contact_name());				//OA联系单名称
		dataMap.put(Dict.OA_CONTACT_NO, demandBaseInfo.getOa_contact_no());					//OA联系单编号 
		dataMap.put(Dict.OA_RECEIVE_DATE, demandBaseInfo.getOa_receive_date());				//OA收件日期
		dataMap.put(Dict.PROPOSE_DEMAND_DEPT, demandBaseInfo.getPropose_demand_dept());		//需求提出部门
		dataMap.put(Dict.PROPOSE_DEMAND_USER, demandBaseInfo.getPropose_demand_user());		//需求联系人
		dataMap.put(Dict.DEMAND_INSTRUCTION, demandBaseInfo.getDemand_instruction());			//需求说明书名称
		dataMap.put(Dict.DEMAND_PLAN_NO, demandBaseInfo.getDemand_plan_no());					//对应需求计划编号
		dataMap.put(Dict.DEMAND_PLAN_NAME, demandBaseInfo.getDemand_plan_name());				//需求计划名称
		dataMap.put(Dict.DEMAND_BACKGROUND, demandBaseInfo.getDemand_background());			//需求背景
		dataMap.put(Dict.FORMER_COMMUNICATION, demandBaseInfo.getFormer_communication());		//前期沟通情况
		dataMap.put(Dict.RESPECT_PRODUCT_DATE, demandBaseInfo.getRespect_product_date());		//需求方期望投产日期		
		dataMap.put(Dict.DEMAND_AVAILABLE, demandBaseInfo.getDemand_available());				//需求是否可行				
		dataMap.put(Dict.DEMAND_ASSESS_WAY, demandBaseInfo.getDemand_assess_way());			//需求评估方式						
		dataMap.put(Dict.FUTURE_ASSESS, demandBaseInfo.getFuture_assess());					//未来是否纳入后评估									
		dataMap.put(Dict.AVAILABLE_ASSESS_IDEA, demandBaseInfo.getAvailable_assess_idea());	//需求可行性评估意见
		dataMap.put(Dict.EXTRA_IDEA,demandBaseInfo.getExtra_idea());							//实施团队可行性评估补充意见


    	String sheetname="需求评估表";
		ExportAssessExcelByDemandId(dataMap,fdevImplementUnits, sheetname,resp );
	}

	@Override
	public void ExportAssessExcelByDemandId(Map<String, String> data, List<FdevImplementUnit> unitData,
			String sheetname, HttpServletResponse resp)  throws Exception{
		// 初始化workbook
		InputStream inputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		//引入模板
		try {
			ClassPathResource classPathResource=new ClassPathResource("assessModel.xlsx");
			inputStream=classPathResource.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			sheet = workbook.getSheetAt(0);
			logger.info("-------load model assessModelDemand success-----");
		} catch (Exception e1) {
			throw new FdevException("初始化模板失败");

		}

		//填充对应单元格数据
		XSSFRow textRow = sheet.getRow(3);
		textRow.getCell(1).setCellValue((String) data.get(Dict.OA_CONTACT_NO));				//OA联系单编号 
		  textRow.getCell(3).setCellValue((String) data.get(Dict.OA_CONTACT_NAME));			//OA联系单名称
		  textRow.getCell(5).setCellValue((String) data.get(Dict.OA_RECEIVE_DATE));			//OA收件日期
		  textRow.getCell(6).setCellValue((String)data.get(Dict.PROPOSE_DEMAND_DEPT));		//需求提出部门
		  textRow.getCell(8).setCellValue((String)data.get(Dict.PROPOSE_DEMAND_USER));		//需求联系人
		  textRow.getCell(9).setCellValue((String)data.get(Dict.DEMAND_INSTRUCTION));		//需求说明书名称
		  textRow.getCell(12).setCellValue((String) data.get(Dict.DEMAND_PLAN_NO));			//对应需求计划编号
		  textRow.getCell(14).setCellValue((String) data.get(Dict.DEMAND_PLAN_NAME));		//需求计划名称

		  XSSFRow textRow1 = sheet.getRow(5);

		  textRow1.getCell(1).setCellValue((String)data.get(Dict.DEMAND_BACKGROUND));		//需求背景
		  textRow1.getCell(8).setCellValue((String)data.get(Dict.FORMER_COMMUNICATION));	//前期沟通情况
		  textRow1.getCell(15).setCellValue((String)data.get(Dict.RESPECT_PRODUCT_DATE));	//需求方期望投产日期

		  XSSFRow textRow2 = sheet.getRow(8);

		  textRow2.getCell(1).setCellValue(DemandBaseInfoUtil.changeAvaliable((String)data.get(Dict.DEMAND_AVAILABLE)));		//需求是否可行
		  textRow2.getCell(3).setCellValue(DemandBaseInfoUtil.changeAssessWay((String)data.get(Dict.DEMAND_ASSESS_WAY)));		//需求评估方式
		  textRow2.getCell(5).setCellValue(DemandBaseInfoUtil.changeFutureAssess((String) data.get(Dict.FUTURE_ASSESS)));			//未来是否纳入后评估
		  textRow2.getCell(8).setCellValue((String)data.get(Dict.AVAILABLE_ASSESS_IDEA)); 	//需求可行性评估意见

		  XSSFRow textRow3 =sheet.getRow(10);
		  textRow3.getCell(1).setCellValue((String)data.get(Dict.EXTRA_IDEA));				//实施团队可行性评估补充意见
		  int i = 0;

		  if (unitData.size()>10) {

			  XSSFCellStyle style1=workbook.createCellStyle(); //添加边框，字体居中
			  style1.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			  style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			  style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			  style1.setBorderLeft(XSSFCellStyle.BORDER_MEDIUM);

			  XSSFCellStyle styleright=workbook.createCellStyle();//非最后一行，最右边框填充样式
			  styleright.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			  styleright.setBorderRight(XSSFCellStyle.BORDER_MEDIUM);

			  XSSFCellStyle styleleft=workbook.createCellStyle();//最后一行，最左样式填充
			  styleleft.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			  styleleft.setBorderRight(XSSFCellStyle.BORDER_THIN);
			  styleleft.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
			  styleleft.setBorderLeft(XSSFCellStyle.BORDER_MEDIUM);

			  XSSFCellStyle styleright1=workbook.createCellStyle();//最后一行，最右样式填充
			  styleright1.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
			  styleright1.setBorderRight(XSSFCellStyle.BORDER_MEDIUM);

			  XSSFCellStyle stylecell=workbook.createCellStyle();//非最后一行中间单元格填充
			  stylecell.setBorderRight(XSSFCellStyle.BORDER_THIN);
			  stylecell.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			  XSSFCellStyle stylecell1=workbook.createCellStyle();//最后一行中间单元格填充
			  stylecell1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			  stylecell1.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);

			  /*需求基础信息*/
			  for (FdevImplementUnit fdevImplementUnit :unitData) {
		            XSSFRow textRow_1 = sheet.createRow(13 + i);
		            textRow_1.createCell(1).setCellValue((Integer)(1+i));

		            textRow_1.createCell(2).setCellValue((String) unitData.get(i).getImplement_unit_content());
		            textRow_1.createCell(3).setCellValue((String) unitData.get(i).getImplement_lead_dept());
		            textRow_1.createCell(4).setCellValue((String) unitData.get(i).getImplement_lead_team());
		          //根据牵头人id发送接口获得姓名

		            textRow_1.createCell(5).setCellValue(CommonUtils.implementUnitLeader(unitData.get(i)));
		            String accountString=null;
		            if (CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_leader_account())) {
						accountString="无";
					}
		            else {
		            	accountString = String.valueOf((Object)unitData.get(i).getImplement_leader_account());
			            accountString = accountString.substring(1,accountString.length()-1).trim();
					}

		            textRow_1.createCell(6).setCellValue(accountString);
		            textRow_1.createCell(7).setCellValue((String)unitData.get(i).getProject_name());
		            textRow_1.createCell(8).setCellValue((String) unitData.get(i).getProject_no());
		            textRow_1.createCell(9).setCellValue((String) unitData.get(i).getPlan_start_date());
		            textRow_1.createCell(10).setCellValue((String) unitData.get(i).getPlan_test_date());
		            textRow_1.createCell(11).setCellValue("");
		            textRow_1.createCell(12).setCellValue("");
		            textRow_1.createCell(13).setCellValue((String) unitData.get(i).getRelate_system_name());
		            textRow_1.createCell(14).setCellValue((Double) unitData.get(i).getDept_workload());
		            textRow_1.createCell(15).setCellValue((Double) unitData.get(i).getCompany_workload());
		            textRow_1.createCell(16).setCellValue((String) unitData.get(i).getRemark());
		            textRow_1.getCell(1).setCellStyle(style1);


		            if (i==unitData.size()-1) {
		            	textRow_1.getCell(1).setCellStyle(styleleft);

		            	for (int j = 2; j <16; j++) {
			    			textRow_1.getCell(j).setCellStyle(stylecell1);
			    		}
		            	textRow_1.getCell(16).setCellStyle(styleright1);
		            	break;
					}

		            for (int j = 2; j < 16; j++) {
		    			textRow_1.getCell(j).setCellStyle(stylecell);
		    		}
		            textRow_1.getCell(16).setCellStyle(styleright);
		            i++;
		        }
		}else {
			for (FdevImplementUnit fdevImplementUnit :unitData) {
	            XSSFRow textRow_1 = sheet.getRow(13 + i);
	            textRow_1.getCell(1).setCellValue((Integer)(1+i));
	            textRow_1.getCell(2).setCellValue((String) unitData.get(i).getImplement_unit_content());
	            textRow_1.getCell(3).setCellValue((String) unitData.get(i).getImplement_lead_dept());
	            textRow_1.getCell(4).setCellValue((String) unitData.get(i).getImplement_lead_team());

	            textRow_1.getCell(5).setCellValue(CommonUtils.implementUnitLeader(unitData.get(i)));
	            String accountString=null;
	            if (CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_leader_account())) {
					accountString="无";
				}
	            else {
	            	accountString = String.valueOf((Object)unitData.get(i).getImplement_leader_account());
		            accountString = accountString.substring(1,accountString.length()-1).trim();
				}
	            textRow_1.getCell(6).setCellValue(accountString);
	            textRow_1.getCell(7).setCellValue((String)unitData.get(i).getProject_name());
	            textRow_1.getCell(8).setCellValue((String) unitData.get(i).getProject_no());
	            textRow_1.getCell(9).setCellValue((String) unitData.get(i).getPlan_start_date());
	            textRow_1.getCell(10).setCellValue((String) unitData.get(i).getPlan_test_date());
	            textRow_1.getCell(11).setCellValue("");
	            textRow_1.getCell(12).setCellValue("");
	            textRow_1.getCell(13).setCellValue((String) unitData.get(i).getRelate_system_name());
	            if (CommonUtils.isNullOrEmpty(unitData.get(i).getDept_workload())) {//判断是否为空
	            	textRow_1.getCell(14).setCellValue((""));
				}else {
					textRow_1.getCell(14).setCellValue((Double)unitData.get(i).getDept_workload());
				}
	            if (CommonUtils.isNullOrEmpty(unitData.get(i).getCompany_workload())) {//判断是否为空
	            	textRow_1.getCell(14).setCellValue((""));
				}else {
					textRow_1.getCell(14).setCellValue((Double)unitData.get(i).getDept_workload());
				}
	            textRow_1.getCell(15).setCellValue((Double) unitData.get(i).getCompany_workload());

	            textRow_1.getCell(16).setCellValue((String) unitData.get(i).getRemark());

	            i++;
	        }
		}





		  try { resp.reset(); resp.setContentType("application/octet-stream");
		  resp.setCharacterEncoding("UTF-8");
		  resp.setHeader("Access-Control-Allow-Origin", "*");
		  resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
		  resp.setHeader("Content-Disposition", "attachment;filename=" +
		  data.get(Dict.OA_CONTACT_NO)+ ".");
		  workbook.write(resp.getOutputStream()); } catch (IOException e) {
	      logger.error("导出需求评估表时，数据读写错误" + e.getMessage()); }

	}

	@Override
	public void exportDemandsExcel(Map map,HttpServletResponse resp) throws Exception{
		//查询全量需求标签
		Set<String> types = new HashSet();
		types.add(Dict.DEMANDLABEL);
		List<DictEntity> demand_label_list = dictDao.queryByTypes(types);
		List<DemandBaseInfo> demandBaseInfos = (List<DemandBaseInfo>)demandBaseInfoService.queryDemandList(map).get("demands");
		Map userMap = fdevUserService.queryByUserCoreData(demandBaseInfos.stream().filter(x -> !CommonUtils.isNullOrEmpty(x.getDemand_create_user_all()) && !CommonUtils.isNullOrEmpty(x.getDemand_create_user())).map(DemandBaseInfo::getDemand_create_user).collect(Collectors.toSet()), null);
		// 初始化workbook
		InputStream inputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		//引入模板
		try {
			ClassPathResource classPathResource=new ClassPathResource("DemandExport.xlsx");
			inputStream=classPathResource.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			sheet = workbook.getSheetAt(0);
			logger.info("-------load model DemandExprotList success-----");

		} catch (Exception e1) {
		    logger.error("---export---" + e1);
			throw new FdevException("需求导出失败，请联系fdev管理员");
		}
		int i=1;//行数
		for (DemandBaseInfo demandBaseInfo:demandBaseInfos) {
			int j=0;//列数
			sheet.createRow(i);
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getOa_contact_name());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getOa_contact_no());
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeDemandType(demandBaseInfo.getDemand_type()));
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeDemandProperty(demandBaseInfo.getDemand_property()));
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeDemandLabelList(demandBaseInfo.getDemand_label(),demand_label_list));
			if (!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_status_special())) {
				if (demandBaseInfo.getDemand_status_special()==1||demandBaseInfo.getDemand_status_special()==2) {
					sheet.getRow(i).createCell(j++).setCellValue("暂缓");
				}else {
					sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeStateCn(demandBaseInfo.getDemand_status_normal()));
				}
			}else {
				sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeStateCn(demandBaseInfo.getDemand_status_normal()));
			}

			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getOa_receive_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPropose_demand_dept());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPropose_demand_user());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_user());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_instruction());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_plan_no());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_plan_name());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_background());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getFormer_communication());
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeAvaliable(demandBaseInfo.getDemand_available()));//
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeAssessWay(demandBaseInfo.getDemand_assess_way()));//
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_record_no());
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeFutureAssess(demandBaseInfo.getFuture_assess()));//
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReview_user());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getRespect_product_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getAvailable_assess_idea());
			if(CommonUtils.isNullOrEmpty(demandBaseInfo.isUi_verify())) {//是否涉及UI审核
				sheet.getRow(i).createCell(j++).setCellValue("");
			}else {
				sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.isUi_verify()?"是":"否");
			}
			if (CommonUtils.isNullOrEmpty(demandBaseInfo.isIs_verify())) {//是否通过审核
				sheet.getRow(i).createCell(j++).setCellValue("");
			}else {
				sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.isIs_verify()?"通过":"不通过");
			}
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getUi_verify_user());
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changeDesignStatus(demandBaseInfo.getDesign_status()));//状态要修改
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_desc());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getExtra_idea());
			if(CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_create_user_all())){
				Object user = userMap.get(demandBaseInfo.getDemand_create_user());
				sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(user) ? "" : (String)(((Map)user).get("user_name_cn")));
			}else{
				sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_create_user_all().getUser_name_cn());//创建人id->名字
			}
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_create_time());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getDemand_leader_group_cn());//牵头小组id->姓名
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.demandLeader(demandBaseInfo));//set id->姓名
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.changePriority(demandBaseInfo.getPriority()));//
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getTech_type());//科技类型
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getTech_type_desc());//备注
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getAccept_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_start_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_inner_test_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_test_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_test_finish_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getPlan_product_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReal_start_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReal_inner_test_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReal_test_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReal_test_finish_date());
			sheet.getRow(i).createCell(j++).setCellValue(demandBaseInfo.getReal_product_date());
			sheet.getRow(i).createCell(j++).setCellValue(DemandBaseInfoUtil.relatePart(demandBaseInfo));//板块			
			i++;
		}
		try {
			resp.reset(); resp.setContentType("application/octet-stream");
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
			resp.setHeader("Content-Disposition", "attachment;filename=" +
			"DemandList"+ ".xlsx");
			workbook.write(resp.getOutputStream()); } catch (IOException e) {
		    logger.error("导出需求评估表时，数据读写错误" + e.getMessage());
		}

	}
	@Override
	public Map importDemandExcel(MultipartFile excel,String demandId) {
		DemandBaseInfo demandBaseInfo = demandBaseInfoDaoImpl.queryById(demandId);
		if (CommonUtils.isNullOrEmpty(demandBaseInfo)||CommonUtils.isNullOrEmpty(demandId)) {
			throw new FdevException("需求不存在");
		}
		Map dataMap = new HashMap();
		InputStream inputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		try {
			inputStream=excel.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			sheet = workbook.getSheetAt(0);
			logger.info("-------load model DemandExprotList success-----");

		} catch (Exception e1) {
		    logger.error("---export---" + e1);
			throw new FdevException("需求导入失败，请联系fdev管理员");

		}
		XSSFRow textRow = sheet.getRow(3);
		XSSFRow textRow1 = sheet.getRow(5);
		XSSFRow textRow2 = sheet.getRow(8);
		XSSFRow textRow3 =sheet.getRow(10);
		List<Integer> init = new ArrayList<Integer>();
		String[] exception = {"oa联系单名称需要为文本格式","oa联系单编号需要为文本格式","OA收件日期需要为yyyy/MM/dd",
				"需求提出部门需要为文本格式","需求联系人需要为文本格式","需求说明书名称需要为文本格式","需求计划编号需要为文本格式",
				"需求计划名称需要为文本格式","需求背景需要为文本格式","前期沟通情况需要为文本格式","期望投产日期格式：“yyyy/MM/dd”",
				"需求是否可行只可填写：可行、不可行、部分可行","需求评估方式只可以填写：业务需求评审 或 科技需求备案",
				"是否纳入后评估只可填写纳入后评估 或 不纳入后评估","需求可行性评估意见需要为文本格式","实施团队可行性评估补充意见需要为文本格式"};
		//单元格判空
		if (CommonUtils.isNullOrEmpty(textRow.getCell(3).toString())) {
			dataMap.put(Dict.OA_CONTACT_NAME, "");
		}else {
			if (textRow.getCell(3).getCellType()!=org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				init.add(0);
			}else {
				dataMap.put(Dict.OA_CONTACT_NAME, textRow.getCell(3).toString());				//OA联系单名称
			}

		}

		if (CommonUtils.isNullOrEmpty(textRow.getCell(1).toString())) {
			throw new FdevException("需求评估表中的OA联系单编号为空，请补全后再上传！");
		}else {
			if (!CommonUtils.isNullOrEmpty(demandBaseInfo.getOa_real_no())) {
				if (!demandBaseInfo.getOa_real_no().equals(textRow.getCell(1).toString())) {
					throw new FdevException("需求评估表中OA联系单编号与已有不符，请确认好再上传！");
				}
			}
			if (textRow.getCell(1).getCellType()!=org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				init.add(1);
			}else {
				dataMap.put(Dict.OA_REAL_NO, textRow.getCell(1).toString());						//OA联系单编号 
			}

		}
		if (CommonUtils.isNullOrEmpty(textRow.getCell(5).toString())) {

		}else {
			if (textRow.getCell(5).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) {//OA收件日期
				dataMap.put(Dict.OA_RECEIVE_DATE, textRow.getCell(5).getDateCellValue());
			}else {
				init.add(2);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow.getCell(6).toString())) {

		}else {
			if (textRow.getCell(6).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.PROPOSE_DEMAND_DEPT, textRow.getCell(6).toString());		//需求提出部门
			}else {
				init.add(3);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow.getCell(8).toString())) {

		}else {
			if (textRow.getCell(8).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.PROPOSE_DEMAND_USER, textRow.getCell(8).toString());		//需求联系人
			}else {
				init.add(4);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow.getCell(9).toString())) {

		}else {
			if (textRow.getCell(9).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.DEMAND_INSTRUCTION, textRow.getCell(9).toString());			//需求说明书名称
			}else {
				init.add(5);
			}
		}

		if (CommonUtils.isNullOrEmpty(textRow.getCell(12).toString())) {

		}else {
			if (textRow.getCell(12).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.DEMAND_PLAN_NO, textRow.getCell(12).toString());					//对应需求计划编号
			}else {
				init.add(6);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow.getCell(14).toString())) {

		}else {
			if (textRow.getCell(14).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.DEMAND_PLAN_NAME, textRow.getCell(14).toString());				//需求计划名称
			}else {
				init.add(7);
			}
		}

		if (CommonUtils.isNullOrEmpty(textRow1.getCell(1).toString())) {

		}else {
			if (textRow1.getCell(1).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.DEMAND_BACKGROUND, textRow1.getCell(1).toString());			//需求背景
			}else {
				init.add(8);
			}
		}

		if (CommonUtils.isNullOrEmpty(textRow1.getCell(8).toString())) {

		}else {
			if (textRow1.getCell(8).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.FORMER_COMMUNICATION, textRow1.getCell(8).toString());		//前期沟通情况
			}else {
				init.add(9);
			}
		}

		if (CommonUtils.isNullOrEmpty(textRow1.getCell(15).toString())) {

		}else {
			if (textRow1.getCell(15).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) {//需求方期望投产日期	
				dataMap.put(Dict.RESPECT_PRODUCT_DATE, textRow1.getCell(15).getDateCellValue());
			}else {
				init.add(10);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow2.getCell(1).toString())) {

		}else {
			if (("可行".equals(textRow2.getCell(1).toString()))||("不可行".equals(textRow2.getCell(1).toString()))||("部分可行".equals(textRow2.getCell(1).toString()))) {
				dataMap.put(Dict.DEMAND_AVAILABLE, textRow2.getCell(1).toString());				//需求是否可行	
			}else {
				init.add(11);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow2.getCell(3).toString())) {

		}else {
			if ("业务需求评审".equals(textRow2.getCell(3).toString())||"科技需求备案".equals(textRow2.getCell(3).toString())) {
				dataMap.put(Dict.DEMAND_ASSESS_WAY, textRow2.getCell(3).toString());			//需求评估方式	
			}else {
				init.add(12);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow2.getCell(5).toString())) {

		}else {
			if ("纳入后评估".equals(textRow2.getCell(5).toString())||"不纳入后评估".equals(textRow2.getCell(5).toString())) {
				dataMap.put(Dict.FUTURE_ASSESS,  textRow2.getCell(5).toString());					//未来是否纳入后评估	
			}else {
				init.add(13);
			}
		}

		if (CommonUtils.isNullOrEmpty(textRow2.getCell(8).toString())) {

		}else {
			if (textRow2.getCell(8).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.AVAILABLE_ASSESS_IDEA, textRow2.getCell(8).toString());	//需求可行性评估意见
			}else {
				init.add(14);
			}
		}
		if (CommonUtils.isNullOrEmpty(textRow3.getCell(1).toString())) {

		}else {
			if (textRow3.getCell(1).getCellType()==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
				dataMap.put(Dict.EXTRA_IDEA,textRow3.getCell(1).toString());							//实施团队可行性评估补充意见
			}else {
				init.add(15);
			}
		}
		if (!CommonUtils.isNullOrEmpty(init)) {
			String error= exception(init, exception);
			throw new FdevException(error);
		}


		return dataMap;
	}
	@Override
	public void exportModelExcel(HttpServletResponse resp) {
		InputStream inputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		//引入模板
		try {
			ClassPathResource classPathResource=new ClassPathResource("assessModel.xlsx");
			inputStream=classPathResource.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			sheet = workbook.getSheetAt(0);
			logger.info("-------load model assessModelDemand success-----");
		} catch (Exception e1) {
			throw new FdevException("下载需求评估表模板失败");

		}
		try {
			resp.reset(); resp.setContentType("application/octet-stream");
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
			resp.setHeader("Content-Disposition", "attachment;filename=" +
			"AssessModel"+ ".xlsx");
			workbook.write(resp.getOutputStream()); } catch (IOException e) {
			logger.error("导出需求评估表失败" + e.getMessage());
		}
	}
	private String exception(List<Integer> ints,String[] strings) {
		String result = "";
		for (int i = 0; i < ints.size(); i++) {
			result+=strings[ints.get(i)]+"，";
		}
		result=result.substring(0, result.length()-1);
		return result;
	}
}
