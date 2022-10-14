package com.mantis.service.impl;

import com.mantis.dict.Dict;
import com.mantis.service.FtmsService;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class FtmsServiceImpl implements FtmsService{
	@Autowired
	private RestTransport restTransport;

	@Override
	public String queryWorkNoByTaskId(String task_id) throws Exception {
		String work_no = "";
		Map<String,String> submit = new HashMap<String,String>();
		submit.put(Dict.TASK_ID, task_id);
		submit.put(Dict.REST_CODE, "query.workno.task.url");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		if(!Util.isNullOrEmpty(task_id)) {
			work_no = (String) restTransport.submitWithHeaders(submit, headers);
		}
		return work_no;
	}

}
