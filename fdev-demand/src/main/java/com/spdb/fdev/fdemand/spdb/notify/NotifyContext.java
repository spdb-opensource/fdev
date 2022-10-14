package com.spdb.fdev.fdemand.spdb.notify;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotifyContext {

    private static final Logger logger = LoggerFactory.getLogger(NotifyContext.class);

    @Autowired
    private Map<String, INotifyStrategy> notifyMap;


    public void notifyContext(String notifyType, Map<String, Object> param) {
        INotifyStrategy notifyStrategy;
        if (Dict.DemandDeferNotifyImpl.equals(notifyType)) {
            logger.info("开始进行需求暂缓邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.DemandDeferNotifyImpl);
        } else if (Dict.DemandRecoverNotifyImpl.equals(notifyType)) {
            logger.info("开始进行需求恢复邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.DemandRecoverNotifyImpl);
        } else if (Dict.DemandInfoNotifyImpl.equals(notifyType)) {
        	logger.info("开始进行需求录入邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.DemandInfoNotifyImpl);
		} else if (Dict.DemandUpdateNotifyImpl.equals(notifyType)) {
        	logger.info("开始进行需求更新邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.DemandUpdateNotifyImpl);
		}else if (Dict.UiUploadedImpl.equals(notifyType)) {
        	logger.info("开始进行UI设计稿已上传邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.UiUploadedImpl);
		}else if (Dict.UiAuditWaitImpl.equals(notifyType)) {
        	logger.info("开始进行UI设计稿待审核邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.UiAuditWaitImpl);
		}else if (Dict.UiAuditPassImpl.equals(notifyType)) {
        	logger.info("开始进行UI设计稿审核通过邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.UiAuditPassImpl);
		}else if (Dict.UiAuditPassNotImpl.equals(notifyType)) {
        	logger.info("开始进行UI设计稿审核未通过邮件发送，进入{}策略", notifyType);
            notifyStrategy = notifyMap.get(Dict.UiAuditPassNotImpl);
		}
        else {
            logger.info("当前类型{}无邮件发送策略", notifyType);
            return;
        }
        notifyStrategy.doNotify(param);
    }
   
}

