package com.auto.common;

/**
 * 自动生成方法涉及sql
 * 
 * @author baimy
 *
 */
public class CommonModuleSql {
	/**
	 * 生成组件java 微服务模块组件方法查询
	 * 
	 * @param name
	 * @return
	 */
	public static String Modulesql(String name) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append("select module_no,module_name,module_name_cn,module_group ");
		sqlstr.append("from tmdb_sit.AUTO_MODULE where module_group = '");
		sqlstr.append(name + "' ");
		sqlstr.append("and deleted = '0'");
		return sqlstr.toString();
	}

	/**
	 * 生成组件java 组件元素关系查询
	 * 
	 * @param name
	 * @return
	 */
	public static String ModuleDetailSql(String name) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append("SELECT ");
		sqlstr.append("m.module_no,");
		sqlstr.append("m.module_name,");
		sqlstr.append("md.element_step_no,");
		sqlstr.append("e.element_content,");
		sqlstr.append("e.element_type,");
		sqlstr.append("e.element_name,");
		sqlstr.append("e.element_DIR,");
		sqlstr.append("md.element_DYNAMIC_TYPE,");
		sqlstr.append("md.element_data,");
		sqlstr.append("md.EXE_TIMES ");
		sqlstr.append("FROM ");
		sqlstr.append("tmdb_sit.AUTO_MODULE_DETAIL md ");
		sqlstr.append("LEFT JOIN ");
		sqlstr.append("tmdb_sit.AUTO_ELEMENT e ON md.ELEMENT_ID = e.ELEMENT_ID AND md.DELETED = '0' AND e.DELETED = '0' ");
		sqlstr.append("INNER JOIN ");
		sqlstr.append("tmdb_sit.AUTO_MODULE m ON m.MODULE_ID = md.MODULE_ID AND m.DELETED = '0'");
		sqlstr.append("AND m.module_no = '");
		sqlstr.append(name);
		sqlstr.append("' ORDER BY cast(md.element_step_no as unsigned integer) ");
		return sqlstr.toString();
	}
	/**
	 * 生成执行java	案例明细关联元素单步骤查询
	 * @param name
	 * @return
	 */
	public static String caseDetailSql(String stepno,String testcase){
		StringBuffer sqlstr = new StringBuffer();
//		sqlstr.append(" select c.element_action,c.element_data,c.assert_id,");
//		sqlstr.append(" e.element_no,e.element_type,e.element_name,c.operation_number from case_detail c,element e");
//		sqlstr.append(" where  c.element_id = e.element_id and c.testcase_no ='" + testcase + "'");
//		sqlstr.append(" and   c.step_no = '" + stepno + "'");
		
		sqlstr.append(" select c.element_DYNAMIC_TYPE,c.element_data,c.assert_id,");
		sqlstr.append(" e.element_CONTENT,e.element_type,e.element_name,c.EXE_TIMES");
		sqlstr.append(" from tmdb_sit.AUTO_CASE_DETAIL c,tmdb_sit.AUTO_ELEMENT e");
		sqlstr.append(" where  c.element_id = e.element_id ");
		sqlstr.append(" and c.testcase_no ='" + testcase + "'");
		sqlstr.append(" and   c.step_no = '" + stepno + "'");
		sqlstr.append(" and   c.DELETED = '0'");
		sqlstr.append(" and   e.DELETED = '0'");
		
		return sqlstr.toString();
	}
	/**
	 * 生成执行java	案例明细单步骤查询
	 * @param name
	 * @return
	 */
	public static String caseDetailSql2(String stepno,String testcase){
		StringBuffer sqlstr = new StringBuffer();
//		sqlstr.append(" select c.element_action,c.element_data,c.assert_id,c.operation_number,c.operation_number  from case_detail c");
//		sqlstr.append(" where  c.testcase_no ='" + testcase + "'");
//		sqlstr.append(" and   c.step_no = '" + stepno + "'");
		sqlstr.append(" select c.element_dynamic_type,c.element_data,c.assert_id,c.exe_times");
		sqlstr.append(" from tmdb_sit.AUTO_CASE_DETAIL c");
		sqlstr.append(" where  c.testcase_no = '" + testcase + "'");
		sqlstr.append(" and   c.step_no = '" + stepno + "'");
		sqlstr.append(" and   c.deleted = '0'");
		return sqlstr.toString();
	}
	/**
	 * 生成案例执行执行java 查询案例
	 * 
	 * @param testcaseNo
	 * @param meunName
	 * @return
	 */
	public static String testcaseFileSql(String testcaseNo, String meunName) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append(" select fd.testcase_no,fd.func_point,fd.testcase_name,fd.precondition,fd.testcase_desc,fd.expected_result,d.menu_name,d.secondary_menu,d.menu_no ");
		sqlstr.append(" from tmdb_sit.AUTO_CASE fd,tmdb_sit.AUTO_MENU_SHEET d");
		sqlstr.append(" where fd.menu_sheet_id = d.menu_sheet_id");
		sqlstr.append("  and d.deleted = '0'");
		sqlstr.append("  and fd.valid_flag = '0'");
		sqlstr.append("  and fd.deleted = '0'");
		sqlstr.append("  and d.menu_no = '" + meunName + "'");
		if (!testcaseNo.isEmpty()) {
			if(testcaseNo.contains(";")) {
				sqlstr.append(" and fd.testcase_no in (");
				String[] split = testcaseNo.split(";");
				int i = 0;
				for(String n : split) {
					if(n != null && !"".equals(n)) {
						if(i == 0) {
							sqlstr.append("'").append(n).append("'");
						} else {
							sqlstr.append(",'").append(n).append("'");
						}
						i ++;
					}
				}
				sqlstr.append(")");
			} else {
				sqlstr.append(" and fd.testcase_no = '" + testcaseNo + "'");
			}
			
		}
		return sqlstr.toString();
	}

	/**
	 * 生成案例执行执行java 案例需要的需要的组件
	 * 
	 * @param testcaseNo
	 * @return
	 */
	public static String testcaseModuleSql(String testcaseNo) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append("select distinct dm.step_no,dm.testcase_no,dm.module_id,dm.element_dynamic_type, dm.module_group,dm.module_name,dm.module_name_cn,dm.element_id ");
		sqlstr.append(" from tmdb_sit.AUTO_CASE_DETAIL cd,");
		sqlstr.append(" (select fd.testcase_no,fd.step_no,fd.module_id,fd.element_dynamic_type,m.module_group,m.module_name,m.module_name_cn,fd.element_id");
		sqlstr.append(" from tmdb_sit.AUTO_CASE_DETAIL fd left join tmdb_sit.AUTO_MODULE m ");
		sqlstr.append(" on fd.module_id = m.module_id and fd.deleted = '0' and m.deleted = '0') dm ");
		sqlstr.append(" where cd.testcase_no = dm.testcase_no");
		sqlstr.append(" and cd.testcase_no = '" + testcaseNo + "'");
		sqlstr.append(" and cd.deleted = '0'");
		sqlstr.append(" group by dm.step_no");
		sqlstr.append("  order by cast(dm.step_no as unsigned integer);");
		/*
		 * sqlstr.append(
		 * "  select m.module_group,m.module_name,m.module_annotation from case_detail_data fd,module_data m where fd.module_id = m.module_id"
		 * ); sqlstr.append("  and fd.testcase_no ='"+testcaseNo+"'");
		 */
		return sqlstr.toString();
	}

	/**
	 * 生成案例执行执行java 查询案例组件所有分组
	 * 
	 * @param testcaseNo
	 * @return
	 */
	public static String testcaseGroupSql(String testcaseNo) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append(" select distinct m.module_group from tmdb_sit.AUTO_CASE_DETAIL fd,tmdb_sit.AUTO_MODULE m where fd.module_id = m.module_id");
		sqlstr.append("  and fd.testcase_no = '" + testcaseNo + "'");
		sqlstr.append("  and fd.deleted = '0'");
		return sqlstr.toString();
	}

	/**
	 * 查询案例数据明细
	 * 
	 * @param testcaseNo
	 * @param tableName
	 * @return
	 */
	public static String testcaseDetailSql(String testcaseNo) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append(" select ad.assertdata1,ad.assertdata2,ad.assertdata3,ad.assertdata4,ad.assertdata5,ad.assertdata6,ad.assertdata7,ad.assertdata8,ad.assertdata9,ad.assertdata10,");
		sqlstr.append(" md.menu_name,f.testcase_no,f.func_point,f.testcase_name,f.precondition,f.testcase_desc,f.expected_result,fd.*,p.username,p.queryPwd");
		sqlstr.append(" from tmdb_sit.AUTO_CASE f,tmdb_sit.AUTO_CASE_DATA fd,tmdb_sit.AUTO_USER p,tmdb_sit.AUTO_MENU_SHEET md,tmdb_sit.AUTO_ASSERT ad");
		sqlstr.append(" where f.data_id = fd.data_id and f.user_id = p.user_id ");
		sqlstr.append(" and f.deleted = '0'");
		sqlstr.append(" and fd.deleted = '0'");
		sqlstr.append(" and p.deleted = '0'");
		sqlstr.append(" and md.deleted = '0'");
		sqlstr.append(" and f.valid_flag = '0'");
		sqlstr.append(" and f.menu_sheet_id = md.menu_sheet_id and f.assert_id = ad.assert_id and f.testcase_no ='" + testcaseNo + "'");
		return sqlstr.toString();
	}

	/**
	 * 查询组件所需的数据
	 * 
	 * @param name
	 * @return
	 */
	public static String CaseModuleDetailSql(String name) {
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append(" select m.module_no,m.module_name,detail.element_step_no,detail.element_content,detail.element_name,detail.element_dir,detail.element_dynamic_type,detail.element_data,detail.exe_times");
		sqlstr.append("  from tmdb_sit.AUTO_MODULE m,");
		sqlstr.append("  (select d.element_step_no,d.module_detail_id,d.module_id,e.element_content,e.element_name,e.element_dir,d.element_dynamic_type,d.element_data,d.exe_times");
		sqlstr.append("  from tmdb_sit.AUTO_MODULE_DETAIL d left join tmdb_sit.AUTO_ELEMENT e ");
		sqlstr.append("  on d.element_id = e.element_id and d.deleted = '0' and e.deleted = '0') detail ");
		sqlstr.append("  where m.module_id = detail.module_id");
		sqlstr.append("  and m.deleted = '0'");
		sqlstr.append(" and m.module_name = '" + name + "'");
		sqlstr.append(" group by detail.element_step_no");
		return sqlstr.toString();
	}
	
	public static String caseElementsql(String testcaseNo){
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append("select  a.step_no, a.element_dynamic_type,a.element_data,a.assert_id,a.element_content,a.element_type,a.element_name,");
		sqlstr.append(" a.exe_times from (");
		sqlstr.append(" select  c.step_no,c.element_dynamic_type,c.element_data,c.assert_id,e.element_content,e.element_type,e.element_name,");
		sqlstr.append(" c.exe_times from tmdb_sit.AUTO_CASE_DETAIL c,tmdb_sit.AUTO_ELEMENT e");
		sqlstr.append("  where  c.element_id = e.element_id and c.testcase_no ='" + testcaseNo + "' and c.deleted = '0' and e.deleted = '0'");
		sqlstr.append(" union ");
		sqlstr.append(" select  c.step_no,c.element_dynamic_type,c.element_data,c.assert_id,'','','',");
		sqlstr.append(" c.exe_times from tmdb_sit.AUTO_CASE_DETAIL c");
		sqlstr.append("  where  c.testcase_no ='" + testcaseNo + "' and c.deleted = '0'");
		sqlstr.append("  and c.element_id is null");
		sqlstr.append(" and c.module_id is null");
		sqlstr.append(" ) a group by a.step_no");
		sqlstr.append("  order by cast(a.step_no as unsigned integer);");
		/*sqlstr.append("select e.element_no,e.element_type from case_detail_data c,element_data e");
		sqlstr.append(" where c.element_id = e.element_id");
		sqlstr.append(" and c.testcase_no = '" + testcaseNo + "'");
		sqlstr.append("  and   c.step_no = '" + stepNo + "'");*/
		return sqlstr.toString();
	}
	
	public static String caseElementsql2(String testcaseNo, String stepNo){
		StringBuffer sqlstr = new StringBuffer();
		sqlstr.append(" select c.element_dynamic,c.element_data,c.assert_id,c.exe_times from tmdb_sit.AUTO_CASE_DETAIL c");
		sqlstr.append(" where  c.testcase_no ='" + testcaseNo + "'");
		sqlstr.append(" and   c.step_no = '" + stepNo + "'");
		sqlstr.append(" and   c.deleted = '0'");
		return sqlstr.toString();
	}
	

}
