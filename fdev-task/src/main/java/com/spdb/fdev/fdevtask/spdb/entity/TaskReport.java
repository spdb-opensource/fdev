package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="任务报告")
public class TaskReport {
	
	@ApiModelProperty(value="提交代码次数")
	private int commit_code_num;
	
	@ApiModelProperty(value="文档数")
	private int doc_num;
	
	@ApiModelProperty(value="生产问题")
	private String[] product_problem;
	
	
	public TaskReport(){
		
	}
	

	public int getCommit_code_num() {
		return commit_code_num;
	}
	public void setCommit_code_num(int commit_code_num) {
		this.commit_code_num = commit_code_num;
	}
	public int getDoc_num() {
		return doc_num;
	}
	public void setDoc_num(int doc_num) {
		this.doc_num = doc_num;
	}
	public String[] getProduct_problem() {
		return product_problem;
	}
	public void setProduct_problem(String[] product_problem) {
		this.product_problem = product_problem;
	}
	
	
	

}
