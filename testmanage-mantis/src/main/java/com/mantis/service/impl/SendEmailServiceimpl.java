package com.mantis.service.impl;

import com.mantis.dict.Constants;
import com.mantis.dict.Dict;
import com.mantis.service.SendEmailService;
import com.mantis.util.MyUtil;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@RefreshScope
public class SendEmailServiceimpl implements SendEmailService{
	@Value("${ftms.tplan.updateMantisEmail}")
	private String updateMantisEmail;
	
	@Autowired
	private MyUtil myUtil;
	
	@Autowired
	private RestTransport restTransport;
	
	private static final Logger logger = LoggerFactory.getLogger(SendEmailServiceimpl.class);
	
	@Async("taskExecutor")
	@Override
	public void updateMantis(Map map) throws Exception {
		map.put(Constants.UPDATETASKUSER, map.get(Dict.USER_NAME));
		String ftmsPlanUrl = myUtil.getFtmsActiveIp(updateMantisEmail);
		try {
			restTransport.submit(ftmsPlanUrl.toString(), map);
		}catch (Exception e){
			logger.error("sendemail error"+e.getStackTrace());
		}		
	}

}
